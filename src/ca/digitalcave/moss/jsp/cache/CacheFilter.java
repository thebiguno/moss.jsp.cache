/*
 * Created on May 28, 2008 by wyatt
 */
package ca.digitalcave.moss.jsp.cache;

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

import ca.digitalcave.moss.common.LogUtil;
import ca.digitalcave.moss.jsp.cache.config.Config;
import ca.digitalcave.moss.jsp.cache.config.ConfigFactory;
import ca.digitalcave.moss.jsp.cache.persistence.CacheDelegate;
import ca.digitalcave.moss.jsp.cache.persistence.CachedResponse;

/**
 * A caching filter which works with the browser (via HTTP headers) to keep
 * network traffic down as much as possible.
 * 
 * Accepts the following init-params:
 *	config (where the cache.xml config file is located, relative to /WEB-INF/)
 * 
 * @author wyatt
 *
 */
public class CacheFilter implements Filter {
	
	private Config config;
	private final Logger logger = Logger.getLogger(CacheDelegate.class.getName());
	
	public void init(FilterConfig filterConfig) throws ServletException {
		config = ConfigFactory.loadConfig(filterConfig);
		LogUtil.setLogLevel(config.getLogLevel());
	}
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		//Verify that this is an HttpServletRequest, and ignore those which are not.
		if (!(req instanceof HttpServletRequest)){
			chain.doFilter(req, res);
			return;
		}
		
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		String url = request.getRequestURL().toString();
		
		//If this resource is not supposed to be cached, just pass it down the filter chain.
		if (!config.isMatched(url)){
			logger.finer("Could not find a match for '" + url + "' in any persistence backing; bypassing cache.");
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
				Long cacheDate = config.getCacheDelegate().getCacheDate(url, config);
				if (cacheDate != null){
					if (cacheDate < modifiedSinceDate){
						//Return Status 304 Not Modified
						response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
						response.getOutputStream().close();
						response.flushBuffer();
						logger.fine("Returning HTTP Status 304 for " + url + " (request If-Modified-Since header is '" + ifModifiedSinceString + "')");
						return;
					}
				}
			}
			catch (ParseException pe){}
		}
			
		//If there is a copy of this request in cache, we will return it. 
		CachedResponse cachedResponse = config.getCacheDelegate().get(url, config);
		if (cachedResponse != null && cachedResponse.getRequestData() != null){
			//The expiry headers should have already been set when the response was cached.
			for (String headerName : cachedResponse.getHeaders().keySet()) {
				response.addHeader(headerName, cachedResponse.getHeaders().get(headerName));				
			}
			response.setLocale(cachedResponse.getLocale());
			response.setContentType(cachedResponse.getContentType());
			response.getOutputStream().write(cachedResponse.getRequestData());
			logger.fine("Returning cached version for " + url + ".");
			return;
		}
		
		//If the object is not cached, we will load it, grab the bytes, and cache it, before returning.
		SplitStreamServletResponseWrapper splitStreamResponse = new SplitStreamServletResponseWrapper(response);
		
		//Set the expiry headers
		Date expiryDate = new Date(System.currentTimeMillis() + (config.getExpiryTime(url) * 1000));
		DateFormat httpDateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		httpDateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		splitStreamResponse.addHeader("Expires", httpDateFormatter.format(expiryDate));
		splitStreamResponse.addHeader("Expires", httpDateFormatter.format(expiryDate));
		splitStreamResponse.addHeader("Cache-Control", "max-age=" + config.getExpiryTime(url));
		
		logger.fine("Caching response for " + url + "; expires in " + config.getExpiryTime(url) + " seconds.");
		
		//Continue on down the filter chain to get the actual content.
		chain.doFilter(request, splitStreamResponse);
		
		//Once the request / response has come through, save the data if it is status 200. 
		if (splitStreamResponse.getStatus() == HttpServletResponse.SC_OK){ 
			if (splitStreamResponse.getData() != null && splitStreamResponse.getData().length > 0){
				CachedResponse returnedResponse = new CachedResponse();
				returnedResponse.setUrl(url);
				returnedResponse.setCachedDate(System.currentTimeMillis());
				returnedResponse.setRequestData(splitStreamResponse.getData());
				returnedResponse.addHeaders(splitStreamResponse.getHeaders());
				returnedResponse.setContentType(splitStreamResponse.getContentType());
				returnedResponse.setLocale(splitStreamResponse.getLocale());

				config.getCacheDelegate().put(url, config, returnedResponse);
			}
			else {
				logger.finer("Response data is empty for '" + url + "'; not storing in cache.");
			}
		}
		else {
			logger.finer("Response status is '" + splitStreamResponse.getStatus() + "' for '" + url + "'; not storing in cache");
		}
	}
		
	public void destroy() {
		config = null;
	}
}