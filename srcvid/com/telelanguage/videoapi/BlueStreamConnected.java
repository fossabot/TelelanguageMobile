package com.telelanguage.videoapi;

public class BlueStreamConnected {
	public String sessionId;
	public String callSessionId;
	public String bsCallId;
	public String bsInterpreterId;
	public String bsInterpreterName;
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "BlueStreamConnected: sessionId: "+sessionId+" callSessionId: "+callSessionId+" bsCallId: "+bsCallId+" bsInterpreterId: "+bsInterpreterId+" bsInterpreterName: "+bsInterpreterName;
	}
}
