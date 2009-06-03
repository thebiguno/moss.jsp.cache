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
	
	public CachedResponse get(String uri, Config config) {
		//Return the first cached request which is found
		for (Persistence cache : cacheList) {
			logger.finest("Looking at cache " + cache.getClass().getName());
			Long cacheDate = cache.getCacheDate(uri, config);
			logger.finest("Cache " + cache.getClass().getName() + " date is " + cacheDate);
			if (cacheDate != null){
				if (isCacheFresh(uri, config, cacheDate)){
					logger.finest("Cache " + cache.getClass().getName() + " is fresh");
					CachedResponse request = cache.get(uri, config);
					if (request != null)
						logger.finest("Returning stored request from cache " + cache.getClass().getName());
						return request;
				}
			}
		}
		
		return null;
	}
	
	public void put(String uri, Config config, CachedResponse request) {
		//Put the cachedRequest in all persistence stores
		for (Persistence cache : cacheList) {
			cache.put(uri, config, request);
		}
	}
	
	public Long getCacheDate(String uri, Config config){
		//Return the first date for the uri which is found
		for (Persistence cache : cacheList) {
			if (cache.getCacheDate(uri, config) != null)
				return cache.getCacheDate(uri, config);
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
