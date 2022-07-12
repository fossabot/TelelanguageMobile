package com.telelanguage.tlvx.ivr;

import java.io.Serializable;

/**
 * Conference
 */
public class Conference implements Serializable
{
	private static final long serialVersionUID = 2987387661924536614L;
	private String conferenceId;
	
	public Conference(String conferenceId)
	{
		this.conferenceId = conferenceId;
	}
	
	public String getConferenceId()
	{
		return conferenceId;
	}
}