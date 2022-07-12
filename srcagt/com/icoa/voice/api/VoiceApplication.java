package com.icoa.voice.api;

import java.util.Map;
import java.util.Properties;

public abstract class VoiceApplication {
	private static VoiceService voiceService;
	
	// call events
	abstract public void incomingCall(Call call);
	abstract public void outgoingCall(Call call);
	abstract public void connectedCall(Call call);
	abstract public void disconnectedCall(Call call);
	public void init() {}
	
	abstract public void exitedDialog(Call call);
	public Call createSipCall(Map<String, String> params) { return voiceService.createSipCall(params); }
	public void setVoiceService(VoiceService voiceService) { VoiceApplication.voiceService = voiceService; }
	abstract public void conferenceUnJoined(Call call);
	abstract public void sessionDestroyed(Call call);
	abstract public void conferenceDestroyed(Call call);
	abstract public void conferenceUnjoined(Call call);
	abstract public Properties getAppProps();
	abstract public void createdConference(Call call);
	abstract public void dialogStarted(Call call);
	abstract public void dialogExit(Call call);
	abstract public void conferenceJoined(Call call);
	abstract public void conferenceErrorJoin(Call call);
	abstract public void callConnectionFailed(Call call);
	abstract public void cleanupSession();
	abstract public void callConnectionErrorWrongstate(Call call);
	abstract public void errorSemantic(Call call);
	abstract public void errorDialogNotStarted(Call call);
	abstract public void ccxmlLoaded(Call call);
}