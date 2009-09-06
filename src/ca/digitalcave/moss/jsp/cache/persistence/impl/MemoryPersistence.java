package ca.digitalcave.moss.jsp.cache.persistence.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import ca.digitalcave.moss.collections.ConcurrentHistoryMap;
import ca.digitalcave.moss.jsp.cache.config.Config;
import ca.digitalcave.moss.jsp.cache.config.PersistenceBacking;
import ca.digitalcave.moss.jsp.cache.persistence.CachedResponse;
import ca.digitalcave.moss.jsp.cache.persistence.Persistence;

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
	
	public CachedResponse get(String url, Config config) {
		if (memoryCache.containsKey(url)){
			logger.finer("Found '" + url + "' in memory cache");
			return memoryCache.get(url);
		}
		
		return null;
	}
	
	public void put(String url, Config config, CachedResponse request) {
		byte[] value = request.getRequestData();
		if (value != null && value.length == 0){
			logger.finer("Request data was empty; not caching, and removing existing cache (if it exists).");
			memoryCache.remove(url);
			return;
		}

		memoryCache.put(url, request);
		logger.finer("Stored '" + url + "' in memory cache; " + memoryCache.size() + "/" + memoryCache.getCapacity() + " responses stored.");
	}
	
	public Long getCacheDate(String url, Config config) {
		if (memoryCache.get(url) != null)
			return memoryCache.get(url).getCachedDate();
		return null;
	}
}
