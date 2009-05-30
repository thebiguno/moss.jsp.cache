package org.homeunix.thecave.moss.jsp.cache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.homeunix.thecave.moss.common.StreamUtil;
import org.homeunix.thecave.moss.jsp.cache.config.CacheConfig;

/**
 * A persistent cache, which stores bytes to disk.
 * 
 * @author wyatt
 *
 */
public class PersistentCache {
	private final Logger logger = Logger.getLogger(PersistentCache.class.toString());

	public synchronized byte[] get(String uri, CacheConfig config) {
		if (!isCachedItemCurrent(uri, config))
			return null;

		File fileCache = getFileCache(uri, config);
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			FileInputStream fis = new FileInputStream(fileCache);
			StreamUtil.copyStream(fis, baos);
			return baos.toByteArray();
		}
		catch (IOException ioe){			
			return null;
		}
	}
	
	public synchronized void put(String uri, CacheConfig config, byte[] value) {
		if (value != null && value.length == 0)
			value = null;
		
		File fileCache = getFileCache(uri, config);
		
		if (value == null){
			fileCache.delete();
			return;
		}
		
		try {
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
			logger.log(Level.SEVERE, "Unable to store data to persistent cache", ioe);
		}
	}
	
	/**
	 * Checks if a given URI is cached, and whether it is stale or not.
	 * @param uri URI to check 
	 * @param config CacheConfig settings
	 * @return true if the cached item exists, and is newer than (greater than) X seconds ago; otherwise, return false.
	 */
	public synchronized boolean isCachedItemCurrent(String uri, CacheConfig config){
		Long cachedItemDate = getCachedItemDate(uri, config);
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
