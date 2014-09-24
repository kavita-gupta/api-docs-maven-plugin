package com.thed.model.api;

import java.util.List;

public class Operation {
	private String name ;
	private String summary ;
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	private String description ;
	private String path ;
	/** POST, GET, etc */
	private String requestType ;
	
	// e.g. "application/json". If multiple values, exist, provide a final value to be put.
	private String consumes ;
	
	/** Line by line text of request json */
	private List<String> jsonRequest ;

	// e.g. "application/json". If multiple values, exist, provide a final value to be put.
	private String produces ;
	
	/** Line by line text of response json */
	private List<String> jsonResponse ;
	/** e.g. 200 */
	private String responseCode ;
	
	private List<QueryParameter> queryParams ;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getRequestType() {
		return requestType;
	}
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}
	public List<String> getJsonRequest() {
		return jsonRequest;
	}
	public void setJsonRequest(List<String> jsonRequest) {
		this.jsonRequest = jsonRequest;
	}
	public List<String> getJsonResponse() {
		return jsonResponse;
	}
	public void setJsonResponse(List<String> jsonResponse) {
		this.jsonResponse = jsonResponse;
	}
	public String getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	public String getConsumes() {
		return consumes;
	}
	public void setConsumes(String consumes) {
		this.consumes = consumes;
	}
	public String getProduces() {
		return produces;
	}
	public void setProduces(String produces) {
		this.produces = produces;
	}
	public List<QueryParameter> getQueryParams() {
		return queryParams;
	}
	public void setQueryParams(List<QueryParameter> queryParams) {
		this.queryParams = queryParams;
	}
	
}

