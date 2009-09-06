package ca.digitalcave.moss.jsp.cache.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("cache-mapping")
public class CacheMapping {
	@XStreamAsAttribute
	private String pattern;
	@XStreamAsAttribute
	private long expiryTime;

	public long getExpiryTime() {
		return expiryTime;
	}
	public String getPattern() {
		return pattern;
	}
	
	public void setExpiryTime(long expiryTime) {
		this.expiryTime = expiryTime;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
}
