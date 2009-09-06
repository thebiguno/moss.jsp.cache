package ca.digitalcave.moss.jsp.cache.persistence;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Represents a cached request, either in memory or on disk.  This includes whatever
 * headers were on the original request, along with the request data and the date 
 * that this request was cached.
 * 
 * @author wyatt
 *
 */
public class CachedResponse implements Serializable {
	public static final long serialVersionUID = 0;
	
	private String url;
	private long cachedDate;
	private final Map<String, String> headers = new HashMap<String, String>();
	private String contentType;
	private Locale locale;
	private byte[] requestData;
	

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public long getCachedDate() {
		return cachedDate;
	}		
	public void setCachedDate(long cachedDate) {
		this.cachedDate = cachedDate;
	}
	
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public Locale getLocale() {
		return locale;
	}
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	public void addHeaders(Map<String, String> headers) {
		this.headers.putAll(headers);
	}
	public void addHeader(String name, String value){
		this.headers.put(name, value);
	}
	public Map<String, String> getHeaders() {
		return headers;
	}
	
	public byte[] getRequestData() {
		return requestData;
	}
	public void setRequestData(byte[] requestData) {
		this.requestData = requestData;
	}
}
