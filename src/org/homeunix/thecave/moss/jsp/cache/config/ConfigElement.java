package org.homeunix.thecave.moss.jsp.cache.config;

public class ConfigElement {

	private long expiryTimeSeconds;
	private String cacheFolder;
	
	public ConfigElement() {
		// TODO Auto-generated constructor stub
	}
	
	public String getCacheFolder() {
		return cacheFolder;
	}
	public void setCacheFolder(String cacheFolder) {
		this.cacheFolder = cacheFolder;
	}
	
	public long getExpiryTimeSeconds() {
		return expiryTimeSeconds;
	}
	public void setExpiryTimeSeconds(long expiryTimeSeconds) {
		this.expiryTimeSeconds = expiryTimeSeconds;
	}
}
