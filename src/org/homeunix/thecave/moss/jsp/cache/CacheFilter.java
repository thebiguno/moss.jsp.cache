/*
 * Created on May 28, 2008 by wyatt
 */
package org.homeunix.thecave.moss.jsp.cache;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CacheFilter implements Filter {
	
	private Map<String, byte[]> cache = null;
	
	public void init(FilterConfig config) throws ServletException {
		String cachePath = config.getInitParameter("cache-path");
		if (cachePath == null)
			cachePath = System.getProperty("java.io.tmpdir") + "/" + CacheFilter.class.getName();
		File cacheDir = new File(cachePath);
		if (!cacheDir.exists()){
			cacheDir.mkdirs();
		}
		
		String cacheExpiryString = config.getInitParameter("cache-expiry");
		if (cacheExpiryString == null || cacheExpiryString.length() == 0)
			cacheExpiryString = "0";
		long cacheExpirySeconds = Long.parseLong(cacheExpiryString);
		
		cache = Collections.synchronizedMap(new PersistentCache(cacheDir, cacheExpirySeconds));
	}
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		//Verify that this is an HttpServletRequest, and ignore those which are not.
		if (!(req instanceof HttpServletRequest)){
			chain.doFilter(req, res);
			return;
		}
			
		String uri = ((HttpServletRequest) req).getRequestURL().toString();
		if (cache.get(uri) != null){
			res.getOutputStream().write(cache.get(uri));
			return;
		}
		
		CachingServletResponseWrapper responseWrapper = new CachingServletResponseWrapper((HttpServletResponse) res);		
		chain.doFilter(req, responseWrapper);
		cache.put(uri, responseWrapper.getData());
	}
	
	public void destroy() {
		cache = null;
	}
}