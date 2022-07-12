package com.telelanguage.video.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.telelanguage.video.client.UserInfo;
import com.telelanguage.video.server.MessageWebSocket;
import com.telelanguage.videoapi.BlueStreamConnected;
import com.telelanguage.videoapi.CredentialRequest;
import com.telelanguage.videoapi.CustomerHangupRequest;
import com.telelanguage.videoapi.CustomerLoginRequest;
import com.telelanguage.videoapi.CustomerLoginResponse;
import com.telelanguage.videoapi.CustomerLogoutRequest;
import com.telelanguage.videoapi.CustomerVideoEstablished;
import com.telelanguage.videoapi.InterpreterRequest;
import com.telelanguage.videoapi.ScheduleInterpreterRequest;
import com.telelanguage.videoapi.TLVXCustomerAPIClient;
import com.telelanguage.videoapi.VideoCustomerInfo;

public class VideoCustomerManager
{
    private static final Logger LOG = Logger.getLogger(VideoCustomerManager.class);
    
	public Map<String, VideoCustomerInfo> sessionIdToVideoCustomerInfo = new ConcurrentHashMap<String, VideoCustomerInfo>();
	
	private static Map<String, TLVXCustomerAPIClient> videoAPIClients = new HashMap<String, TLVXCustomerAPIClient>();
	
	synchronized public TLVXCustomerAPIClient getTLVXCustomerAPIClient(String url) {
		if (videoAPIClients.containsKey(url)) return videoAPIClients.get(url);
		TLVXCustomerAPIClient newClient = new TLVXCustomerAPIClient(url);
		LOG.info("TLVXCustomerAPIClient created for "+url);
		videoAPIClients.put(url, newClient);
		return newClient;
	}
    
	public void dispatchInterpreterMessage(VideoCustomerInfo videoCustomerInfo, Map<String, String> data, String type) {
		//InterpreterServiceImpl.sendInterpreterMessage(interpreter, type+":"+data.toString());
		MessageWebSocket.sendMessage(videoCustomerInfo.sessionId, type+":"+data.toString());
	}
	
	public void logout(String sessionId) {
    	if (LOG.isDebugEnabled()) LOG.debug("interpreter "+sessionId+" log-off request");
    	if (sessionId == null) return;
    	//VideoCustomerInfo videoCustomerInfo = TLVXManager.tlvxClient.findVideoCustomerByEmail(emailAddress);
        //if (null != videoCustomerInfo)
        //{
        //	videoCustomerInfo.onWebPage = null;
        //	TLVXManager.tlvxClient.saveVideoCustomer(videoCustomerInfo);
        //}
        //dispatchInterpreterMessage(videoCustomerInfo, getVideoCustomerInfo(videoCustomerInfo), "status");
        //MessageWebSocket.sendMessageToEmail(videoCustomerInfo.email, "Logout:");
	}

	public void requestCall(String sessionId, Integer room, String accessCode, String language, String gender, boolean requireVideo, List<String> questionInputs, String deptCode) {
		LOG.info("requestCall "+sessionId);
		InterpreterRequest request = new InterpreterRequest();
		VideoCustomerInfo vci = new VideoCustomerInfo();
		request.videoCustomerInfo = vci;
		request.videoCustomerInfo.sessionId = sessionId;
		request.videoCustomerInfo.email = MessageWebSocket.getEmail(sessionId);
		request.videoCustomerInfo.webPhoneSipAddress = TLVXManager.getProperties().getProperty("sipPrefix")+sessionId+"@"+TLVXManager.getProperties().getProperty("webPhoneSipAddressEnding");
		request.videoCustomerInfo.videoServer = TLVXManager.getProperties().getProperty("thisApiUrl"); //"http://v18.icoa.com:8018/api/";
		request.videoCustomerInfo.roomNumber = ""+room;
		request.videoCustomerInfo.janusServer = TLVXManager.getProperties().getProperty("webrtcUrl");
		request.videoCustomerInfo.accessCode = accessCode;
		request.videoCustomerInfo.language = language;
		request.videoCustomerInfo.interpreterGender = gender;
		request.videoCustomerInfo.requireVideo = requireVideo;
		request.videoCustomerInfo.questionInputs = questionInputs;
		request.videoCustomerInfo.departmentCode = deptCode;
		getTLVXCustomerAPIClient(TLVXManager.getProperties().getProperty("tlvxApiUrl")).requestInterpreter(request);
	}

	public UserInfo getUserInfoByToken(String sessionId, String loginToken) {
		LOG.info("getUserInfoByToken "+sessionId+" "+loginToken);
		CustomerLoginRequest request = new CustomerLoginRequest();
		request.loginToken = loginToken;
		CustomerLoginResponse response = getTLVXCustomerAPIClient(TLVXManager.getProperties().getProperty("tlvxApiUrl")).loginCustomer(request);
		UserInfo userInfo = new UserInfo();
		if (response == null ) return userInfo;
		userInfo.marketingInfo = response.marketingText;
		userInfo.forgotPasswordLink = response.forgotPasswordLink;
		userInfo.alreadyCustomerLink = response.alreadyCustomerLink;
		userInfo.openAnAccountLink = response.openAnAccountLink;
		userInfo.preScheduleInterpreterLink = response.preScheduleInterpreterLink;
		if (response.email == null) return userInfo;
		userInfo.email = response.email;
		userInfo.accessCode = response.accessCode;
		userInfo.deptCode = response.deptCode;
		userInfo.deptLabel = response.deptLabel;
		userInfo.deptQuestion = response.deptQuestion;
		userInfo.questionId = response.questionId;
		userInfo.questionLabel = response.questionLabel;
		userInfo.questionPlaceholder = response.questionPlaceholder;
		return userInfo;
	}

	public UserInfo getUserInfoByEmailPassword(String sessionId, String email, String password, boolean rememberme, String token) {
		LOG.info("getUserInfoByUsernamePassword "+sessionId+" "+email);
		CustomerLoginRequest request = new CustomerLoginRequest();
		request.email = email;
		request.password = password;
		if (rememberme) request.loginToken = token;
		CustomerLoginResponse response = getTLVXCustomerAPIClient(TLVXManager.getProperties().getProperty("tlvxApiUrl")).loginCustomer(request);
		if (response == null || response.email == null) throw new RuntimeException("Login Failed, please contact Telelanguage.");
		UserInfo userInfo = new UserInfo();
		userInfo.email = response.email;
		userInfo.accessCode = response.accessCode;
		userInfo.deptCode = response.deptCode;
		userInfo.deptLabel = response.deptLabel;
		userInfo.deptQuestion = response.deptQuestion;
		userInfo.questionId = response.questionId;
		userInfo.questionLabel = response.questionLabel;
		userInfo.questionPlaceholder = response.questionPlaceholder;
		userInfo.marketingInfo = response.marketingText;
		userInfo.forgotPasswordLink = response.forgotPasswordLink;
		userInfo.alreadyCustomerLink = response.alreadyCustomerLink;
		userInfo.openAnAccountLink = response.openAnAccountLink;
		userInfo.preScheduleInterpreterLink = response.preScheduleInterpreterLink;
		return userInfo;
	}

	public void logoutToken(String tokenValue) {
		CustomerLogoutRequest request = new CustomerLogoutRequest();
		request.loginToken = tokenValue;
		getTLVXCustomerAPIClient(TLVXManager.getProperties().getProperty("tlvxApiUrl")).logoutCustomer(request);
	}

	public void hangupCall(String sessionId, String callSessionId, String tokenValue) {
		CustomerHangupRequest request = new CustomerHangupRequest();
		request.sessionId = sessionId;
		request.callSessionId = callSessionId;
		request.loginToken = tokenValue;
		getTLVXCustomerAPIClient(TLVXManager.getProperties().getProperty("tlvxApiUrl")).hangupCustomer(request);
	}

	public void videoSessionStarted(String sessionId, String callSessionId) {
		CustomerVideoEstablished request = new CustomerVideoEstablished();
		request.sessionId = sessionId;
		request.callSessionId = callSessionId;
		getTLVXCustomerAPIClient(TLVXManager.getProperties().getProperty("tlvxApiUrl")).videoSessionStarted(request);
	}

	public void requestCredentials(String email, String name, String phone, String org, String custString) {
		LOG.info("requestCredentials ");
		CredentialRequest request = new CredentialRequest();
		request.email = email;
		request.name = name;
		request.phone = phone;
		request.org = org;
		request.custString = custString;
		getTLVXCustomerAPIClient(TLVXManager.getProperties().getProperty("tlvxApiUrl")).requestCredentials(request);
	}

	public void scheduleInterpreter(String userInfoCode, String userInfoEmail, String email, String name, 
			String phone,
			String org, String languageString, String typeString, String datetime, Date dateSumbitted,
			Integer timezone) {
		LOG.info("scheduleInterpreter ");
		ScheduleInterpreterRequest request = new ScheduleInterpreterRequest();
		request.accessCode = userInfoCode;
		request.videoEmail = userInfoEmail;
		request.email = email;
		request.name = name;
		request.phone = phone;
		request.org = org;
		request.languageString = languageString;
		request.typeString = typeString;
		request.date = datetime;
		request.dateSubmitted = dateSumbitted;
		request.timezone = timezone;
		getTLVXCustomerAPIClient(TLVXManager.getProperties().getProperty("tlvxApiUrl")).scheduleInterpreter(request);

	}

	public void blueStreamConnected(String sessionId, String callSessionId, String bsCallId, String bsInterpreterId,
			String bsInterpreterName) {
		LOG.info("blueStreamConnected "+sessionId+" "+bsCallId+" "+bsInterpreterId+" "+bsInterpreterName);
		BlueStreamConnected bsc = new BlueStreamConnected();
		bsc.sessionId = sessionId;
		bsc.callSessionId = callSessionId;
		bsc.bsCallId = bsCallId;
		bsc.bsInterpreterId = bsInterpreterId;
		bsc.bsInterpreterName = bsInterpreterName;
		getTLVXCustomerAPIClient(TLVXManager.getProperties().getProperty("tlvxApiUrl")).bluestreamConnected(bsc);
	}
}