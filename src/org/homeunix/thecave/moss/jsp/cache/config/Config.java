package org.homeunix.thecave.moss.jsp.cache.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.homeunix.thecave.moss.jsp.cache.persistence.CacheDelegate;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("cache")
public class Config {

	private transient Map<String, PersistenceBacking> persistenceBackingsByClass;
	private transient Map<Pattern, CacheMapping> cacheMappingsByPattern;
	
	//The following lists are just to allow XStream to store things properly.
	@XStreamImplicit
	private List<CacheMapping> cacheMappings = new ArrayList<CacheMapping>();
	@XStreamImplicit
	private List<PersistenceBacking> persistenceBackings = new ArrayList<PersistenceBacking>();	
	@XStreamImplicit(itemFieldName="ignore-header")
	private Set<String> headerBlacklist = new HashSet<String>();
	
	private CacheDelegate cacheDelegate = null;
	
	public PersistenceBacking getPersistenceBacking(String className){
		return persistenceBackingsByClass.get(className);
	}
	
	public Collection<PersistenceBacking> getPersistenceBackingsByClass() {
		return Collections.unmodifiableCollection(persistenceBackingsByClass.values());
	}
	public Collection<CacheMapping> getCacheElements() {
		return Collections.unmodifiableCollection(cacheMappingsByPattern.values());
	}
	public Set<String> getHeaderBlacklist() {
		return Collections.unmodifiableSet(headerBlacklist);
	}
	
	public void addPersistenceBacking(PersistenceBacking cacheBacking){
		this.persistenceBackingsByClass.put(cacheBacking.getClassName(), cacheBacking);
		this.persistenceBackings.add(cacheBacking);
		this.cacheDelegate = null;
	}
	public void addCacheMapping(CacheMapping cacheMapping){
		this.cacheMappingsByPattern.put(Pattern.compile(cacheMapping.getPattern()), cacheMapping);
		this.cacheMappings.add(cacheMapping);
	}
	public void addHeaderBlacklist(String blacklistEntry){
		this.headerBlacklist.add(blacklistEntry);
	}
	
	/**
	 * Returns the expiry time (in seconds) for the given URI.  This is set on a per-match
	 * basis.  If multiple patterns match, the first one (defined by the order they are 
	 * declared in the XML) will be used.
	 * 
	 * If the URI doesn't match any patterns, it will return 0.
	 */
	public long getExpiryTime(String uri){
		for (Pattern pattern : cacheMappingsByPattern.keySet()) {
			if (pattern.matcher(uri).matches()) {
				CacheMapping cacheMapping = cacheMappingsByPattern.get(pattern);
				if (cacheMapping != null) {
					return cacheMapping.getExpiryTime();
				}
			}
		}
		return 0;
	}
	
	/**
	 * Does the given URI match at least one cache mapping pattern? 
	 * @param uri The URI to match
	 * @return true if the URI matches at least one pattern, false otherwise.
	 */
	public boolean isMatched(String uri){
		for (Pattern pattern : cacheMappingsByPattern.keySet()) {
			if (pattern.matcher(uri).matches())
				return true;
		}
		return false;
	}

	/**
	 * Returns the cache delegate.  This will always be kept up to date whenever new
	 * persistence backings are added, so make sure you always reference this getter,
	 * and don't cache the object for multiple requests.
	 * @return
	 */
	public CacheDelegate getCacheDelegate(){
		if (cacheDelegate == null)
			cacheDelegate = new CacheDelegate(new ArrayList<String>(persistenceBackingsByClass.keySet()));
		return cacheDelegate;
	}
	
	private Object readResolve() {
		cacheMappingsByPattern = new LinkedHashMap<Pattern, CacheMapping>();
		for (CacheMapping cacheMapping : cacheMappings) {
			cacheMappingsByPattern.put(Pattern.compile(cacheMapping.getPattern()), cacheMapping);
		}
		
		persistenceBackingsByClass = new LinkedHashMap<String, PersistenceBacking>();
		for (PersistenceBacking persistenceBacking : persistenceBackings) {
			persistenceBackingsByClass.put(persistenceBacking.getClassName(), persistenceBacking);
		}
		return this;
	}
}
