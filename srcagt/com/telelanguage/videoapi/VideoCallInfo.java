package com.telelanguage.videoapi;

public class VideoCallInfo {
	public VideoCustomerInfo interpreterInfo;
	public boolean agentOnCall;
	public Boolean onHold;
	public String language;
	public boolean interpreterValidated;
	public boolean interpreterBrowserSound;
	public boolean interpreterOnCall;
	public boolean thirdPartyOnCall;
	public String callSessionId;
	public Boolean isVideo;
	public Long callId;
	public String customerId;
	public String customerName;
	public String event;
	
	@Override
	public String toString() {
		return "VideoCallInfo: "+interpreterInfo+" agentOnCall: "+agentOnCall+" onHold: "+onHold+" language: "+language+" callSessionId: "+callSessionId+" isVideo: "+isVideo+" callId: "+callId+ " customerId: " + customerId + " customerName: " + customerName + " event: "+ event;
	}
}
