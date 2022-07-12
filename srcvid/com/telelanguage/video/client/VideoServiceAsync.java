package com.telelanguage.video.client;

import java.util.Date;
import java.util.List;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface VideoServiceAsync {
	void login(String sessionId, String email, String password, AsyncCallback<Void> callback);
	void getCustomerInfoByToken(String sessionId, Boolean isVideo, AsyncCallback<CustomerInfo> callback);
	void call(String sessionId, Integer room, String accessCode, String language, String gender, boolean requireVideo, List<String> questionsInput, String deptCode, AsyncCallback<Void> callback);
	void getUserInfoByToken(String sessionId, String loginToken, AsyncCallback<UserInfo> asyncCallback);
	void getUserInfoByEmailPassword(String sessionId, String username, String password, boolean rememberme, AsyncCallback<UserInfo> asyncCallback);
	void logout(String sessionId, AsyncCallback<Void> asyncCallback);
	void hangup(String sessionId, String callSessionId, AsyncCallback<Void> asyncCallback);
	void ping(AsyncCallback<Void> callback);
	void videoSessionStarted(String sessionId, String callSessionId, AsyncCallback<Void> asyncCallback);
	void vriCredSubmit(String email, String name, String phone, String org, String custString,
			AsyncCallback<Void> asyncCallback);
	void schedIntSubmit(String userInfoEmail, String userInfoCode, String email, String name, String phone, String org, String languageString,
			String typeString, String datetime, Date dateSubmitted, Integer timezone, AsyncCallback<Void> asyncCallback);
	void getBluestreamJWT(AsyncCallback<String> callback);
	void blueStreamConnected(String sessionId, String callSessionId, String bsCallId, String bsInterpreterId, String bsInterpreterName,
			AsyncCallback<Void> asyncCallback);
	void blueStreamDisconnected(String sessionId, String callSessionId, long callDuration, AsyncCallback<Void> asyncCallback);
}
