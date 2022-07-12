package com.telelanguage.videoapi;

public class CredentialRequest {
	
	public String email;
	public String name;
	public String phone;
	public String org;
	public String custString;

	@Override
	public String toString() {
		return "CredentialRequest: "+email+" "+name+" "+phone+" "+org+" "+custString;
	}
}
