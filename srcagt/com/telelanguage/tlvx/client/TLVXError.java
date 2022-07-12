package com.telelanguage.tlvx.client;

public class TLVXError extends RuntimeException {
	private static final long serialVersionUID = -8142828913571027438L;
	Boolean forceLogout;
	public TLVXError(String message, Boolean forceLogout) {
		super(message);
		this.forceLogout = forceLogout;
	}
}
