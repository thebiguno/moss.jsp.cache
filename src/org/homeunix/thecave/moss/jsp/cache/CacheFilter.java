/*
 * Created on May 28, 2008 by wyatt
 */
package org.homeunix.thecave.moss.jsp.cache;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.homeunix.thecave.moss.jsp.cache.config.CacheConfig;

/**
 * A caching filter which works with the browser (via HTTP headers) to keep
 * network traffice down as much as possible.
 * 
 * Accepts the following init-params:
 *	config (where the cache.xml config file is located, relative to /WEB-INF/) 
 * 
 * @author wyatt
 *
 */
public class CacheFilter implements Filter {
	
	private PersistentCache cache;
	private CacheConfig config;
	
	public void init(FilterConfig filterConfig) throws ServletException {
		config = new CacheConfig(filterConfig);
		cache = new PersistentCache();
	}
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		//Verify that this is an HttpServletRequest, and ignore those which are not.
		if (!(req instanceof HttpServletRequest)){
			chain.doFilter(req, res);
			return;
		}
		
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		String uri = request.getRequestURL().toString();
		
		if (browserCacheIsCurrent(request, response, uri))
			return;
			
		if (serverCacheIsCurrent(request, response, uri))
			return;
		
		storeCacheOnServer(request, response, uri, chain);
	}
	
	private boolean browserCacheIsCurrent(HttpServletRequest request, HttpServletResponse response, String uri) throws IOException, ServletException {
		//Basically, think of this as a simple question: the browser asks "Hey, I just
		// saw this page last Friday!  Have you changed it since then?"  If the current
		// version of the object (cache date) is later than (greater than) the modified
		// since date, then we return false (meaning that, No, the browser cache is not
		// current).  If the cache date is earlier than the last time the browser saw it,
		// then we return 304 Not Modified.
		String ifModifiedSinceString = request.getHeader("If-Modified-Since");
		if (ifModifiedSinceString != null){
			try {
				Long modifiedSinceDate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z").parse(ifModifiedSinceString).getTime();
				Long cacheDate = cache.getCachedItemDate(uri, config);
				if (cacheDate == null)
					return false;
				
				if (cacheDate < modifiedSinceDate){
					//Return Status 304 Not Modified
					response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					response.getOutputStream().close();
					response.flushBuffer();
					System.out.println("304");
					return true;
				}
			}
			catch (ParseException pe){}
		}
		
		return false;
	}
	
	private boolean serverCacheIsCurrent(HttpServletRequest request, HttpServletResponse response, String uri) throws IOException, ServletException {
		if (cache.get(uri, config) != null){
			response.getOutputStream().write(cache.get(uri, config));

			Date expiryDate = new Date(cache.getCachedItemDate(uri, config) + config.getExpiryTimeSeconds(uri) * 1000);
			DateFormat httpDateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
			httpDateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
			response.addHeader("Expires", httpDateFormatter.format(expiryDate));
			response.addHeader("Cache-Control", "max-age=" + config.getExpiryTimeSeconds(uri));

			System.out.println("Returning Cached Version");
			return true;
		}
		
		return false;
	}
	
	private void storeCacheOnServer(HttpServletRequest request, HttpServletResponse response, String uri, FilterChain chain) throws IOException, ServletException {
		//If the object is not cached, we will load it, grab the bytes, and cache it, before returning.
		CachingServletResponseWrapper responseWrapper = new CachingServletResponseWrapper(response);
		Date expiryDate = new Date(System.currentTimeMillis() + (config.getExpiryTimeSeconds(uri) * 1000));
		DateFormat httpDateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		httpDateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		responseWrapper.addHeader("Expires", httpDateFormatter.format(expiryDate));
		responseWrapper.addHeader("Cache-Control", "max-age=" + config.getExpiryTimeSeconds(uri));
		
		//Continue on down the filter chain to get the actual content.
		chain.doFilter(request, responseWrapper);
		
		//Once the request / response has come through, save the data. 
		cache.put(uri, config, responseWrapper.getData());
		
		System.out.println("Storing data in server cache");
	}
	
	public void destroy() {
		config = null;
		cache = null;
	}
}