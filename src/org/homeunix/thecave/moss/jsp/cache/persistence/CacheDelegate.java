package org.homeunix.thecave.moss.jsp.cache.persistence;

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
	
	private final List<PersistenceBacking> cacheList = new ArrayList<PersistenceBacking>();
	
	@SuppressWarnings("unchecked")
	public CacheDelegate(List<String> cacheClassImpls) {
		for (String string : cacheClassImpls) {
			try {
				Class<PersistenceBacking> cacheImpl = (Class<PersistenceBacking>) Class.forName(string);
				PersistenceBacking cache = cacheImpl.newInstance();
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
	
	public synchronized CachedRequest get(String uri, Config config) {
		//Return the first cached request which is found
		for (PersistenceBacking cache : cacheList) {
			logger.finest("Looking at cache " + cache.getClass().getName());
			Long cacheDate = cache.getCacheDate(uri, config);
			logger.finest("Cache " + cache.getClass().getName() + " date is " + cacheDate);
			if (cacheDate != null){
				if (isCacheFresh(uri, config, cacheDate)){
					logger.finest("Cache " + cache.getClass().getName() + " is fresh");
					CachedRequest request = cache.get(uri, config);
					if (request != null)
						logger.finest("Returning stored request from cache " + cache.getClass().getName());
						return request;
				}
			}
		}
		
		return null;
	}
	
	public synchronized void put(String uri, Config config, CachedRequest request) {
		//Put the cachedRequest in all persistence stores
		for (PersistenceBacking cache : cacheList) {
			cache.put(uri, config, request);
		}
	}
	
	public synchronized Long getCacheDate(String uri, Config config){
		//Return the first date for the uri which is found
		for (PersistenceBacking cache : cacheList) {
			if (cache.getCacheDate(uri, config) != null)
				return cache.getCacheDate(uri, config);
		}
		
		return null;
	}

	private synchronized boolean isCacheFresh(String uri, Config config, long cachedItemDate){
		//We want to find if the cachedItemDate is within (greater than) X seconds ago (where X is expiry time for the URI)
		if (cachedItemDate + config.getExpiryTime(uri) * 1000 < System.currentTimeMillis())
			return false;

		return true;
	}

}
