package ca.digitalcave.moss.jsp.cache.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("parameter")
public class Parameter {
	@XStreamAsAttribute
	private String name;
	@XStreamAsAttribute
	private String value;
	
	public Parameter() {}
	
	public Parameter(String name, String value) {
		this.setName(name);
		this.setValue(value);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
