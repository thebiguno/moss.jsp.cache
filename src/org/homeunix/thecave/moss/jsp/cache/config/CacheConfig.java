package org.homeunix.thecave.moss.jsp.cache.config;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
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

	private final Map<Pattern, Long> expiryTimes = new LinkedHashMap<Pattern, Long>();
	private final Map<Pattern, File> cacheFolders = new LinkedHashMap<Pattern, File>();
	
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
			expiryTimes.clear();
			cacheFolders.clear();
			defaultCacheFolder = new File(System.getProperty("java.io.tmpdir") + "/moss-jsp-cache");
			
			//The 'nodes' list now contains all the children of root ('cache')
			NodeList nodes = doc.getFirstChild().getChildNodes();
			
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeName().equals("cache-folder")){
					defaultCacheFolder = new File(node.getTextContent());
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
						}
						else if (cacheElementChild.getNodeName().equals("expiry-time")){
							if (pattern != null){
								long expiryTime = Long.parseLong(cacheElementChild.getTextContent());
								expiryTimes.put(pattern, expiryTime);
							}
						}
						else if (cacheElementChild.getNodeName().equals("cache-folder")){
							if (pattern != null){
								File cacheFolder = new File(cacheElementChild.getTextContent());
								cacheFolders.put(pattern, cacheFolder);
							}
						}
					}
				}
			}
			
			lastConfigRefresh = System.currentTimeMillis();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
	}

	public synchronized File getCacheFolder(String uri){
		loadConfig();
		
		for (Pattern pattern : cacheFolders.keySet()) {
			if (pattern.matcher(uri).matches()){
				return cacheFolders.get(pattern);
			}
		}
		
		return defaultCacheFolder;
	}
	
	public synchronized long getExpiryTimeSeconds(String uri){
		loadConfig();
		
		for (Pattern pattern : expiryTimes.keySet()) {
			if (pattern.matcher(uri).matches()){
				return expiryTimes.get(pattern);
			}
		}
		
		return 0;
	}
}
