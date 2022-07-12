package com.telelanguage.tlvx.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.telelanguage.tlvx.client.AgentService;
import com.telelanguage.tlvx.client.ClientVersion;
import com.telelanguage.tlvx.model.Agent;
import com.telelanguage.tlvx.model.CallInformation;
import com.telelanguage.tlvx.model.Department;
import com.telelanguage.tlvx.model.InterpreterLine;
import com.telelanguage.tlvx.model.Language;
import com.telelanguage.tlvx.model.QuestionItem;
import com.telelanguage.tlvx.service.TLVXManager;

@SuppressWarnings("serial")
public class AgentServiceImpl extends RemoteServiceServlet implements AgentService, HttpSessionListener, ServletContextListener {
	private static final Logger LOG = Logger.getLogger(AgentServiceImpl.class);
	static private ThreadLocal<String> threadNames = new ThreadLocal<String>();
	
	static public Map<String, List<String>> sessionIdToMessageList = new ConcurrentHashMap<String, List<String>>();
	static public Map<String, List<String>> emailToSessionId = new ConcurrentHashMap<String, List<String>>();
	static public Map<String, String> sessionIdToEmail = new ConcurrentHashMap<String, String>();
	static public Map<String, Date> lastDelivery = new HashMap<String, Date>();
	static public Map<String, Boolean> pingCheck = new HashMap<String, Boolean>();

	static private Timer connectionValidationTimer = new Timer(true);
	static int mb = 1024*1024;
	static {
		connectionValidationTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					Runtime runtime = Runtime.getRuntime();
					System.out.println("\nActive sessions LP: "+sessionIdToMessageList.size()+" Java Threads: "+Thread.activeCount()+" Used Memory: "+((runtime.totalMemory() - runtime.freeMemory()) / mb)+" Free Memory: "+(runtime.freeMemory() / mb)+"\n");
					Date now = new Date();
					Map<String, List<String>> feedbackSessions = new HashMap<String, List<String>>();
					feedbackSessions.putAll(sessionIdToMessageList);
					for(String sessionId : feedbackSessions.keySet()) {
						//if (!MessageWebSocket.sessionIdToSocketMapping.containsKey(sessionId)) {
							try {
								Date lastHeartbeat = lastDelivery.get(sessionId);
								List<String> messages = feedbackSessions.get(sessionId);
								if (messages != null && lastHeartbeat != null) {
									pingCheck.remove(sessionId);
									long diff = now.getTime() - lastHeartbeat.getTime();
									if (diff > 60000) 
										clearMessagesForSessionId(sessionId);
								} else if (lastHeartbeat == null) {
									if (pingCheck.containsKey(sessionId)) {
										clearMessagesForSessionId(sessionId);
									}
									lastDelivery.put(sessionId, new Date());
								}
							}
							catch (IllegalStateException sessionalreadyInvalidated) {
								clearMessagesForSessionId(sessionId);
							}
						//}
					}
					sendAllMessage("ping:");
					//thisServer.setLastPing(new Date());
					//OggFeedbackManager.oggFlowService.saveServer(thisServer);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
				}
			}
//			@Override
//			public void run() {
//				try {
//					Runtime runtime = Runtime.getRuntime();
//					System.out.println("Active sessions: "+sessionIdMessages.size()+" Java Threads: "+Thread.activeCount()+" Used Memory: "+((runtime.totalMemory() - runtime.freeMemory()) / mb)+" Free Memory: "+(runtime.freeMemory() / mb) + " Call Sessions: "+TLVXManager.callSessionManager.sessions.size());
//					Date now = new Date();
//					List<String> agentSessions = new ArrayList<String>();
//					agentSessions.addAll(sessionIdMessages.keySet());
//					for(String httpSessionId : agentSessions) {
//						List<String> tempHttpSessionId = new ArrayList<String>();
//						tempHttpSessionId.addAll(agentSessions);
//						for (String httpSession : agentSessions) {
//							try {
//								String email = (String) httpSession.getAttribute("email");
//								//System.out.println("HTTP Session: "+email+" "+httpSession);
//								Date lastHeartbeat = (Date) httpSession.getAttribute("lastDelivery");
//								@SuppressWarnings("unchecked")
//								List<String> messages = (List<String>) httpSession.getAttribute("messages");
//								if (messages != null && lastHeartbeat != null) {
//									long diff = now.getTime() - lastHeartbeat.getTime();
//									if (diff > 65000) {
//										tempHttpSession.remove(httpSession);
//										if (tempHttpSession.size() == 0) {
//											TLVXManager.agentManager.logout(email);
//											AgentServiceImpl.agentSessions.remove(email);
//										} else
//											AgentServiceImpl.agentSessions.put(email, tempHttpSession);
//									}
//								} else if (lastHeartbeat == null) {
//									String pingCheck = (String) httpSession.getAttribute("pingCheck");
//									if (pingCheck != null) {
//										tempHttpSession.remove(httpSession);
//										if (tempHttpSession.size() == 0) {
//											TLVXManager.agentManager.logout(email);
//											AgentServiceImpl.agentSessions.remove(email);
//										} else
//											AgentServiceImpl.agentSessions.put(email, tempHttpSession);
//									}
//									httpSession.setAttribute("pingCheck","check");
//								}
//							} catch (IllegalStateException e) {
//								if (e.getMessage() != null && e.getMessage().contains("Session already invalidated")) {
//									try {
//										LOG.warn("Attempting to remove agent due to invalid session.");
//										httpSessions.remove(httpSession);
//									} catch (Exception ee) {
//										LOG.error(ee);
//										ee.printStackTrace();
//									}
//								}
//								LOG.error(e);
//							}
//						}
//					}
//					sendAllMessage("ping:");
//				} catch (Exception e) {
//					e.printStackTrace();
//				} finally {
//					TLVXManager.cleanupSession();
//				}
//			}
		}, 15000, 15000);
	}
	
	public static void addSessionIdEmail(String sessionId, String email) {
		sessionIdToEmail.put(sessionId, email);
		sessionIdToMessageList.put(sessionId, new ArrayList<String>());
		List<String> sessionIds = emailToSessionId.get(email);
		if (sessionIds == null) sessionIds = new ArrayList<String>();
		sessionIds.add(sessionId);
		emailToSessionId.put(email, sessionIds);
	}

	public static String getEmail(String sessionId) {
		return sessionIdToEmail.get(sessionId);
	}

	public static void removeSessionId(String sessionId) {
		String email = sessionIdToEmail.get(sessionId);
		if (email != null) {
			List<String> sessionIds = emailToSessionId.get(email);
			if (sessionIds != null) {
				sessionIds.remove(sessionId);
				if (sessionIds.size() == 0) {
					emailToSessionId.remove(email);
				}
			}
		}
		sessionIdToEmail.remove(sessionId);
	}
	
	public static void clearMessagesForSessionId(String sessionId) {
		//TLVXManager.interpreterManager.logout(MessageWebSocket.getEmail(sessionId));
		String email = sessionIdToEmail.get(sessionId);
		sessionIdToMessageList.remove(sessionId);
		lastDelivery.remove(sessionId);
		if (email != null && emailToSessionId.containsKey(email)) {
			List<String> sessions = emailToSessionId.get(email);
			sessions.remove(sessionId);
			if (sessions.size() == 0) {
				emailToSessionId.remove(email);
				//TLVXManager.interpreterManager.logout(email);
			}
		}
		System.out.println("clearMessagesForSessionId "+sessionId);
	}
	
	@Override
	public Integer logon(String sessionId, String email, String password) throws IllegalArgumentException {
		LOG.info("logon email: "+email);
		String serverId = "Unknown";
//		getThreadLocalRequest().getSession().removeAttribute("lastHeartbeat");
//		getThreadLocalRequest().getSession().removeAttribute("lastHeartbeatAttempted");
		try {
			serverId = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
		}
		if (TLVXManager.agentManager.logon(email, password, 
				getThreadLocalRequest().getSession().getId(), 
				getThreadLocalRequest().getRemoteAddr(), 
				getThreadLocalRequest().getRemoteHost(), 
				getThreadLocalRequest().getHeader("User-Agent"),
				getThreadLocalRequest().getHeader("Referer"),
				serverId)) {
			addSessionIdEmail(sessionId, email);
		} else return 0;

		return ClientVersion.version;  //return email ID?
	}
	
//	private String getEmail() {
//		System.out.println("SessionId: "+getThreadLocalRequest().getSession().getId()+" "+" email: "+getThreadLocalRequest().getSession().getAttribute("email"));
//		return (String) getThreadLocalRequest().getSession().getAttribute("email");
//	}
	
	@Override
	public void logout(String sessionId) {
		LOG.info("logout");
		if (null == getEmail(sessionId)) return;
		TLVXManager.agentManager.logout(getEmail(sessionId));
		clearMessagesForSessionId(sessionId);
	}

	@Override
	public void startTakingCalls(String sessionId) {
		LOG.info("startTakingCalls");
		TLVXManager.agentManager.changeAgentStatus(getEmail(sessionId), true);
	}
	
	@Override
	public void stopTakingCalls(String sessionId) {
		LOG.info("stopTakingCalls");
		TLVXManager.agentManager.changeAgentStatus(getEmail(sessionId), false);
	}

	public static void sendAllMessage(String message) {
		for(String sessionId : sessionIdToMessageList.keySet()) {
			sendMessage(sessionId, message);
		}
	}
	
	private static List<String> getMessagesForSessionId(String sessionId) {
		List<String> messages = sessionIdToMessageList.get(sessionId);
		if (messages == null) {
			messages = new ArrayList<String>();
			sessionIdToMessageList.put(sessionId, messages);
			//updateSessionInfo(sessionId, null);
		}
		return messages;
	}
	
	public static void sendMessage(String sessionId, String message) {
		//System.out.println("sendMessage: in "+Thread.currentThread().getName());
//		if (MessageWebSocket.sessionIdToSocketMapping.containsKey(sessionId)) {
//			sendWSMessage(sessionId, message);
//		} else {
			List<String> messages = getMessagesForSessionId(sessionId);
			synchronized(messages) {
				messages.add(message);
				messages.notifyAll();
				//System.out.println("sendMessage: out "+Thread.currentThread().getName());
			}
//		}
	}

	public static void sendAgentMessage(Agent agent, String message) {
		if (agent == null) return;
		sendMessageToEmail(agent.getUser().getEmail(), message);
	}
	
	public static void sendMessageToEmail(String email, String message) {
		List<String> sessionIds = emailToSessionId.get(email);
		if (sessionIds != null) {
			for(String sessionId : sessionIds) {
				sendMessage(sessionId, message);
			}
		}
	}

	@Override
	public void sessionCreated(HttpSessionEvent arg0) {
		arg0.getSession().setMaxInactiveInterval(43200);
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent session) {
		//String email = (String) getThreadLocalRequest().getSession().getAttribute("email");
		//TLVXManager.getAgentManager().logout(email);
//		if (session.getSession() != null && session.getSession().getAttribute("email")!=null) {
//			List<HttpSession> sessionsForEmail = agentSessions.get(session.getSession().getAttribute("email"));
//			if (sessionsForEmail != null) {
//				sessionsForEmail.remove(session.getSession());
//				if (sessionsForEmail.size() == 0) agentSessions.remove(session.getSession().getAttribute("email"));
//				synchronized(session.getSession()) {
//					session.getSession().notifyAll();
//				}
//			}
//		}
	}

	@Override
	public void callSessionCustomerConnect(String sessionId, String callSessionId, String dest) {
		LOG.info("callSessionCustomerConnect callSessionId: "+callSessionId+", dest: "+dest);
		TLVXManager.callSessionManager.onCallSessionCustomerConnect(callSessionId, dest);
	}

	@Override
	public void callerOnHold(String sessionId, String callId, Boolean completeCall) {
		LOG.info("callSessionCustomerConnect callId: "+callId+", completeCall: "+completeCall);
		TLVXManager.callSessionManager.onCallSessionCallerHoldOn(callId);
	}
	
	@Override
	public void interpreterOnHold(String sessionId, String callId) {
		LOG.info("interpreterOnHold callId: "+callId);
		TLVXManager.callSessionManager.onCallSessionInterpreterHoldOn(callId);
	}

	@Override
	public void thirdPartyOnHold(String sessionId, String callId, String connectionId) {
		LOG.info("thirdPartyOnHold callId: "+callId+" connectionId: "+connectionId);
		TLVXManager.callSessionManager.onCallSessionThirdpartyHoldOn(connectionId, callId);
	}


	@Override
	public void callerOffHold(String sessionId, String callId) {
		LOG.info("thirdPartyOnHold callId: "+callId);
		TLVXManager.callSessionManager.onCallSessionCallerHoldOff(callId);
	}
	

	@Override
	public void interpreterOffHold(String sessionId, String callId) {
		LOG.info("thirdPartyOnHold callId: "+callId);
		TLVXManager.callSessionManager.onCallSessionInterpreterHoldOff(callId);
	}

	@Override
	public void thirdPartyOffHold(String sessionId, String callId, String connectionId) {
		LOG.info("thirdPartyOnHold callId: "+callId);
		TLVXManager.callSessionManager.onCallSessionThirdpartyHoldOff(connectionId, callId);
	}

	@Override
	public void updateStatus(String sessionId) {
		LOG.info("updateStatus getEmail: "+getEmail(sessionId));
		TLVXManager.agentManager.sendAgentStatusUpdate(getEmail(sessionId));
	}

	@Override
	public List<Language> getLanguages() {
		return TLVXManager.languageManager.findLanguages();
	}

	@Override
	public List<QuestionItem> getAdditionalQuestions(String sessionId, String callId, String customerId) {
		LOG.info("getAdditionalQuestions callId: "+callId +", customerId: "+customerId);
		return TLVXManager.companyManager.getAdditionalQuestions(callId, customerId);
	}

	@Override
	public QuestionItem serializeQuestionItem(String sessionId, QuestionItem item) {
		return null;
	}

	@Override
	public void transfer(String sessionId, String callId, String dest) {
		LOG.info("transfer callId: "+callId+", dest: "+dest);
		TLVXManager.callSessionManager.onCallSessionTransferCall(callId, dest);
	}

	@Override
	public void updateNumber(String sessionId, String phoneNumber) {
		LOG.info("updateNumber phoneNumber: "+phoneNumber);
		TLVXManager.agentManager.handleAgentPhoneChange(getEmail(sessionId), phoneNumber);
	}

	@Override
	public void dialThirdParty(String sessionId, String callId, String dest) {
		LOG.info("dialThirdParty callId: "+callId+", dest: "+dest);
		TLVXManager.callSessionManager.onCallSessionThirdpartyConnect(callId, dest);
	}
	
	@Override
	public List<InterpreterLine> getInterpreters(String sessionId, String callId, String language, String interpreterGender, Boolean video) {
		LOG.info("getInterpreters callId: "+callId+", language: "+language+", interpreterGender: "+interpreterGender+", video: "+ video);
		return TLVXManager.callSessionManager.getInterpreters(callId, language, interpreterGender, video);
	}

	@Override
	public List<InterpreterLine> getInterpreterHistory(String sessionId, String callId, String language) {
		LOG.info("getInterpreterHistory callId: "+callId+", language: "+language);
		return TLVXManager.callSessionManager.getInterpreterHistory(callId, language);
	}

	@Override
	public InterpreterLine serializeInterpreterLine(String sessionId, InterpreterLine item) {
		return null;
	}

	@Override
	public void callInterpreter(String sessionId, String callId, String language, String interpreterId) {
		LOG.info("callInterpreter callId: "+callId+", interpreterId: "+interpreterId+", language: "+language);
		TLVXManager.interpreterManager.handleInterpreterConnect(callId, language, null, null, interpreterId, true, false);
	}
	
	@Override
	public Boolean callLanguage(String sessionId, String callId, String language, String interpreterGender, Boolean interpreterVideo) {
		LOG.info("callLanguage callId: "+callId+", language: "+language+", interpreterGender: "+interpreterGender+", interpreterVideo: "+interpreterVideo);
		return TLVXManager.interpreterManager.handleInterpreterConnect(callId, language, interpreterGender, interpreterVideo, null, false, false);
	}

	@Override
	public void hangupThirdParty(String sessionId, String callId, String connectionId) {
		LOG.info("hangupThirdParty callId: "+callId+" id: "+connectionId);
		TLVXManager.callSessionManager.onCallSessionThirdpartyDisconnect(connectionId, callId);
	}

	@Override
	public void hangupInterpreter(String sessionId, String callId) {
		LOG.info("hangupInterpreter callId: "+callId);
		TLVXManager.callSessionManager.onCallSessionInterpreterDisconnect(callId);
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
	public Department serializeDepartment(Department d) {
		return null;
	}

	@Override
	public CallInformation getCallInformationByCallId(String sessionId, String callId) {
		LOG.info("getCallInformationByCallId callId: "+callId);
		return TLVXManager.callSessionManager.getCallInformationByCallId(callId);
	}

	@Override
	public CallInformation getCallInformationByCallIdAccessCode(String sessionId, String callId, String accessCode) {
		LOG.info("getCallInformationByCallIdAccessCode callId: "+callId);
		return TLVXManager.callSessionManager.onCallSessionAccessCode(callId, accessCode);
	}

	@Override
	public Boolean completeCall(String sessionId, CallInformation callInformation, Boolean hangupCaller) {
		LOG.info("completeCall callInformation: "+callInformation+", hangupCaller: "+hangupCaller);
		TLVXManager.callSessionManager.saveCallInformation(callInformation);
		return TLVXManager.callSessionManager.onCallSessionCompleteCall(callInformation.callId, hangupCaller, callInformation.language);
	}

	@Override
	public void requeueRequest(String sessionId, CallInformation callInformation) {
		LOG.info("requeueRequest callInformation: "+callInformation);
		TLVXManager.callSessionManager.saveCallInformation(callInformation);
		TLVXManager.agentManager.doAgentRequeueRequest(getEmail(sessionId), callInformation.callId);
	}

	@Override
	public void personalHoldRequest(String sessionId, CallInformation callInformation) {
		LOG.info("personalHoldRequest callInformation: "+callInformation);
		TLVXManager.callSessionManager.saveCallInformation(callInformation);
		TLVXManager.agentManager.handleAgentPersonalHoldRequest(getEmail(sessionId), callInformation.callId);
	}
	
	@Override
	public void saveCallInformation(String sessionId, CallInformation callInformation) {
		LOG.info("saveCallInformation callInformation: "+callInformation);
		TLVXManager.callSessionManager.saveCallInformation(callInformation);
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
	public List<String> getMessages(String sessionId) {
		//System.out.println("getMessages: Waiting "+Thread.currentThread().getName());
		lastDelivery.put(sessionId, new Date());
		List<String> messages = getMessagesForSessionId(sessionId);
		if (messages == null) {
			return new ArrayList<String>();
		}
		synchronized(messages) {
			try {
				if(messages.size() == 0) {
					messages.wait(45000);
				}
				messages = sessionIdToMessageList.get(sessionId);
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
			lastDelivery.put(sessionId, new Date());
			//System.out.println("getMessages: notified "+Thread.currentThread().getName() + messages);
			List<String> returnMessages = new ArrayList<String>();
			if (messages != null) {
				returnMessages.addAll(messages);
				messages.clear();
			}
			return returnMessages;
		}
	}
}
