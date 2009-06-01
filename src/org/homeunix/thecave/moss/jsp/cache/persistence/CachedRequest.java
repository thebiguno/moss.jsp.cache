package org.homeunix.thecave.moss.jsp.cache.persistence;

import java.util.Map;

/**
 * Represents a cached request, either in memory or on disk.  This includes whatever
 * headers were on the original request, along with the request data and the date 
 * that this request was cached.
 * 
 * @author wyatt
 *
 */
public class CachedRequest {

	private long cachedDate;
	private Map<String, String> headers;
	private byte[] requestData;
	
	//TODO Add headers as well
	public CachedRequest(byte[] requestData) {
		this.requestData = requestData;
		this.cachedDate = System.currentTimeMillis();
	}
	
	public long getCachedDate() {
		return cachedDate;
	}
	
	public Map<String, String> getHeaders() {
		return headers;
	}
	
	public byte[] getRequestData() {
		return requestData;
	}
}
