package com.telelanguage.videoapi;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

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

public class VideoAPIClient implements VideoAPIService {
	private Client client;
	WebTarget webTarget;
	
	public VideoAPIClient(String url) {
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
		webTarget = client.target(url+"video/v1/");
	}

	@Override
	public void videoCallOnHold(VideoCallInfo videoCallInfo) {
		Response response = webTarget.path("videoCallOnHold")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(videoCallInfo, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("videoCallOnHold "+response.getStatus());
		}
	}

	@Override
	public void videoCallOffHold(VideoCallInfo videoCallInfo) {
		Response response = webTarget.path("videoCallOffHold")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(videoCallInfo, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("videoCallOffHold "+response.getStatus());
		}
	}
	
	@Override
	public void disconnect(VideoCallInfo videoCallInfo) {
		Response response = webTarget.path("disconnect")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(videoCallInfo, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("disconnect "+response.getStatus());
		}
	}

	@Override
	public void callIncoming(VideoCallInfo videoCallInfo) {
		Response response = webTarget.path("callIncoming")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(videoCallInfo, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("callIncoming "+response.getStatus());
		}
	}

	@Override
	public void statusChanged(VideoCallInfo videoCallInfo) {
		Response response = webTarget.path("statusChanged")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(videoCallInfo, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("statusChanged "+response.getStatus());
		}
	}
	
	public static void main(String[] args) {
		//TLVXCustomerAPIClient test = new TLVXCustomerAPIClient("http://localhost.icoa.com:8890/api/");
		VideoAPIClient test = new VideoAPIClient("http://v18.icoa.com:8018/api/");
		VideoCallInfo videoCallInfo = new VideoCallInfo();
		test.videoCallOnHold(videoCallInfo);
	}

	@Override
	public void playVideoRequest(VideoCallInfo videoCallInfo) {
		Response response = webTarget.path("playVideoRequest")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(videoCallInfo, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("playVideoRequest "+response.getStatus());
		}
	}

	@Override
	public void pauseVideoRequest(VideoCallInfo videoCallInfo) {
		Response response = webTarget.path("pauseVideoRequest")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(videoCallInfo, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("pauseVideoRequest "+response.getStatus());
		}
	}

	public void bluestreamInitiateCall(VideoCallInfo videoCallInfo) {
		Response response = webTarget.path("bluestreamInitiateCall")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(videoCallInfo, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("bluestreamInitiateCall "+response.getStatus());
		}
	}

	@Override
	public void bluestreamOnHold(VideoCallInfo videoCallInfo) {
		Response response = webTarget.path("bluestreamOnHold")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(videoCallInfo, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("bluestreamOnHold "+response.getStatus());
		}
	}

	@Override
	public void bluestreamOffHold(VideoCallInfo videoCallInfo) {
		Response response = webTarget.path("bluestreamOffHold")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(videoCallInfo, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("bluestreamOffHold "+response.getStatus());
		}
	}

	@Override
	public void bluestreamHangup(VideoCallInfo videoCallInfo) {
		Response response = webTarget.path("bluestreamHangup")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(videoCallInfo, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("bluestreamHangup "+response.getStatus());
		}
	}
}
