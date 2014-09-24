package com.thed.model.api;

public class QueryParameter {
	private String name ;
	private String type ;
	private String description ;
	private String isRequired;
	private String urlName;
	
	public String getUrlName() {
		return urlName;
	}
	public void setUrlName(String urlName) {
		this.urlName = urlName;
	}
	public String getIsRequired() {
		return isRequired;
	}
	public void setIsRequired(String isRequired) {
		this.isRequired = isRequired;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
