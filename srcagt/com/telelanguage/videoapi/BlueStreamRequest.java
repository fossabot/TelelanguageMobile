package com.telelanguage.videoapi;

public class BlueStreamRequest {
	
	public String event;
	public String customerId;
	public String customerName;
	public String language;

	@Override
	public String toString() {
		return "BlueStreamRequest: "+event+" "+customerId+" "+customerName+" "+language;
	}
}
