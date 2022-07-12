package com.telelanguage.interpreter.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("interpreter")
public interface InterpreterService extends RemoteService {
	boolean updateStatus(String sessionId);
	ConfigInfo logon(String sessionId, String name, String password);
	void logout(String sessionId);
	void startTakingCalls(String sessionId);
	void stopTakingCalls(String sessionId);
	String getRelease();
	//List<String> getMessages(String sessionId);
	void updateNumber(String sessionId, Boolean webrtc, String phoneNumber);
	void setWebPhoneEnabled(String sessionId, boolean enabled);
	void hangupRequest(String sessionId);
	void requestAgent(String sessionId);
	void rejectCall(String sessionId, String reason);
	void acceptCall(String sessionId);
	void acceptVideo(String sessionId, boolean video, boolean videoOnly);
	void playCustomerVideo(String sessionId, boolean play);
	void videoSessionStarted(String sessionId);
	void dialThirdParty(String sessionId, String value);
	void hangupThirdParty(String sessionId, String thirdPartyId);
}
