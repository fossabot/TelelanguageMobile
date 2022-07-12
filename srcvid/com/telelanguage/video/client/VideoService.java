package com.telelanguage.video.client;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("video")
public interface VideoService extends RemoteService {
	CustomerInfo getCustomerInfoByToken(String sessionId, Boolean isVideo) throws RuntimeException;
	void login(String sessionId, String email, String password);
	void call(String sessionId, Integer room, String accessCode,
			String language, String gender, boolean requireVideo,
			List<String> questionsInput, String deptCode);
	UserInfo getUserInfoByToken(String sessionId, String loginToken) throws RuntimeException;
	UserInfo getUserInfoByEmailPassword(String sessionId, String username,
			String password, boolean rememberme) throws RuntimeException;
	void logout(String sessionId);
	void hangup(String sessionId, String callSessionId);
	void ping();
	void videoSessionStarted(String sessionId, String callSessionId);
	void vriCredSubmit(String email, String name, String phone, String org, String custString) throws RuntimeException;
	void schedIntSubmit(String userInfoEmail, String userInfoCode, String email, String name, String phone, String org,
			String languageString, String typeString, String datetime, Date dateSubmitted, Integer timezone) throws RuntimeException;
	String getBluestreamJWT();
	void blueStreamConnected(String sessionId, String callSessionId, String bsCallId, String bsInterpreterId, String bsInterpreterName);
	void blueStreamDisconnected(String sessionId, String callSessionId, long callDuration);
}
