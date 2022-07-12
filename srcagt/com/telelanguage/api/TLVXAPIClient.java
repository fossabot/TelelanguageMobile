package com.telelanguage.api;

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

public class TLVXAPIClient implements TLVXAPIService {
	private Client client;
	WebTarget webTarget;
	
	public TLVXAPIClient(String url) {
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
		webTarget = client.target(url+"v1/");
	}

	@Override
	public InterpreterInfo findInterpreterByEmail(String email) {
		Response response = webTarget.path("findInterpreterByEmail")
			.request(MediaType.APPLICATION_JSON)
			.post(Entity.entity(email, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 200) {
			return response.readEntity(InterpreterInfo.class);
		} else {
			System.out.println("Error findInterpreterByEmail "+email+" "+response.getStatus());
			return null;
		}
	}

	@Override
	public void saveInterpreter(InterpreterInfo interpreter) {
		Response response = webTarget.path("saveInterpreter")
			.request(MediaType.APPLICATION_JSON)
			.post(Entity.entity(interpreter, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("Error saving intpreter "+response.getStatus());
		}
	}

	@Override
	public void markInterpreterOffCall(InterpreterInfo interpreter) {
		Response response = webTarget.path("markInterpreterOffCall")
			.request(MediaType.APPLICATION_JSON)
			.post(Entity.entity(interpreter, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("Error marking interpreter off call "+response.getStatus());
		}
	}

	@Override
	public Boolean checkCommunicationsOk(String thisServerUrl) {
		Response response = webTarget.path("checkCommunicationsOk")
			.request(MediaType.APPLICATION_JSON)
			.post(Entity.entity(thisServerUrl, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 200) {
			return response.readEntity(Boolean.class);
		} else {
			System.out.println("Error checkCommunicationsOk "+thisServerUrl);
			return null;
		}
	}

	@Override
	public void hangupCall(InterpreterInfo interpreter) {
		Response response = webTarget.path("hangupCall")
			.request(MediaType.APPLICATION_JSON)
			.post(Entity.entity(interpreter, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("Error hangupCall "+interpreter);
		}
	}

	@Override
	public void acceptCall(InterpreterInfo interpreter) {
		Response response = webTarget.path("acceptCall")
			.request(MediaType.APPLICATION_JSON)
			.post(Entity.entity(interpreter, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("Error acceptCall "+interpreter);
		}
	}

	@Override
	public void rejectCall(InterpreterInfo interpreter) {
		Response response = webTarget.path("rejectCall")
			.request(MediaType.APPLICATION_JSON)
			.post(Entity.entity(interpreter, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("Error rejectCall "+interpreter);
		}
	}

	@Override
	public void requestAgent(InterpreterInfo interpreter) {
		Response response = webTarget.path("requestAgent")
			.request(MediaType.APPLICATION_JSON)
			.post(Entity.entity(interpreter, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("Error requestAgent "+interpreter);
		}
	}

	@Override
	public void acceptVideo(InterpreterInfo interpreter) {
		Response response = webTarget.path("acceptVideo")
			.request(MediaType.APPLICATION_JSON)
			.post(Entity.entity(interpreter, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("Error acceptVideo "+response.getStatus()+ " "+interpreter);
		}	
	}
	
	@Override
	public void acceptVideoOnly(InterpreterInfo interpreter) {
		Response response = webTarget.path("acceptVideoOnly")
			.request(MediaType.APPLICATION_JSON)
			.post(Entity.entity(interpreter, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("Error acceptVideo "+response.getStatus()+ " "+interpreter);
		}	
	}

	@Override
	public void dontAcceptVideo(InterpreterInfo interpreter) {
		Response response = webTarget.path("dontAcceptVideo")
			.request(MediaType.APPLICATION_JSON)
			.post(Entity.entity(interpreter, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("Error dontAcceptVideo "+interpreter);
		}
	}

	@Override
	public void playCustomerVideo(InterpreterInfo interpreter) {
		Response response = webTarget.path("playCustomerVideo")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(interpreter, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("Error playCustomerVideo "+interpreter);
		}
	}

	@Override
	public void pauseCustomerVideo(InterpreterInfo interpreter) {
		Response response = webTarget.path("pauseCustomerVideo")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(interpreter, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("Error pauseCustomerVideo "+interpreter);
		}
	}

	@Override
	public void videoSessionStarted(InterpreterInfo interpreter) {
		Response response = webTarget.path("videoSessionStarted")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(interpreter, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("Error videoSessionStarted "+interpreter);
		}
	}

	public void dialThirdParty(InterpreterInfo interpreter) {
		Response response = webTarget.path("dialThirdParty")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(interpreter, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("Error dialThirdParty "+interpreter);
		}
	}

	public void hangupThirdParty(InterpreterInfo interpreter) {
		Response response = webTarget.path("hangupThirdParty")
				.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(interpreter, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return;
		} else {
			System.out.println("Error hangupThirdParty "+interpreter);
		}
	}


	
//	public static void main(String[] args) {
//		TLVXAPIClient test = new TLVXAPIClient("http://216.151.25.48:8080/tlvx/api/");
//		InterpreterInfo info = test.findInterpreterByEmail("violetta_8023@hotmail.com");
//		System.out.println(info);
//	}
}
