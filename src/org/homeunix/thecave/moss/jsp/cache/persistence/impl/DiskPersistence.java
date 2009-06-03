package org.homeunix.thecave.moss.jsp.cache.persistence.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.homeunix.thecave.moss.common.StreamUtil;
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
	
	public synchronized CachedResponse get(String uri, Config config) {
		//TODO Support headers / metadata
		File fileCache = getFileCache(uri, config);
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			FileInputStream fis = new FileInputStream(fileCache);
			StreamUtil.copyStream(fis, baos);
			logger.finer("Found '" + uri + "' in disk cache");
			CachedResponse cachedResponse = new CachedResponse();
			cachedResponse.setRequestData(baos.toByteArray());
			return cachedResponse;
		}
		catch (IOException ioe){
			logger.log(Level.WARNING, "Error retrieving '" + uri + " in disk cache", ioe);
			return null;
		}
	}
	
	public synchronized void put(String uri, Config config, CachedResponse request) {
		//TODO Support headers / metadata
		File fileCache = getFileCache(uri, config);
		
		byte[] value = request.getRequestData();
		if (value == null || value.length == 0){
			fileCache.delete();
			logger.finer("Request data was empty; not caching, and removing existing cache (if it exists).");
			return;
		}
		
		try {
			logger.finer("Stored '" + uri + "' in disk cache at '" + fileCache.getAbsolutePath() + "'");
			if (!cachePath.exists())
				cachePath.mkdirs();
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
	
	public synchronized Long getCacheDate(String uri, Config config){
		File fileCache = getFileCache(uri, config);
		if (!fileCache.exists())
			return null;
		return fileCache.lastModified();
	}
		
	private File getFileCache(String uri, Config config){
		return new File(cachePath.getAbsolutePath() + File.separator + uri.replaceAll("[^0-9a-zA-Z-_]", "_"));
	}
	
//	private File getCacheFolder(String uri, Config config){
//		String cachePath = config.getPersistenceBacking(this.getClass().getName()).getParameter(CACHE_PATH_PARAMETER);
//		if (cachePath == null)
//			cachePath = System.getProperty("java.io.tmpdir", "/tmp") + "/org.homeunix.thecave.moss.jsp.cache";
//
//		return new File(cachePath);
//	}
}
