package com.telelanguage.video.server;

import java.util.Date;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.telelanguage.video.client.CustomerInfo;
import com.telelanguage.video.client.UserInfo;
import com.telelanguage.video.client.VideoService;
import com.telelanguage.video.service.BlueStreamService;
import com.telelanguage.video.service.TLVXManager;

@SuppressWarnings("serial")
public class VideoServiceImpl extends RemoteServiceServlet implements VideoService {
	
	public static String getClientIpAddr(HttpServletRequest request) {  
        String ip = request.getHeader("X-Forwarded-For");  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("Proxy-Client-IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("WL-Proxy-Client-IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("HTTP_CLIENT_IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getRemoteAddr();  
        }  
        return ip;  
    }
	
	@Override
	public CustomerInfo getCustomerInfoByToken(String sessionId, Boolean isVideo) {
		CustomerInfo info = TLVXManager.balancerService.getCustomerInfoByToken(sessionId, isVideo, getClientIpAddr(getThreadLocalRequest()));
		return info;
	}
	
	private String getTokenValue() {
		String token = null;
		if (getThreadLocalRequest().getCookies() != null)
		for (Cookie cookie: getThreadLocalRequest().getCookies()) {
			if (cookie.getName().equals("tlvxvideo.token")) {
				token = cookie.getValue();
			}
		}
		if (token == null) {
			token = TLVXManager.grabNewUuid();
			Cookie oauthCookie = new Cookie("tlvxvideo.token", token);
			oauthCookie.setPath("/");
			oauthCookie.setDomain(".telelanguage.com");
			oauthCookie.setMaxAge(1209600);
			getThreadLocalResponse().addCookie(oauthCookie);
		}
		return token;
	}

	@Override
	public void login(String sessionId, String email, String password) throws IllegalArgumentException {
		
	}

	@Override
	public void call(String sessionId, Integer room, String accessCode, String language, String gender, boolean requireVideo, List<String> questionInputs, String deptCode) {
		TLVXManager.videoCustomerManager.requestCall(sessionId, room, accessCode, language, gender, requireVideo, questionInputs, deptCode);
	}

	@Override
	public UserInfo getUserInfoByToken(String sessionId, String loginToken) {
		String token = getTokenValue();
		UserInfo userInfo = TLVXManager.videoCustomerManager.getUserInfoByToken(sessionId, token);
		if (userInfo != null && userInfo.email != null) {
			MessageWebSocket.addSessionIdEmail(sessionId, userInfo.email);
			System.out.println("getUserInfoByToken: "+userInfo);
		} else {
			System.out.println("getUserInfoByToken: email not found: "+userInfo);
		}
		return userInfo;
	}

	@Override
	public UserInfo getUserInfoByEmailPassword(String sessionId, String email, String password, boolean rememberme) {
		UserInfo userInfo = TLVXManager.videoCustomerManager.getUserInfoByEmailPassword(sessionId, email, password, rememberme, getTokenValue());
		if (userInfo != null && userInfo.email != null) {
			MessageWebSocket.addSessionIdEmail(sessionId, userInfo.email);
			System.out.println("getUserInfoByToken: "+userInfo);
		} else {
			System.out.println("getUserInfoByToken: email not found: "+userInfo);
		}
		return userInfo;
	}

	@Override
	public void logout(String sessionId) {
		TLVXManager.videoCustomerManager.logoutToken(getTokenValue());
	}

	@Override
	public void hangup(String sessionId, String callSessionId) {
		TLVXManager.videoCustomerManager.hangupCall(sessionId, callSessionId, getTokenValue());
	}

	@Override
	public void ping() {
	}

	@Override
	public void videoSessionStarted(String sessionId, String callSessionId) {
		TLVXManager.videoCustomerManager.videoSessionStarted(sessionId, callSessionId);
	}

	@Override
	public void vriCredSubmit(String email, String name, String phone, String org,
			String custString) {
		System.out.println("email: "+email);
		System.out.println("name: "+name);
		System.out.println("phone: "+phone);
		System.out.println("org: "+org);
		System.out.println("custString: "+custString);
		TLVXManager.videoCustomerManager.requestCredentials(email, name, phone, org, custString);
	}

	@Override
	public void schedIntSubmit(String userInfoEmail, String userInfoCode, String email, String name, String phone, String org,
			String languageString, String typeString, String datetime, Date dateSumbitted, Integer timezone) {
		System.out.println("User Info Code: "+userInfoCode);
		System.out.println("User Info Email: "+userInfoEmail);
		System.out.println("email: "+email);
		System.out.println("name: "+name);
		System.out.println("phone: "+phone);
		System.out.println("org: "+org);
		System.out.println("languageString: "+languageString);
		System.out.println("typeString: "+typeString);
		System.out.println("datetime: "+datetime);
		System.out.println("date submitted: "+dateSumbitted.toLocaleString());
		System.out.println("timezone: "+timezone);
		TLVXManager.videoCustomerManager.scheduleInterpreter(userInfoCode, userInfoEmail, email, name, phone, org, languageString,
				typeString, datetime, dateSumbitted, timezone);
	}

	@Override
	public String getBluestreamJWT() {
		return BlueStreamService.getBluestreamJWT();
	}

	@Override
	public void blueStreamConnected(String sessionId, String callSessionId, String bsCallId, String bsInterpreterId,
			String bsInterpreterName) {
		System.out.println("\nblueStreamConnected: time:"+new Date().getTime()+" sessionId:"+sessionId+" callSessionId:"+callSessionId+" bsCallId:"+bsCallId+" bsInterpreterId:"+bsInterpreterId+" bsInterpreterName:"+bsInterpreterName);
		TLVXManager.videoCustomerManager.blueStreamConnected(sessionId, callSessionId, bsCallId, bsInterpreterId, bsInterpreterName);
	}

	@Override
	public void blueStreamDisconnected(String sessionId, String callSessionId, long callDuration) {
		System.out.println("\nblueStreamDisconnected: time:"+new Date().getTime()+" sessionId:"+sessionId+" callSessionId:"+callSessionId+" callDuration:"+callDuration);
	}
}
