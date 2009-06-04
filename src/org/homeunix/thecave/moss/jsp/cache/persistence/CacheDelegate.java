package org.homeunix.thecave.moss.jsp.cache.persistence;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.homeunix.thecave.moss.jsp.cache.config.Config;

/**
 * The single persistence class which the CacheFilter interacts with.  This class will pass
 * on cache requests to all configured persistence backing implementations, such as memory,
 * disk, etc. 
 * 
 * @author wyatt
 *
 */
public class CacheDelegate {
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	
	private final List<Persistence> cacheList = new ArrayList<Persistence>();
	
	public CacheDelegate(Config config) {
		List<String> cacheClassImpls = config.getPersistenceBackingClassNames();
		for (String string : cacheClassImpls) {
			try {
				@SuppressWarnings("unchecked")
				Class<Persistence> cacheImpl = (Class<Persistence>) Class.forName(string);
				Constructor<Persistence> constuctor = cacheImpl.getConstructor(Config.class);
				Persistence cache = constuctor.newInstance(config);
				cacheList.add(cache);
			} 
			catch (ClassNotFoundException e) {
				logger.log(Level.CONFIG, "I could not find cache implementation '" + string + ".", e);
			} 
			catch (Exception e) {
				logger.log(Level.CONFIG, "Error initializing '" + string + ".", e);
			} 
		}
	}
	
	public CachedResponse get(String url, Config config) {
		//Return the first cached request which is found.  Once a request is found, ensure that
		// it is present in all caches.
		for (Persistence cache : cacheList) {
			logger.finest("Looking at cache " + cache.getClass().getName());
			Long cacheDate = cache.getCacheDate(url, config);
			if (cacheDate != null){
				if (isCacheFresh(url, config, cacheDate)){
					CachedResponse response = cache.get(url, config);
					if (response != null){
						logger.finer("Returning stored response for " + url + " from cache " + cache.getClass().getName());
						updateAllPersistenceBackings(response, config);
						return response;
					}
				}
			}
		}
		
		return null;
	}
	
	private void updateAllPersistenceBackings(CachedResponse response, Config config){
		for (Persistence cache : cacheList) {
			Long cacheDate = cache.getCacheDate(response.getUrl(), config);
			if (cacheDate == null){
				logger.finest("Updating cache " + cache.getClass().getName() + " with " + response.getUrl());
				cache.put(response.getUrl(), config, response);
			}
		}
	}
	
	public void put(String url, Config config, CachedResponse response) {
		if (url == null 
				|| config == null 
				|| response == null 
				|| response.getCachedDate() == 0
				|| response.getRequestData() == null
				|| response.getRequestData().length == 0)
			return;
		
		//Put the cachedRequest in all persistence stores
		for (Persistence cache : cacheList) {
			cache.put(url, config, response);
		}
	}
	
	public Long getCacheDate(String url, Config config){
		if (url == null || config == null)
			return null;
		
		//Return the first date for the url which is found; null if it is not found in any cache
		for (Persistence cache : cacheList) {
			if (cache.getCacheDate(url, config) != null)
				return cache.getCacheDate(url, config);
		}
		
		return null;
	}

	private boolean isCacheFresh(String uri, Config config, long cachedItemDate){
		//We want to find if the cachedItemDate is within (greater than) X seconds ago (where X is expiry time for the URI)
		if (cachedItemDate + config.getExpiryTime(uri) * 1000 < System.currentTimeMillis())
			return false;

		return true;
	}

}
