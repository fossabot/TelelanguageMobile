package com.telelanguage.interpreter.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ConfigInfo implements IsSerializable {
	public String webRtcUrl;
	public String wsUrl;
	public String sipRegistrationServer;
	public String turnServer;
	public String turnUsername;
	public String turnPassword;
	public boolean allowVideo;
	public boolean videoEnabled;
	public boolean videoOnly;
	public boolean forceVideoOnly;
}
