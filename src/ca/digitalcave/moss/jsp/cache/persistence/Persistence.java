package ca.digitalcave.moss.jsp.cache.persistence;

import ca.digitalcave.moss.jsp.cache.config.Config;

public interface Persistence {
	public CachedResponse get(String url, Config config);
	public void put(String url, Config config, CachedResponse response);
	public Long getCacheDate(String url, Config config);
}
