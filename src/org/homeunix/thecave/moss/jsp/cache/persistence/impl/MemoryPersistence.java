package org.homeunix.thecave.moss.jsp.cache.persistence.impl;

import java.util.logging.Logger;

import org.homeunix.thecave.moss.collections.HistoryMap;
import org.homeunix.thecave.moss.jsp.cache.config.Config;
import org.homeunix.thecave.moss.jsp.cache.persistence.CachedRequest;
import org.homeunix.thecave.moss.jsp.cache.persistence.PersistenceBacking;

public class MemoryPersistence implements PersistenceBacking {
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	
	private final HistoryMap<String, CachedRequest> memoryCache = new HistoryMap<String, CachedRequest>();
	
	public static final String ITEM_COUNT = "item-count";
	
	public synchronized CachedRequest get(String uri, Config config) {
		if (memoryCache.containsKey(uri)){
			logger.finer("Found '" + uri + "' in memory cache");
			return memoryCache.get(uri);
		}
		
		return null;
	}
	
	public synchronized void put(String uri, Config config, CachedRequest request) {
		byte[] value = request.getRequestData();
		if (value != null && value.length == 0){
			logger.finer("Request data was empty; not caching, and removing existing cache (if it exists).");
			memoryCache.remove(uri);
			return;
		}

		memoryCache.put(uri, request);
		logger.finer("Stored '" + uri + "' in memory cache");
	}
	
	public synchronized Long getCacheDate(String uri, Config config) {
		if (memoryCache.get(uri) != null)
			return memoryCache.get(uri).getCachedDate();
		return null;
	}
}
