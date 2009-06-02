package org.homeunix.thecave.moss.jsp.cache.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.homeunix.thecave.moss.jsp.cache.config.CacheConfig;

/**
 * A persistent cache, which stores bytes to disk.  Also stores the given number of entries
 * in a memory cache for even faster access.
 * 
 * @author wyatt
 *
 */
public class CacheManager {
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	
	private final List<Cache> cacheList = new ArrayList<Cache>();
	
	@SuppressWarnings("unchecked")
	public CacheManager(List<String> cacheClassImpls) {
		for (String string : cacheClassImpls) {
			try {
				Class<Cache> cacheImpl = (Class<Cache>) Class.forName(string);
				Cache cache = cacheImpl.newInstance();
				cacheList.add(cache);
			} 
			catch (ClassNotFoundException e) {
				logger.log(Level.CONFIG, "I could not find cache implementation '" + string + ".", e);
			} 
			catch (InstantiationException e) {
				logger.log(Level.CONFIG, "Error initializing '" + string + ".", e);
			} 
			catch (IllegalAccessException e) {
				logger.log(Level.CONFIG, "Illegal access while initializing '" + string + ".", e);
			}
		}
	}
	
	public synchronized CachedRequest get(String uri, CacheConfig config) {
		//Return the first cached request which is found
		for (Cache cache : cacheList) {
			if (isCacheFresh(uri, config, cache.getCacheDate(uri, config))){
				CachedRequest request = cache.get(uri, config);
				if (request != null)
					return cache.get(uri, config);
			}
		}
		
		return null;
	}
	
	public synchronized void put(String uri, CacheConfig config, CachedRequest request) {
		//Put the cachedRequest in all persistence stores
		for (Cache cache : cacheList) {
			cache.put(uri, config, request);
		}
	}

	private synchronized boolean isCacheFresh(String uri, CacheConfig config, long cachedItemDate){
		//We want to find if the cachedItemDate is within (greater than) X seconds ago (where X is expiry time for the URI)
		if (cachedItemDate + config.getExpiryTimeSeconds(uri) * 1000 < System.currentTimeMillis())
			return false;

		return true;
	}

}
