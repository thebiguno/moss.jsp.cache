package org.homeunix.thecave.moss.jsp.cache.persistence;

import org.homeunix.thecave.moss.jsp.cache.config.Config;

public interface Persistence {
	public CachedResponse get(String uri, Config config);
	public void put(String uri, Config config, CachedResponse request);
	public Long getCacheDate(String uri, Config config);
}
