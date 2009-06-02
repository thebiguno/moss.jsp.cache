package org.homeunix.thecave.moss.jsp.cache.persistence;

import java.util.logging.Logger;

import org.homeunix.thecave.moss.collections.HistoryMap;
import org.homeunix.thecave.moss.jsp.cache.config.CacheConfig;

public class MemoryCache implements Cache {
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	
	private final HistoryMap<String, CachedRequest> memoryCache = new HistoryMap<String, CachedRequest>();
	
	public synchronized CachedRequest get(String uri, CacheConfig config) {
		if (memoryCache.containsKey(uri)){
			logger.finer("Found '" + uri + "' in memory cache");
			return memoryCache.get(uri);
		}
		
		return null;
	}
	
	public synchronized void put(String uri, CacheConfig config, CachedRequest request) {
		memoryCache.put(uri, request);
		logger.finer("Put '" + uri + "' in memory cache");
	}
	
	public synchronized Long getCacheDate(String uri, CacheConfig config) {
		if (memoryCache.get(uri) != null)
			return memoryCache.get(uri).getCachedDate();
		return null;
	}
}
