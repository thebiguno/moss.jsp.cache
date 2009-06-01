package org.homeunix.thecave.moss.jsp.cache.config;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.FilterConfig;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CacheConfig {

	private final FilterConfig filterConfig;

	private long refreshConfig; //in millis
	private long lastConfigRefresh = 0l;
	private File defaultCacheFolder;
	private int memoryCacheItemCapacity = 0;

	private final Map<Pattern, ConfigElement> cacheElements = new LinkedHashMap<Pattern, ConfigElement>();
	private final Set<String> purgedHeaders = new HashSet<String>();

	private final Logger logger = Logger.getLogger(this.getClass().getName());

	public CacheConfig(FilterConfig filterConfig) {
		this.filterConfig = filterConfig;
	}

	private synchronized void loadConfig(){
		if (lastConfigRefresh + refreshConfig > System.currentTimeMillis())
			return;

		try {
			String config = filterConfig.getInitParameter("config");
			if (config == null || config.length() == 0)
				config = "cache.xml";
			InputStream is = filterConfig.getServletContext().getResourceAsStream("/WEB-INF/" + config);
			if (is == null){
				logger.warning("No config file '/WEB-INF/" + config + "' found!  Cache filter will not be configured.");
			}
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = sf.newSchema(new StreamSource(this.getClass().getResourceAsStream("cache.xsd")));
			dbf.setSchema(schema);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(is);
			doc.getDocumentElement().normalize();

			try {
				refreshConfig = Long.parseLong(doc.getFirstChild().getAttributes().getNamedItem("refresh-config").getNodeValue()) * 1000;
			}
			catch (NumberFormatException nfe){
				refreshConfig = 60 * 1000;
			}

			//Clear out the last cache elements
			memoryCacheItemCapacity = 0;
			cacheElements.clear();
			purgedHeaders.clear();
			defaultCacheFolder = new File(System.getProperty("java.io.tmpdir") + "/org.homeunix.thecave.moss.jsp.cache");

			//The 'nodes' list now contains all the children of root ('cache')
			NodeList nodes = doc.getFirstChild().getChildNodes();

			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeName().equals("cache-folder")){
					defaultCacheFolder = new File(node.getTextContent());
				}
				else if (node.getNodeName().equals("purge-header")){
					purgedHeaders.add(node.getTextContent());
				}
				else if (node.getNodeName().equals("memory-item-capacity")){
					try {
						this.memoryCacheItemCapacity = Integer.parseInt(node.getTextContent());
					}
					catch (NumberFormatException nfe){
						logger.log(Level.CONFIG, "Error parsing memory cache item capacity", nfe);
					}
				}
				else if (node.getNodeName().equals("cache-element")){
					NodeList cacheElement = node.getChildNodes();
					Pattern pattern = null;
					for (int j = 0; j < cacheElement.getLength(); j++){
						//The schema declares pattern to be required, and must appear 
						// before expiry-time and cache-folder; we just check that it 
						// is set before saving other values.
						Node cacheElementChild = cacheElement.item(j);
						if (cacheElementChild.getNodeName().equals("pattern")){
							pattern = Pattern.compile(cacheElementChild.getTextContent(), Pattern.CASE_INSENSITIVE);
							cacheElements.put(pattern, new ConfigElement());
						}
						else if (cacheElementChild.getNodeName().equals("expiry-time")){
							if (cacheElements.get(pattern) != null){
								long expiryTime = Long.parseLong(cacheElementChild.getTextContent());
								cacheElements.get(pattern).setExpiryTimeSeconds(expiryTime);
							}
							else {
								logger.config("There was a problem reading the XML file - within a cache-element, pattern must be specified before expiry-time.");
							}
						}
						else if (cacheElementChild.getNodeName().equals("cache-folder")){
							if (cacheElements.get(pattern) != null){
								cacheElements.get(pattern).setCacheFolder(cacheElementChild.getTextContent());
							}
							else {
								logger.config("There was a problem reading the XML file - within a cache-element, pattern must be specified before cache-folder.");
							}
						}
					}
				}
			}

			lastConfigRefresh = System.currentTimeMillis();
		}
		catch (Exception e){
			logger.log(Level.WARNING, "There was an error reading the cache config file", e);
		}

	}

	/**
	 * Does the given URI match at least one pattern in the config file?
	 * @param uri
	 * @return
	 */
	public boolean isConfigMatchUri(String uri){
		loadConfig();

		for (Pattern pattern : cacheElements.keySet()) {
			if (pattern.matcher(uri).matches()){
				return true;
			}
		}

		return false;
	}

	/**
	 * Get the cache folder for the given URI.  This will be the cache-folder element
	 * of the first matching URI (if it exists), or the cache-folder element of the
	 * root cache folder, or (if all else fails) the default cache folder.
	 * @param uri
	 * @return
	 */
	public synchronized File getCacheFolder(String uri){
		loadConfig();

		for (Pattern pattern : cacheElements.keySet()) {
			if (pattern.matcher(uri).matches()){
				if (cacheElements.get(pattern).getCacheFolder() != null)
					return new File(cacheElements.get(pattern).getCacheFolder());
				else
					return defaultCacheFolder;
			}
		}

		return defaultCacheFolder;
	}

	/**
	 * Returns the expiry time of the given URI.  This will be the expiry time of
	 * the first matching URI found when scanning the XML, or null if there are no
	 * matches.
	 * @param uri
	 * @return
	 */
	public synchronized Long getExpiryTimeSeconds(String uri){
		loadConfig();

		for (Pattern pattern : cacheElements.keySet()) {
			if (pattern.matcher(uri).matches()){
				return cacheElements.get(pattern).getExpiryTimeSeconds();
			}
		}

		return null;
	}

	/**
	 * Should the given header be included in the response (if possible)?  Any headers
	 * which have not been specified as a 'purge-header' are allowed. 
	 * @param headerName
	 * @return
	 */
	public synchronized boolean isHeaderAllowed(String headerName){
		loadConfig();

		if (purgedHeaders.contains(headerName))
			return false;

		return true;
	}
	
	public int getMemoryCacheItemCapacity() {
		return memoryCacheItemCapacity;
	}
}
