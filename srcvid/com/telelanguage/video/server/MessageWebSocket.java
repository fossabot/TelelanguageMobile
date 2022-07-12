package com.telelanguage.video.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.eclipse.jetty.websocket.WebSocketServlet;

import com.telelanguage.video.client.VideoMessages;
import com.telelanguage.video.service.TLVXManager;

public class MessageWebSocket extends WebSocketServlet {

	private static final long serialVersionUID = 1L;
	public static final Map<String, WidgetSession> sessionIdToSocketMapping = new ConcurrentHashMap<String, WidgetSession>();
	public static final Map<WidgetSession, String> socketToSessionIdMapping = new ConcurrentHashMap<WidgetSession, String>();
	static public Map<String, List<String>> emailToSessionId = new ConcurrentHashMap<String, List<String>>();
	static public Map<String, String> sessionIdToEmail = new ConcurrentHashMap<String, String>();
	static public Map<String, Date> lastDelivery = new HashMap<String, Date>();
	static public Map<String, Boolean> pingCheck = new HashMap<String, Boolean>();
	
	// Room reservation system
	static public Set<String> roomsInUse = new HashSet<String>();
	static public Map<String, String> sessionIdToRoomMap = new HashMap<String, String>();
	
	static private Timer connectionValidationTimer = new Timer(true);
	static int mb = 1024*1024;
	
	static {
		connectionValidationTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					Runtime runtime = Runtime.getRuntime();
					System.out.println("\nActive sessions EL: "+sessionIdToEmail.size()+" WS: "+MessageWebSocket.sessionIdToSocketMapping.size()+" Java Threads: "+Thread.activeCount()+" Used Memory: "+((runtime.totalMemory() - runtime.freeMemory()) / mb)+" Free Memory: "+(runtime.freeMemory() / mb)+", Rooms: "+roomsInUse+"\n");
					sendAllMessage("ping:");
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
				}
			}
		}, 15000, 15000);
	}
	
	static int maxRoom = 29;
	static private Integer nextRoomNumber = 1;
	private static Integer getNextRoomNumber() {
		nextRoomNumber++;
		if (nextRoomNumber > maxRoom) nextRoomNumber = 1;
		return nextRoomNumber;
	}
	
	public static Integer getRoomForSessionId(String sessionId) {
		Integer room = null;
		if (sessionIdToRoomMap.containsKey(sessionId)) {
			room = Integer.parseInt(sessionIdToRoomMap.get(sessionId));
		} else {
			room = getNextRoomNumber();
			int count = 1;
			while(roomsInUse.contains(""+room)) {
				room = getNextRoomNumber();
				if (count > maxRoom) {
					System.out.println("All circuits are busy.");
					throw new RuntimeException("All circuits are busy.");
				}
				count++;
			}
			roomsInUse.add(""+room);
			sessionIdToRoomMap.put(sessionId, ""+room);
		}
		return room;
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
			String room = sessionIdToRoomMap.get(sessionId);
			if (room != null) {
				roomsInUse.remove(room);
			}
			socketToSessionIdMapping.remove(this);
			clearMessagesForSessionId(sessionId);
		}

		@Override
		public void onOpen(Connection conn) {
			this.session = conn;
			System.out.println("onOpen "+sessionId);
			try {
				session.sendMessage(VideoMessages.SessionIdRequest.toString()+":");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onMessage(String message) {
			System.out.print("onMessage "+sessionId+" "+message);
			if (message.startsWith(VideoMessages.SessionIdResponse.toString())) {
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
		lastDelivery.remove(sessionId);
		if (email != null && emailToSessionId.containsKey(email)) {
			List<String> sessions = emailToSessionId.get(email);
			sessions.remove(sessionId);
			if (sessions.size() == 0) {
				emailToSessionId.remove(email);
				TLVXManager.videoCustomerManager.logout(email);
			}
		}
		removeSessionId(sessionId);
		System.out.println("clearMessagesForSessionId "+sessionId);
	}
	
	public static void sendAllMessage(String message) {
		for(String sessionId : sessionIdToSocketMapping.keySet()) {
			sendMessage(sessionId, message);
		}
	}
	
	public static void sendMessage(String sessionId, String message) {
		System.out.println("MessageWebSocket.sendMessage: "+sessionId+": "+message);
		if (MessageWebSocket.sessionIdToSocketMapping.containsKey(sessionId)) {
			sendWSMessage(sessionId, message);
		}
	}

	public static void addSessionIdEmail(String sessionId, String email) {
		sessionIdToEmail.put(sessionId, email);
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
		System.out.println("sessionIdToEmail.remove("+sessionId+")");
		sessionIdToEmail.remove(sessionId);
		TLVXManager.balancerService.removeSessionId(sessionId);
	}
}
