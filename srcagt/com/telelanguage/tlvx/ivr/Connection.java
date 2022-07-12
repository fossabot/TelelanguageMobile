package com.telelanguage.tlvx.ivr;

import java.io.Serializable;

/**
 * Connection
 */
public class Connection implements Serializable
{
	private static final long serialVersionUID = 2942001302629660459L;

	public enum ConnectionState {CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED};

	private String connectionId;
	private String destination;
	private String origination;
	private ConnectionState state;
	private boolean conferenced;
	public int wrongErrorStates = 0;
	private boolean video = false;
	
	public Connection(String connectionId, String destination, String origination)
	{
		this.connectionId = connectionId;
		this.destination = destination;
		this.origination = origination;
	}

	public String getConnectionId() 
	{
		return connectionId;
	}

	public void setConnectionId(String connectionId) 
	{
		this.connectionId = connectionId;
	}

	public String getDestination() 
	{
		return destination;
	}

	public void setDestination(String destination) 
	{
		this.destination = destination;
	}

	public String getOrigination() 
	{
		return origination;
	}

	public void setOrigination(String origination) 
	{
		this.origination = origination;
	}
	
	public ConnectionState getState()
	{
		return state;
	}
	
	public void setState(ConnectionState state)
	{
		this.state = state;
	}

	public boolean isConferenced() 
	{
		return conferenced;
	}

	public void setConferenced(boolean conferenced) 
	{
		this.conferenced = conferenced;
	}
	
	public boolean isMuted()
	{
		return !conferenced;
	}

	@Override
	public String toString() {
		return "connection "+connectionId+": "+origination+" -> "+destination+": "+state+" conferenced: "+conferenced+" wrong state errors: "+wrongErrorStates+" video: "+video;
	}
	
	public boolean isVideo() {
		return video;
	}

	public void setVideo(boolean b) {
		video = b;
	}
}
