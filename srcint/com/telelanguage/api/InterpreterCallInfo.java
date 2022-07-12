package com.telelanguage.api;

import java.util.List;

public class InterpreterCallInfo {
	public InterpreterInfo interpreterInfo;
	public boolean agentOnCall;
	public Boolean onHold;
	public String language;
	public boolean interpreterValidated;
	public boolean interpreterBrowserSound;
	public Long callId;
	public String videoJanusServer;
	public String roomNumber;
	public String missedCalls;
	public List<String> thirdPartyNumber;
	public List<String> thirdPartyId;
	public String instructionsWav;
	public String instructionsText;
	public boolean dialingThirdParty;
	
	@Override
	public String toString() {
		return ""+language+" "+onHold+" "+agentOnCall+" "+interpreterValidated+" "+interpreterInfo+" "+interpreterBrowserSound + " callId: "+callId+" videoJanusServer: "+videoJanusServer+" roomNumber: "+roomNumber+" missedCalls: "+missedCalls+" thirdPartyNumber: "+thirdPartyNumber+" thirdPartyId: "+thirdPartyId + " instructionsWav: "+instructionsWav+" instructionsText: "+instructionsText;
	}
}
