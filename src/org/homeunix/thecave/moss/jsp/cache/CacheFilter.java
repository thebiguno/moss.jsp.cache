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
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.homeunix.thecave.moss.jsp.cache.config.CacheConfig;
import org.homeunix.thecave.moss.jsp.cache.persistence.PersistentCache;

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
	
	private final Logger logger = Logger.getLogger(PersistentCache.class.toString());
	
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
		
		//If this resource is not supposed to be cached, just pass it down the filter chain.
		if (!config.isConfigMatchUri(uri)){
			chain.doFilter(req, res);
			return;
		}
		
		//Basically, think of this as a simple question: the browser asks "Hey, I just
		// saw this page last Friday!  Have you changed it since then?"  If the current
		// version of the object (cacheDate) is later (greater) than the modifiedSinceDate, 
		// then we skip this (meaning that, No, the browser cache is not
		// current).  If the cache date is earlier than the last time the browser saw it,
		// then we mark the response as status 304 Not Modified, and return.
		String ifModifiedSinceString = request.getHeader("If-Modified-Since");
		if (ifModifiedSinceString != null){
			try {
				Long modifiedSinceDate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z").parse(ifModifiedSinceString).getTime();
				Long cacheDate = cache.getCachedItemDate(uri, config);
				if (cacheDate != null){
					if (cacheDate < modifiedSinceDate){
						//Return Status 304 Not Modified
						response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
						response.getOutputStream().close();
						response.flushBuffer();
						logger.fine("Returning HTTP Status 304 for " + uri + " (request If-Modified-Since header is '" + ifModifiedSinceString + "'");
						return;
					}
				}
			}
			catch (ParseException pe){}
		}
			
		if (cache.get(uri, config) != null){
			response.getOutputStream().write(cache.get(uri, config));

			Date expiryDate = new Date(cache.getCachedItemDate(uri, config) + config.getExpiryTimeSeconds(uri) * 1000);
			addHeaders(response, uri, expiryDate);
			
			logger.fine("Returning cached version for " + uri + ".");
			return;
		}
		
		//If the object is not cached, we will load it, grab the bytes, and cache it, before returning.
		SplitStreamServletResponseWrapper splitStreamResponse = new SplitStreamServletResponseWrapper(response);
		Date expiryDate = new Date(System.currentTimeMillis() + (config.getExpiryTimeSeconds(uri) * 1000));
		addHeaders(splitStreamResponse, uri, expiryDate);
		
		//Continue on down the filter chain to get the actual content.
		chain.doFilter(request, splitStreamResponse);
		
		//Once the request / response has come through, save the data. 
		cache.put(uri, config, splitStreamResponse.getData());
	}
	
	private void addHeaders(HttpServletResponse response, String uri, Date expiryDate){
		DateFormat httpDateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		httpDateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		addHeaderIfAllowed(response, "Expires", httpDateFormatter.format(expiryDate));
		addHeaderIfAllowed(response, "Cache-Control", "max-age=" + config.getExpiryTimeSeconds(uri));		
	}
	
	private void addHeaderIfAllowed(HttpServletResponse response, String headerName, String headerValue){
		if (config.isHeaderAllowed(headerName))
			response.addHeader(headerName, headerValue);
	}
	
	public void destroy() {
		config = null;
		cache = null;
	}
}