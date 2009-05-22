package org.homeunix.thecave.moss.jsp.cache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.homeunix.thecave.moss.common.StreamUtil;

/**
 * A persistent cache, backed by both memory and disk.  We use a weak hash map to 
 * let the GC automatically cleanse objects from cache if more memory is needed,
 * while still allowing some memory caching as needed.
 * 
 * @author wyatt
 *
 */
public class PersistentCache extends WeakHashMap<String, byte[]>{
	private final Logger logger = Logger.getLogger(PersistentCache.class.toString());

	private final File cacheDir;
	public PersistentCache(File cacheDir) {
		this.cacheDir = cacheDir;
	}
	
	@Override
	public byte[] get(Object key) {
		if (super.get(key) != null)
			return super.get(key);
		return getFromPersistentCache(key);
	}
	
	@Override
	public byte[] put(String key, byte[] value) {
		if (value != null && value.length == 0)
			value = null;
		
		super.put(key, value);
		putInPersistentCache(key, value);
		return null;
	}
	
	private byte[] getFromPersistentCache(Object key){
		File fileCache = getFileCache(key.toString());
		if (!fileCache.exists())
			return null;
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
	
	private void putInPersistentCache(String key, byte[] value){
		File fileCache = getFileCache(key);
		
		if (value == null){
			fileCache.delete();
			return;
		}
		
		try {
			fileCache.createNewFile();
			FileOutputStream fos = new FileOutputStream(fileCache);
			fos.write(value);
			fos.flush();
			fos.close();
		}
		catch (IOException ioe){
			logger.log(Level.SEVERE, "Unable to store data to persistent cache", ioe);
		}
	}
	
	private File getFileCache(String key){
		return new File(cacheDir.getAbsolutePath() + File.separator + key.replaceAll("[^0-9a-zA-Z-_]", "_"));
	}
}
