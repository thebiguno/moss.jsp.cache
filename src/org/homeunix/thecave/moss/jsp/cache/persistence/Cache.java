package org.homeunix.thecave.moss.jsp.cache.persistence;

import org.homeunix.thecave.moss.jsp.cache.config.CacheConfig;

public interface Cache {
	public CachedRequest get(String uri, CacheConfig config);
	public void put(String uri, CacheConfig config, CachedRequest request);
	public Long getCacheDate(String uri, CacheConfig config);
}
