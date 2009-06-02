package org.homeunix.thecave.moss.jsp.cache.config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("persistence-backing")
public class PersistenceBacking {

	@XStreamAsAttribute
	@XStreamAlias("class-name")
	private String className;
	
	private transient Map<String, String> parametersByName = new HashMap<String, String>();
	
	@XStreamImplicit
	private List<Parameter> parameters;
	
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public List<Parameter> getParameters() {
		return parameters;
	}
	public void addParameter(Parameter parameter) {
		this.parametersByName.put(parameter.getName(), parameter.getValue());
		this.parameters.add(parameter);
	}
	public String getParameter(String parameterName){
		return parametersByName.get(parameterName);
	}
	
	private Object readResolve() {
		parametersByName = new LinkedHashMap<String, String>();
		if (this.parameters != null){
			for (Parameter parameter : this.parameters) {
				parametersByName.put(parameter.getName(), parameter.getValue());
			}
		}
		
		return this;
	}
}
