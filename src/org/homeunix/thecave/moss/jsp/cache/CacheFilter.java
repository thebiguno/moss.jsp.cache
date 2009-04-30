/*
 * Created on May 28, 2008 by wyatt
 */
package org.homeunix.thecave.moss.jsp.cache;

import java.io.IOException;
import java.util.HashMap;
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
	private FilterConfig filterConfig; 
	
	public void init(FilterConfig config) throws ServletException {
		this.filterConfig = config;
	}
	
	private final static Map<String, byte[]> cache = new HashMap<String, byte[]>();
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {		
		String uri = ((HttpServletRequest) req).getRequestURI();
		if (cache.get(uri) != null){
			res.getOutputStream().write(cache.get(uri));
			return;
		}
		
		ByteServletResponseWrapper responseWrapper = new ByteServletResponseWrapper((HttpServletResponse) res);
		
		chain.doFilter(req, responseWrapper);
		
		byte[] data = responseWrapper.getResponseBytes();
		cache.put(uri, data);
		res.getOutputStream().write(data);
	}
	
	public void destroy() {
		filterConfig = null;
	}
}
