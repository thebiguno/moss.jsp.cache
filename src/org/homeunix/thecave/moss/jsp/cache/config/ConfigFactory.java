package org.homeunix.thecave.moss.jsp.cache.config;

import java.io.InputStream;
import java.util.logging.Logger;

import javax.servlet.FilterConfig;

import org.homeunix.thecave.moss.jsp.cache.persistence.impl.DiskPersistence;
import org.homeunix.thecave.moss.jsp.cache.persistence.impl.MemoryPersistence;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ConfigFactory {
	
	private final static Logger logger = Logger.getLogger(ConfigFactory.class.getName());

	public static Config loadConfig(FilterConfig filterConfig) {
		XStream xstream = getXStream();
		
		String config = filterConfig.getInitParameter("config");
		if (config == null || config.length() == 0)
			config = "cache.xml";
		InputStream is = filterConfig.getServletContext().getResourceAsStream("/WEB-INF/" + config);
		if (is == null){
			logger.config("No config file '/WEB-INF/" + config + "' found!  Config filter will not be configured.");
			return null;
		}
		
		Object o = xstream.fromXML(is);
		if (o instanceof Config)
			return (Config) o;
		
		logger.config("Could not load config file; resulting object of type " + o.getClass().getName());
		
		return null;
	}
	
	public static void main(String[] args) {
		Config config = new Config();
		
		PersistenceBacking mem = new PersistenceBacking();
		mem.setClassName(MemoryPersistence.class.getName());
		mem.addParameter(new Parameter(MemoryPersistence.ITEM_COUNT, "100"));
		config.addPersistenceBacking(mem);
		
		PersistenceBacking disk = new PersistenceBacking();
		disk.setClassName(DiskPersistence.class.getName());
		disk.addParameter(new Parameter(DiskPersistence.CACHE_PATH, "/tmp/foo/bar"));
		config.addPersistenceBacking(disk);
		
		CacheMapping map1 = new CacheMapping();
		map1.setPattern(".*\\.jsp");
		map1.setExpiryTime(60);
		config.addCacheMapping(map1);
		
		config.addHeaderBlacklist("X-Header-1");
		config.addHeaderBlacklist("X-Header-2");		
		
		XStream xstream = getXStream();
		xstream.toXML(config, System.out);
	}
	
	private static XStream getXStream(){
		XStream xstream = new XStream(new DomDriver());
		xstream.processAnnotations(Config.class);
		
//		xstream.addImplicitCollection(PersistenceBacking.class, "parameters");
//		xstream.addImplicitCollection(Config.class, "cacheBackings");
//		xstream.addImplicitCollection(Config.class, "cacheElements");
		
		return xstream;
	}
}
