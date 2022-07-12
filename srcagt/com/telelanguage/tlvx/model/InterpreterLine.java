package com.telelanguage.tlvx.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public class InterpreterLine implements IsSerializable {
	private String id;
	private String name;
	private String phoneNumber;
	private String code;
	private boolean isAvailable;
	public InterpreterLine() {}
	public InterpreterLine(String id, String name, String phoneNumber, boolean inSession, String code) {
		this.id = id;
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.isAvailable = inSession;
		this.code = code;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public boolean getAvailable() {
		return isAvailable;
	}
	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
}
