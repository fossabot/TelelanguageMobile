package com.telelanguage.video.service;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;

import com.icoa.voxeo.routing.model.RoutingRequest;
import com.icoa.voxeo.routing.model.RoutingResponse;
import com.telelanguage.video.client.CustomerInfo;
import com.telelanguage.video.server.MessageWebSocket;

public class BalancerService {
	
	private Client client;
	WebTarget webTarget;
	int maxRoom = Integer.parseInt(TLVXManager.getProperties().getProperty("maxRoom"));
	Set<String> roomsInUse = new HashSet<String>();
	Map<String, String> sessionIdToRoomMap = new HashMap<String, String>();
	
	public BalancerService(String url) {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){ 
			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			} 

        }}; 
        HostnameVerifier hv = new HostnameVerifier() { 
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			} 
        }; 
		
		SSLContext sc=null;
		try {
			sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new SecureRandom());
		} catch (Exception e) {
			e.printStackTrace();
		}
		client = ClientBuilder
				.newBuilder()
				.sslContext(sc)
				.hostnameVerifier(hv)
				//.register(new LoggingFilter(Logger.getAnonymousLogger(), true))
				.build();
		client.property(ClientProperties.CONNECT_TIMEOUT, 1000);
		client.property(ClientProperties.READ_TIMEOUT, 2000);
		webTarget = client.target(url+"balancer/v1/");
	}
	
	public synchronized void removeSessionId(String sessionId) {
		String room = sessionIdToRoomMap.get(sessionId);
		if (room != null) {
			roomsInUse.remove(room);
		}
		sessionIdToRoomMap.remove(sessionId);
	}
	
	static private Integer nextRoomNumber = 1;
	private Integer getNextRoomNumber() {
		nextRoomNumber++;
		if (nextRoomNumber > maxRoom) nextRoomNumber = 1;
		return nextRoomNumber;
	}
	
	public synchronized CustomerInfo getCustomerInfoByToken(String sessionId, Boolean isVideo, String ipAddress) {
		CustomerInfo info = new CustomerInfo();
		if (isVideo != null && isVideo) {
			info.room = MessageWebSocket.getRoomForSessionId(sessionId);
//			if (sessionIdToRoomMap.containsKey(sessionId)) {
//				info.room = Integer.parseInt(sessionIdToRoomMap.get(sessionId));
//			} else {
//				info.room = getNextRoomNumber();
//				int count = 1;
//				while(roomsInUse.contains(""+info.room)) {
//					info.room = getNextRoomNumber();
//					if (count > maxRoom) throw new RuntimeException("All circuits are busy.");
//					count++;
//				}
//				roomsInUse.add(""+info.room);
//				sessionIdToRoomMap.put(sessionId, ""+info.room);
//			}
		}
		info.email="shawn@rhoads.com";
		info.name="Shawn Rhoads";
		info.janusApiUrl = TLVXManager.getProperties().getProperty("webrtcUrl");
		info.sipAddress = TLVXManager.getProperties().getProperty("sipPrefix")+sessionId+"@"+TLVXManager.getProperties().getProperty("webPhoneSipAddressEnding");
		info.sipProxy = TLVXManager.getProperties().getProperty("webPhoneSipProxy");
		info.wsUrl = TLVXManager.getProperties().getProperty("wsUrl");
		info.loginToken="unused";
		if (ipAddress != null && ipAddress.startsWith(TLVXManager.getProperties().getProperty("turnServerLocalPrefix"))) {
			info.turnServer = TLVXManager.getProperties().getProperty("turnServerLocal");
		} else {
			info.turnServer = TLVXManager.getProperties().getProperty("turnServer");
		}
		System.out.println("turnServer: "+info.turnServer);
		info.turnUsername = TLVXManager.getProperties().getProperty("turnUsername");
		info.turnPassword = TLVXManager.getProperties().getProperty("turnPassword");
		return info;
//		RoutingRequest request = new RoutingRequest();
//		RoutingResponse rr = null;
//		CustomerInfo info =  null;
//		Response response = webTarget.path("getVideoRoom")
//				.request(MediaType.APPLICATION_JSON)
//				.post(Entity.entity(request, MediaType.APPLICATION_JSON));
//		if (response.getStatus() == 200) {
//			rr = response.readEntity(RoutingResponse.class);
//		//info.sipAddress = "sip:vid-"+sessionId+"@"+TLVXManager.getProperties().getProperty("webPhoneSipAddressEnding");
		//info.sipAddress = rr.sipPrefix +sessionId+"@"+rr.sipHost;
//		info.room = rr.room;
//		info.sipProxy = rr.sipProxy;
//		} else {
//			return null;
//		}
	}

}
