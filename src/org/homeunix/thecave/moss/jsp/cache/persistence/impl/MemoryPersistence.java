package org.homeunix.thecave.moss.jsp.cache.persistence.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.homeunix.thecave.moss.collections.ConcurrentHistoryMap;
import org.homeunix.thecave.moss.jsp.cache.config.Config;
import org.homeunix.thecave.moss.jsp.cache.config.PersistenceBacking;
import org.homeunix.thecave.moss.jsp.cache.persistence.CachedResponse;
import org.homeunix.thecave.moss.jsp.cache.persistence.Persistence;

public class MemoryPersistence implements Persistence {
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	
	private final ConcurrentHistoryMap<String, CachedResponse> memoryCache = new ConcurrentHistoryMap<String, CachedResponse>();
	
	public static final String ITEM_COUNT_PARAMETER = "item-count";
	public static final int DEFAULT_ITEM_COUNT = 32;
	
	public MemoryPersistence(Config config) {
		PersistenceBacking backing = config.getPersistenceBacking(this.getClass().getName());
		if (backing != null){
			String itemCount = backing.getParameter(ITEM_COUNT_PARAMETER);
			if (itemCount != null){
				try {
					int capacity = Integer.parseInt(itemCount);
					memoryCache.setCapacity(capacity);
				}
				catch (NumberFormatException nfe){
					logger.log(Level.CONFIG, "Cannot parse item count; setting to default cache size of " + DEFAULT_ITEM_COUNT, nfe);
					memoryCache.setCapacity(DEFAULT_ITEM_COUNT);
				}
			}
		}
	}
	
	public CachedResponse get(String uri, Config config) {
		if (memoryCache.containsKey(uri)){
			logger.finer("Found '" + uri + "' in memory cache");
			return memoryCache.get(uri);
		}
		
		return null;
	}
	
	public void put(String uri, Config config, CachedResponse request) {
		byte[] value = request.getRequestData();
		if (value != null && value.length == 0){
			logger.finer("Request data was empty; not caching, and removing existing cache (if it exists).");
			memoryCache.remove(uri);
			return;
		}

		memoryCache.put(uri, request);
		logger.finer("Stored '" + uri + "' in memory cache; " + memoryCache.size() + "/" + memoryCache.getCapacity() + " responses stored.");
	}
	
	public Long getCacheDate(String uri, Config config) {
		if (memoryCache.get(uri) != null)
			return memoryCache.get(uri).getCachedDate();
		return null;
	}
}
