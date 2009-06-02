package org.homeunix.thecave.moss.jsp.cache.persistence;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.homeunix.thecave.moss.common.StreamUtil;
import org.homeunix.thecave.moss.jsp.cache.config.CacheConfig;

public class DiskCache implements Cache {
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	
	public synchronized CachedRequest get(String uri, CacheConfig config) {
		File fileCache = getFileCache(uri, config);
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			FileInputStream fis = new FileInputStream(fileCache);
			StreamUtil.copyStream(fis, baos);
			logger.fine("Retrieved cached item from disk");
			return new CachedRequest(baos.toByteArray());
		}
		catch (IOException ioe){			
			return null;
		}
	}
	
	public synchronized void put(String uri, CacheConfig config, CachedRequest request) {
		byte[] value = request.getRequestData();
		if (value != null && value.length == 0)
			value = null;
		
		File fileCache = getFileCache(uri, config);
		
		if (value == null){
			fileCache.delete();
			return;
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
	
	public synchronized Long getCacheDate(String uri, CacheConfig config){
		File fileCache = getFileCache(uri, config);
		if (!fileCache.exists())
			return null;
		return fileCache.lastModified();
	}
		

	
	private File getFileCache(String uri, CacheConfig config){
		return new File(config.getCacheFolder(uri).getAbsolutePath() + File.separator + uri.replaceAll("[^0-9a-zA-Z-_]", "_"));
	}
}
