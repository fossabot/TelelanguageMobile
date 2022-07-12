package com.telelanguage.interpreter.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface InterpreterServiceAsync {
	//void getMessages(String sessionId, AsyncCallback<List<String>> callback) throws TLVXError;
	void getRelease(AsyncCallback<String> callback) throws TLVXError;
	void logon(String sessionId, String name, String password,
			AsyncCallback<ConfigInfo> callback);
	void logout(String sessionId, AsyncCallback<Void> callback);
	void startTakingCalls(String sessionId, AsyncCallback<Void> callback) throws TLVXError;
	void stopTakingCalls(String sessionId, AsyncCallback<Void> callback);
	void updateStatus(String sessionId, AsyncCallback<Boolean> asyncCallback);
	void updateNumber(String sessionId, Boolean webrtc, String phoneNumber, AsyncCallback<Void> callback) throws TLVXError;
	void setWebPhoneEnabled(String sessionId, boolean enabled, AsyncCallback<Void> asyncCallback);
	void hangupRequest(String sessionId, AsyncCallback<Void> asyncCallback);
	void requestAgent(String sessionId, AsyncCallback<Void> asyncCallback);
	void rejectCall(String sessionId, String reason, AsyncCallback<Void> asyncCallback);
	void acceptCall(String sessionId, AsyncCallback<Void> asyncCallback);
	void acceptVideo(String sessionId, boolean video, boolean videoOnly, AsyncCallback<Void> asyncCallback);
	void playCustomerVideo(String sessionId, boolean play, AsyncCallback<Void> callback);
	void videoSessionStarted(String sessionId, AsyncCallback<Void> asyncCallback);
	void dialThirdParty(String sessionId, String value, AsyncCallback<Void> asyncCallback);
	void hangupThirdParty(String sessionId, String thirdPartyId, AsyncCallback<Void> asyncCallback);
}
