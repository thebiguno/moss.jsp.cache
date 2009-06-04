package org.homeunix.thecave.moss.jsp.cache.persistence.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.homeunix.thecave.moss.jsp.cache.config.Config;
import org.homeunix.thecave.moss.jsp.cache.config.PersistenceBacking;
import org.homeunix.thecave.moss.jsp.cache.persistence.CachedResponse;
import org.homeunix.thecave.moss.jsp.cache.persistence.Persistence;

public class DiskPersistence implements Persistence {
	public static final String CACHE_PATH_PARAMETER = "cache-path";
	
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private final File cachePath;
	
	
	public DiskPersistence(Config config) {
		PersistenceBacking backing = config.getPersistenceBacking(this.getClass().getName());
		String cachePath = null;
		if (backing != null){
			String itemCount = backing.getParameter(CACHE_PATH_PARAMETER);
			if (itemCount != null){
				cachePath = itemCount;
			}
		}
		
		if (cachePath == null)
			cachePath = System.getProperty("java.io.tmpdir", "/tmp") + "/org.homeunix.thecave.moss.jsp.cache";

		this.cachePath = new File(cachePath);
	}
	
	public synchronized CachedResponse get(String url, Config config) {
		File fileCache = getFileCache(url, config);
		try {
			FileInputStream fis = new FileInputStream(fileCache);
			long startTime = System.currentTimeMillis();
			ObjectInputStream objectInputStream = new ObjectInputStream(fis);
			Object persistedObject = objectInputStream.readObject();
			long endTime = System.currentTimeMillis();
			if (persistedObject instanceof CachedResponse){
				logger.finer("Found '" + url + "' in disk cache");
				logger.finest("Time to de-serialize response: " + (endTime - startTime) + " milliseconds");
				return (CachedResponse) persistedObject;
			}
		}
		catch (Exception e){
			logger.log(Level.WARNING, "Error retrieving '" + url + " in disk cache", e);
		}
		return null;
	}
	
	public synchronized void put(String url, Config config, CachedResponse request) {
		File fileCache = getFileCache(url, config);
		
		byte[] value = request.getRequestData();
		if (value == null || value.length == 0){
			fileCache.delete();
			logger.finer("Request data was empty; not caching, and removing existing cache (if it exists).");
			return;
		}
		
		try {
			logger.finer("Stored '" + url + "' in disk cache at '" + fileCache.getAbsolutePath() + "'");
			if (!cachePath.exists())
				cachePath.mkdirs();
			if (!fileCache.exists())
				fileCache.createNewFile();
			long startTime = System.currentTimeMillis();
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(fileCache));
			os.writeObject(request);
			os.flush();
			os.close();
			fileCache.setLastModified(request.getCachedDate());
			long endTime = System.currentTimeMillis();
			logger.finest("Time to serialize response: " + (endTime - startTime) + " milliseconds");
		}
		catch (IOException ioe){
			logger.log(Level.WARNING, "Unable to store " + url + " to persistent cache", ioe);
		}
	}
	
	public synchronized Long getCacheDate(String url, Config config){
		File fileCache = getFileCache(url, config);
		if (!fileCache.exists())
			return null;
		return fileCache.lastModified();
	}
		
	private File getFileCache(String url, Config config){
		return new File(cachePath.getAbsolutePath() + File.separator + url.replaceAll("[^0-9a-zA-Z-_]", "_"));
	}
}
