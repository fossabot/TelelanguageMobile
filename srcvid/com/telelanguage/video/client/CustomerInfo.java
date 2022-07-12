package com.telelanguage.video.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CustomerInfo implements IsSerializable {
	public String name;
	public String email;
	public String sipAddress;
	public String sipProxy;
	public String janusApiUrl;
	public Integer room;
	public String loginToken;
	public String wsUrl;
	public String turnServer;
	public String turnUsername;
	public String turnPassword;
}
