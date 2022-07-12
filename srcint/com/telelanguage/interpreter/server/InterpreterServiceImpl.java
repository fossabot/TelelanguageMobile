package com.telelanguage.interpreter.server;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.telelanguage.api.InterpreterInfo;
import com.telelanguage.interpreter.client.ConfigInfo;
import com.telelanguage.interpreter.client.InterpreterService;
import com.telelanguage.interpreter.service.TLVXManager;

@SuppressWarnings("serial")
public class InterpreterServiceImpl extends RemoteServiceServlet implements InterpreterService, ServletContextListener {
	private static final Logger LOG = Logger.getLogger(InterpreterServiceImpl.class);
	static private ThreadLocal<String> threadNames = new ThreadLocal<String>();
	
	public ConfigInfo logon(String sessionId, String email, String password) throws IllegalArgumentException {
		if (email != null) email = email.toLowerCase();
		LOG.info("logon email: "+email);
		String serverId = "Unknown";
		getThreadLocalRequest().getSession().removeAttribute("lastHeartbeat");
		getThreadLocalRequest().getSession().removeAttribute("lastHeartbeatAttempted");
		try {
			serverId = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
		}
		InterpreterInfo interpreter = TLVXManager.tlvxClient.findInterpreterByEmail(email);
		if (TLVXManager.interpreterManager.logon(interpreter, email, password, 
				getThreadLocalRequest().getSession().getId(), 
				getThreadLocalRequest().getRemoteAddr(), 
				getThreadLocalRequest().getRemoteHost(), 
				getThreadLocalRequest().getHeader("User-Agent"),
				getThreadLocalRequest().getHeader("Referer"),
				serverId)) {
			MessageWebSocket.addSessionIdEmail(sessionId, email);
		} else return null;
		ConfigInfo ci = new ConfigInfo();
		ci.webRtcUrl = TLVXManager.getProperties().getProperty("webrtcUrl");
		ci.wsUrl = TLVXManager.getProperties().getProperty("wsUrl");
		ci.sipRegistrationServer = TLVXManager.getProperties().getProperty("webPhoneSipAddressEnding");
		ci.turnServer = TLVXManager.getProperties().getProperty("turnServer");
		ci.turnUsername = TLVXManager.getProperties().getProperty("turnUsername");
		ci.turnPassword = TLVXManager.getProperties().getProperty("turnPassword");
		String allowVideo = TLVXManager.getProperties().getProperty("allowVideo");
		if (allowVideo == null || allowVideo.equals("true")) {
			ci.allowVideo = interpreter.allowVideo;
		} else ci.allowVideo = false;
		ci.videoOnly = interpreter.videoOnly;
		ci.videoEnabled = interpreter.videoEnabled;
		ci.forceVideoOnly = interpreter.forceVideoOnly;
		return ci;
	}
	
	@Override
	public void logout(String sessionId) {
		LOG.info("logout");
		TLVXManager.interpreterManager.logout(MessageWebSocket.getEmail(sessionId));
		MessageWebSocket.removeSessionId(sessionId);
	}

	@Override
	public void startTakingCalls(String sessionId) {
		LOG.info("startTakingCalls");
		TLVXManager.interpreterManager.changeInterpreterStatus(MessageWebSocket.getEmail(sessionId), true);
	}
	
	@Override
	public void stopTakingCalls(String sessionId) {
		LOG.info("stopTakingCalls");
		TLVXManager.interpreterManager.changeInterpreterStatus(MessageWebSocket.getEmail(sessionId), false);
	}

//	public static void sendAllMessage(String message) {
//		for(List<HttpSession> httpSessions : interpreterSessions.values())
//			for (HttpSession httpSession : httpSessions)
//				sendMessageToSession(httpSession, message);
//	}
	
//	@SuppressWarnings("unchecked")
//	private static void sendMessageToSession(String sessionId, HttpSession httpSession, String message) {
//		//System.out.println("sendMessage: in "+Thread.currentThread().getName());
//		synchronized(httpSession) {
//			try {
//				List<String> messages = (List<String>) httpSession.getAttribute("messages");
//				if (messages == null) {
//					messages = new ArrayList<String>();
//					httpSession.setAttribute("messages", messages);
//				}
//				messages.add(message);
//				httpSession.notifyAll();
//				//System.out.println("sendMessage: out "+Thread.currentThread().getName());
//			} catch (IllegalStateException e) {
//				LOG.error(e.getMessage(), e);
//			}
//		}
//	}
	
//	@SuppressWarnings("unchecked")
//	@Override
//	public List<String> getMessages() {
//		//System.out.println("getMessages: Waiting "+Thread.currentThread().getName()+" from "+getThreadLocalRequest().getRemoteAddr()+" session "+getThreadLocalRequest().getSession());
//		TLVXManager.cleanupSession();
//		synchronized(getThreadLocalRequest().getSession()) {
//			List<String> messages = (List<String>) getThreadLocalRequest().getSession().getAttribute("messages");
//			if(messages == null) try {
//				getThreadLocalRequest().getSession().wait(45000);
//				messages = (List<String>) getThreadLocalRequest().getSession().getAttribute("messages");
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			if (messages != null) {
//				getThreadLocalRequest().getSession().setAttribute("lastDelivery", new Date());
//				getThreadLocalRequest().getSession().removeAttribute("messages");
//			}
//			//System.out.println("getMessages: " + messages);
//			return messages;
//		}
//	}

//	public static void sendInterpreterMessage(InterpreterInfo interpreter, String message) {
//		//System.out.println("client message:  "+agent.getUser().getEmail()+": "+message);
//		List<HttpSession> httpSessions = interpreterSessions.get(interpreter.email);
//		// Make temp copy
//		if (httpSessions != null) 
//			for (HttpSession httpSession : httpSessions)
//				sendMessageToSession(httpSession, message);
//	}

//	@Override
//	public void sessionCreated(HttpSessionEvent arg0) {
//		arg0.getSession().setMaxInactiveInterval(43200);
//	}

//	@Override
//	public void sessionDestroyed(HttpSessionEvent session) {
//		//String email = (String) getThreadLocalRequest().getSession().getAttribute("email");
//		//TLVXManager.getAgentManager().logout(email);
//		if (session.getSession() != null && session.getSession().getAttribute("email")!=null) {
//			List<HttpSession> sessionsForEmail = interpreterSessions.get(session.getSession().getAttribute("email"));
//			if (sessionsForEmail != null) {
//				sessionsForEmail.remove(session.getSession());
//				if (sessionsForEmail.size() == 0) interpreterSessions.remove(session.getSession().getAttribute("email"));
//				synchronized(session.getSession()) {
//					session.getSession().notifyAll();
//				}
//			}
//		}
//	}

	@Override
	public boolean updateStatus(String sessionId) {
		LOG.info("updateStatus getEmail: "+MessageWebSocket.getEmail(sessionId));
		return TLVXManager.interpreterManager.updateStatus(MessageWebSocket.getEmail(sessionId));
	}

	@Override
	public void updateNumber(String sessionId, Boolean webrtc, String phoneNumber) {
		LOG.info("updateNumber phoneNumber: "+phoneNumber);
		TLVXManager.interpreterManager.handleInterpreterPhoneChange(MessageWebSocket.getEmail(sessionId), webrtc, phoneNumber);
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		TLVXManager.contextDestoryed();
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
	}

	@Override
	public String getRelease() {
		return (String) TLVXManager.getProperties().getProperty("release");
	}
	
	@Override
	protected void onBeforeRequestDeserialized(String serializedRequest) {
		super.onBeforeRequestDeserialized(serializedRequest);
		threadNames.set(Thread.currentThread().getName());
	}
	
	@Override
	protected void onAfterResponseSerialized(String serializedResponse) {
		super.onAfterResponseSerialized(serializedResponse);
		TLVXManager.cleanupSession();
		Thread.currentThread().setName(threadNames.get());
		threadNames.remove();
	}

	@Override
	public void setWebPhoneEnabled(String sessionId, boolean enabled) {
		TLVXManager.interpreterManager.setWebPhoneEnabled(MessageWebSocket.getEmail(sessionId), enabled);
	}

	@Override
	public void hangupRequest(String sessionId) {
		InterpreterInfo ii = MessageWebSocket.emailToInterpreterInfo.get(MessageWebSocket.getEmail(sessionId));
		if (ii != null) {
			TLVXManager.tlvxClient.hangupCall(ii);
		}
	}

	@Override
	public void requestAgent(String sessionId) {
		InterpreterInfo ii = MessageWebSocket.emailToInterpreterInfo.get(MessageWebSocket.getEmail(sessionId));
		if (ii != null) {
			TLVXManager.tlvxClient.requestAgent(ii);
		}
	}

	@Override
	public void rejectCall(String sessionId, String reason) {
		InterpreterInfo ii = MessageWebSocket.emailToInterpreterInfo.get(MessageWebSocket.getEmail(sessionId));
		ii.rejectReason = reason;
		if (ii != null) {
			TLVXManager.tlvxClient.rejectCall(ii);
		}
	}

	@Override
	public void acceptCall(String sessionId) {
		InterpreterInfo ii = MessageWebSocket.emailToInterpreterInfo.get(MessageWebSocket.getEmail(sessionId));
		if (ii != null) {
			TLVXManager.tlvxClient.acceptCall(ii);
		}
	}

	@Override
	public void acceptVideo(String sessionId, boolean video, boolean videoOnly) {
		String email = MessageWebSocket.getEmail(sessionId);
		InterpreterInfo ii = MessageWebSocket.emailToInterpreterInfo.get(email);
		if (ii == null) {
			ii = new InterpreterInfo();
			ii.email = email;
		}
		System.out.println("acceptVideo: "+sessionId+" "+email+" "+ii+" "+video);
		if (ii != null) {
			if (video) {
				if (videoOnly) {
					TLVXManager.tlvxClient.acceptVideoOnly(ii);
				} else {
					TLVXManager.tlvxClient.acceptVideo(ii);
				}
			} else {
				TLVXManager.tlvxClient.dontAcceptVideo(ii);
			}
		}
	}

	@Override
	public void playCustomerVideo(String sessionId, boolean play) {
		String email = MessageWebSocket.getEmail(sessionId);
		InterpreterInfo ii = MessageWebSocket.emailToInterpreterInfo.get(email);
		if (ii == null) {
			ii = new InterpreterInfo();
			ii.email = email;
		}
		System.out.println("playCustomerVideo: "+sessionId+" "+email+" "+ii+" "+play);
		if (ii != null) {
			if (play) {
				TLVXManager.tlvxClient.playCustomerVideo(ii);
			} else {
				TLVXManager.tlvxClient.pauseCustomerVideo(ii);
			}
		}
	}

	@Override
	public void videoSessionStarted(String sessionId) {
		String email = MessageWebSocket.getEmail(sessionId);
		InterpreterInfo ii = MessageWebSocket.emailToInterpreterInfo.get(email);
		if (ii == null) {
			ii = new InterpreterInfo();
			ii.email = email;
		}
		System.out.println("videoSessionStarted: "+sessionId+" "+email+" "+ii);
		if (ii != null) {
			TLVXManager.tlvxClient.videoSessionStarted(ii);
		}
	}

	@Override
	public void dialThirdParty(String sessionId, String phoneNumber) {
		String email = MessageWebSocket.getEmail(sessionId);
		LOG.info("dialThirdParty phoneNumber: "+phoneNumber+" "+email+" "+sessionId);
		InterpreterInfo ii = MessageWebSocket.emailToInterpreterInfo.get(email);
		if (ii == null) {
			ii = new InterpreterInfo();
			ii.email = email;
		}
		ii.phoneNumber = phoneNumber;
		TLVXManager.tlvxClient.dialThirdParty(ii);
	}

	@Override
	public void hangupThirdParty(String sessionId, String thirdPartyId) {
		String email = MessageWebSocket.getEmail(sessionId);
		LOG.info("hangupThirdParty id: "+thirdPartyId+" "+email+" "+sessionId);
		InterpreterInfo ii = MessageWebSocket.emailToInterpreterInfo.get(email);
		if (ii == null) {
			ii = new InterpreterInfo();
		}
		ii.thirdPartyId = thirdPartyId;
		TLVXManager.tlvxClient.hangupThirdParty(ii);
	}
}
