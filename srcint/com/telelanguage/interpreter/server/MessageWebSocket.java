package com.telelanguage.interpreter.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.eclipse.jetty.websocket.WebSocketServlet;

import com.telelanguage.api.InterpreterInfo;
import com.telelanguage.interpreter.client.InterpreterMessages;
import com.telelanguage.interpreter.service.TLVXManager;

public class MessageWebSocket extends WebSocketServlet {

	private static final long serialVersionUID = 1L;
	public static final Map<String, WidgetSession> sessionIdToSocketMapping = new ConcurrentHashMap<String, WidgetSession>();
	public static final Map<WidgetSession, String> socketToSessionIdMapping = new ConcurrentHashMap<WidgetSession, String>();
	static public Map<String, List<String>> sessionIdToMessageList = new ConcurrentHashMap<String, List<String>>();
	static public Map<String, List<String>> emailToSessionId = new ConcurrentHashMap<String, List<String>>();
	static public Map<String, String> sessionIdToEmail = new ConcurrentHashMap<String, String>();
	static public Map<String, Date> lastDelivery = new HashMap<String, Date>();
	static public Map<String, Boolean> pingCheck = new HashMap<String, Boolean>();
	static public Map<String, InterpreterInfo> emailToInterpreterInfo = new ConcurrentHashMap<String, InterpreterInfo>();
	static private Timer connectionValidationTimer = new Timer(true);
	static int mb = 1024*1024;
	
	static {
		connectionValidationTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					Runtime runtime = Runtime.getRuntime();
					System.out.println("\nActive sessions LP: "+sessionIdToMessageList.size()+" WS: "+MessageWebSocket.sessionIdToSocketMapping.size()+" Java Threads: "+Thread.activeCount()+" Used Memory: "+((runtime.totalMemory() - runtime.freeMemory()) / mb)+" Free Memory: "+(runtime.freeMemory() / mb)+"\n");
					sendAllMessage("ping:");
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
				}
			}
		}, 15000, 15000);
	}
	
	public class WidgetSession implements OnTextMessage {
		public String sessionId;
		public Connection session;
		public WidgetSession(String sessionId) {
			this.sessionId = sessionId;
			sessionIdToSocketMapping.put(sessionId, this);
		}

		@Override
		public void onClose(int arg0, String arg1) {
			System.out.println("onClose "+sessionId+" "+arg0+" "+arg1);
			sessionIdToSocketMapping.remove(sessionId);
			socketToSessionIdMapping.remove(this);
			clearMessagesForSessionId(sessionId);
		}

		@Override
		public void onOpen(Connection conn) {
			this.session = conn;
			System.out.println("onOpen "+sessionId);
			try {
				session.sendMessage(InterpreterMessages.SessionIdRequest.toString()+":");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onMessage(String message) {
			System.out.println("onMessage "+sessionId+" "+message);
			if (message.startsWith(InterpreterMessages.SessionIdResponse.toString())) {
				String sessionId = message.substring(message.indexOf(":")+1);
				sessionIdToSocketMapping.put(sessionId, this);
				socketToSessionIdMapping.put(this, sessionId);
			}
		}
	}
	
	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request, String arg1) {
		return new WidgetSession(request.getParameter("sid"));
	}

	public static void sendWSMessage(String sessionId, String message) {
		if (message.startsWith("ping")) {
			System.out.print(":");
		} else {
			System.out.println("sendMessage "+sessionId+" message: "+message);
		}
		WidgetSession ws = sessionIdToSocketMapping.get(sessionId);
		if (ws != null) {
			try {
				ws.session.sendMessage(message);
			} catch (Exception e) {
				
			}
		}
	}
	
	public static void clearMessagesForSessionId(String sessionId) {
		String email = sessionIdToEmail.get(sessionId);
		sessionIdToMessageList.remove(sessionId);
		lastDelivery.remove(sessionId);
		if (email != null && emailToSessionId.containsKey(email)) {
			List<String> sessions = emailToSessionId.get(email);
			sessions.remove(sessionId);
			if (sessions.size() == 0) {
				emailToSessionId.remove(email);
				TLVXManager.interpreterManager.logout(email);
			}
		}
		System.out.println("clearMessagesForSessionId "+sessionId);
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
		if (MessageWebSocket.sessionIdToSocketMapping.containsKey(sessionId)) {
			sendWSMessage(sessionId, message);
		} else {
			List<String> messages = getMessagesForSessionId(sessionId);
			synchronized(messages) {
				messages.add(message);
				messages.notifyAll();
			}
		}
	}
	
	public static List<String> getMessages(String sessionId) {
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
			List<String> returnMessages = new ArrayList<String>();
			if (messages != null) {
				returnMessages.addAll(messages);
				messages.clear();
			}
			return returnMessages;
		}
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

	public static void sendMessageToEmail(String email, String message) {
		try {
			List<String> sessionIds = emailToSessionId.get(email);
			if (sessionIds != null) {
				for(String sessionId : sessionIds) {
					sendMessage(sessionId, message);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
