package com.telelanguage.tlvx.ivr;

import java.io.Serializable;
import java.util.Map;

/**
 * Dialog
 */
public class Dialog implements Serializable
{
	private static final long serialVersionUID = -2221473323982692699L;
	private String dialogId;
	private Map parameters;
	private String connectionId;
	private boolean finished = false;
	private boolean started = false;

	public Dialog(Map parameters)
	{
		this.parameters = parameters;
	}
	
	public void setDialogId(String dialogId)
	{
		this.dialogId = dialogId;
	}
	
	public String getDialogId()
	{
		return dialogId;
	}
	
	public String getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}
	
	public Map getParameters()
	{
		return parameters;
	}
	
	public void setFinished(boolean val)
	{
		finished = val;
	}
	
	public boolean getFinished()
	{
		return finished;
	}
	
	public void setStarted(boolean started) {
		this.started = started;
	}

	public boolean getStarted() {
		return started;
	}
	
	@Override
	public String toString() {
		return "Dialog: "+dialogId+" "+parameters+" finished: "+finished+" started: "+started+" connectionid: "+connectionId;
	}
}
