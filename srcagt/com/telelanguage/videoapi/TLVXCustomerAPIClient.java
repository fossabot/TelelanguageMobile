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

public class TLVXCustomerAPIClient implements TLVXCustomerAPIService {
	private Client client;
	WebTarget webTarget;
	
	public TLVXCustomerAPIClient(String url) {
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
		webTarget = client.target(url+"customer/v1/");
	}

	@Override
	public CustomerLoginResponse registerCustomer(CustomerRegistrationRequest request) {
		Response response = webTarget.path("registerCustomer")
			.request(MediaType.APPLICATION_JSON)
			.post(Entity.entity(request, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return null;
		} else {
			return response.readEntity(CustomerLoginResponse.class);
		}
	}

	@Override
	public CustomerLoginResponse loginCustomer(CustomerLoginRequest request) {
		Response response = webTarget.path("loginCustomer")
			.request(MediaType.APPLICATION_JSON)
			.post(Entity.entity(request, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 200) {
			return response.readEntity(CustomerLoginResponse.class);
		} else {
			return null;
		}
	}

	@Override
	public void requestInterpreter(InterpreterRequest request) {
		Response response = webTarget.path("requestInterpreter")
			.request(MediaType.APPLICATION_JSON)
			.post(Entity.entity(request, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("Error requestInterpreter "+response.getStatus());
		}
	}

	public static void main(String[] args) {
		//TLVXCustomerAPIClient test = new TLVXCustomerAPIClient("http://localhost.icoa.com:8890/api/");
		TLVXCustomerAPIClient test = new TLVXCustomerAPIClient("http://207.170.197.36:8080/tlvx/api/");
		InterpreterRequest request = new InterpreterRequest();
		//request.accessCode = "9999";
		//request.sipUri = "4074044249";
		//request.sipUri = //"sip:6000@v18.icoa.com:5060";
		//"sip:vid-0EC46199-3DB1-4F17-AC79-AA087C6BD28C@192.241.197.94:5060";
		//request.videoServer = "http://v18.icoa.com:8018/api/";
		//request.deptCode="1000";
		test.requestInterpreter(request);
	}

	public VideoCustomerInfo findVideoCustomerByEmail(String emailAddress) {
		// TODO Auto-generated method stub
		return null;
	}

	public void saveVideoCustomer(VideoCustomerInfo interpreter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CustomerLogoutResponse logoutCustomer(CustomerLogoutRequest request) {
		Response response = webTarget.path("logoutCustomer")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(request, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 200) {
			return response.readEntity(CustomerLogoutResponse.class);
		} else {
			return null;
		}
	}

	@Override
	public CustomerHangupResponse hangupCustomer(CustomerHangupRequest request) {
		Response response = webTarget.path("hangupCustomer")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(request, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 200) {
			return response.readEntity(CustomerHangupResponse.class);
		} else {
			return null;
		}
	}
	
	@Override
	public void videoSessionStarted(CustomerVideoEstablished request) {
		Response response = webTarget.path("videoSessionStarted")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(request, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("Error requestInterpreter "+response.getStatus());
		}
	}

	@Override
	public void requestCredentials(CredentialRequest request) {
		Response response = webTarget.path("requestCredentials")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(request, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("Error requestInterpreter "+response.getStatus());
		}
	}

	@Override
	public void scheduleInterpreter(ScheduleInterpreterRequest request) {
		Response response = webTarget.path("scheduleInterpreter")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(request, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("Error requestInterpreter "+response.getStatus());
		}
	}

	@Override
	public void bluestreamConnected(BlueStreamConnected request) {
		Response response = webTarget.path("bluestreamConnected")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(request, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("Error requestInterpreter "+response.getStatus());
		}
	}

	@Override
	public void bluestreamAgentRequest(BlueStreamConnected request) {
		Response response = webTarget.path("bluestreamAgentRequest")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(request, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("Error requestInterpreter "+response.getStatus());
		}
	}

	@Override
	public void bluestreamDisconnected(BlueStreamConnected request) {
		Response response = webTarget.path("bluestreamDisconnected")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(request, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("Error requestInterpreter "+response.getStatus());
		}
	}
}
