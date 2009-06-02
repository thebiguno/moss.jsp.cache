package org.homeunix.thecave.moss.jsp.cache.persistence;

import org.homeunix.thecave.moss.jsp.cache.config.Config;

public interface PersistenceBacking {
	public CachedRequest get(String uri, Config config);
	public void put(String uri, Config config, CachedRequest request);
	public Long getCacheDate(String uri, Config config);
}
