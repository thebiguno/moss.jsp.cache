package org.homeunix.thecave.moss.jsp.cache.persistence;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.homeunix.thecave.moss.collections.HistoryMap;
import org.homeunix.thecave.moss.common.StreamUtil;
import org.homeunix.thecave.moss.jsp.cache.config.CacheConfig;

/**
 * A persistent cache, which stores bytes to disk.  Also stores the given number of entries
 * in a memory cache for even faster access.
 * 
 * @author wyatt
 *
 */
public class PersistentCache {
	//TODO Add thread safety for both PersistentCache and MemoryCache
	private final Logger logger = Logger.getLogger(PersistentCache.class.toString());
	
	private final HistoryMap<String, CachedRequest> memoryCache = new HistoryMap<String, CachedRequest>(0); 

	public void setMemoryCacheItemCapacity(int capacity){
		this.memoryCache.setCapacity(capacity);
	}
	
	public int getMemoryCacheItemCapacity(){
		return memoryCache.getCapacity();
	}
	
	public synchronized byte[] get(String uri, CacheConfig config) {
		if (!isCachedItemCurrent(uri, config))
			return null;

		//TODO Return the entire cached request object, so that headers, etc can be set.
		if (memoryCache.get(uri) != null){
			logger.fine("Retrieved cached item from memory");
			return memoryCache.get(uri).getRequestData();
		}
		
		File fileCache = getFileCache(uri, config);
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			FileInputStream fis = new FileInputStream(fileCache);
			StreamUtil.copyStream(fis, baos);
			logger.fine("Retrieved cached item from disk");
			return baos.toByteArray();
		}
		catch (IOException ioe){			
			return null;
		}
	}
	
	public synchronized void put(String uri, CacheConfig config, byte[] value) {
		if (memoryCache.getCapacity() != config.getMemoryCacheItemCapacity())
			memoryCache.setCapacity(config.getMemoryCacheItemCapacity());
		
		if (value != null && value.length == 0)
			value = null;
		
		File fileCache = getFileCache(uri, config);
		
		if (value == null){
			fileCache.delete();
			return;
		}
		
		if (memoryCache.getCapacity() > 0){
			logger.fine("Persisting " + uri + " to memory cache");
			memoryCache.put(uri, new CachedRequest(value));
		}
		
		try {
			logger.fine("Persisting " + uri + " to file " + fileCache.getAbsolutePath());
			if (!config.getCacheFolder(uri).exists())
				config.getCacheFolder(uri).mkdirs();
			if (!fileCache.exists())
				fileCache.createNewFile();
			FileOutputStream fos = new FileOutputStream(fileCache);
			fos.write(value);
			fos.flush();
			fos.close();
			fileCache.setLastModified(System.currentTimeMillis());
		}
		catch (IOException ioe){
			logger.log(Level.WARNING, "Unable to store " + uri + " to persistent cache", ioe);
		}
	}
	
	/**
	 * Checks if a given URI is cached, and whether it is stale or not.
	 * @param uri URI to check 
	 * @param config CacheConfig settings
	 * @return true if the cached item exists, and is newer than (greater than) X seconds ago; otherwise, return false.
	 */
	public synchronized boolean isCachedItemCurrent(String uri, CacheConfig config){
		Long cachedItemDate;
		
		//If the cached item is in memory, it is guaranteed to also be on disk.  The reverse is not the case.
		if (memoryCache.get(uri) != null)
			cachedItemDate = memoryCache.get(uri).getCachedDate();
		else
			cachedItemDate = getCachedItemDate(uri, config);
		
		if (cachedItemDate == null)
			return false;

		//We want to find if the cachedItemDate is within (greater than) X seconds ago (where X is expiry time for the URI)
		if (cachedItemDate + config.getExpiryTimeSeconds(uri) * 1000 < System.currentTimeMillis())
			return false;

		return true;
	}
	
	/**
	 * Returns the date at which the item was cached.  If the file doesn't exist, return null.
	 * @param uri
	 * @param config
	 * @return
	 */
	public synchronized Long getCachedItemDate(String uri, CacheConfig config){
		File fileCache = getFileCache(uri, config);
		if (!fileCache.exists())
			return null;
		return fileCache.lastModified();
	}
		
	private File getFileCache(String uri, CacheConfig config){
		return new File(config.getCacheFolder(uri).getAbsolutePath() + File.separator + uri.replaceAll("[^0-9a-zA-Z-_]", "_"));
	}
}
