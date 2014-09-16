package com.thed.model.api;

import java.util.List;

/**
*
*/
public class Resource {
	private String name ;
	private String groupNotes ;
	private String path ;
	private String produces ;
	private String consumes ;
	private List<Operation> operations ;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getGroupNotes() {
		return groupNotes;
	}
	public void setGroupNotes(String groupNotes) {
		this.groupNotes = groupNotes;
	}
	public List<Operation> getOperations() {
		return operations;
	}
	public void setOperations(List<Operation> operations) {
		this.operations = operations;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getProduces() {
		return produces;
	}
	public void setProduces(String produces) {
		this.produces = produces;
	}
	public String getConsumes() {
		return consumes;
	}
	public void setConsumes(String consumes) {
		this.consumes = consumes;
	}
	
}