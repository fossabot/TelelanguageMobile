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
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientProperties;

public class INTAPIClient implements INTAPIService {
	private Client client;
	WebTarget webTarget;
	
	private static final Logger LOG = Logger.getLogger(INTAPIClient.class);
	
	public INTAPIClient(String url) {
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
	public void interpreterOnHold(InterpreterCallInfo interpreterCallInfo) {
		LOG.info(interpreterCallInfo);
		webTarget.path("interpreterOnHold")
				.request(MediaType.APPLICATION_JSON)
				.async()
				.post(Entity.entity(interpreterCallInfo, MediaType.APPLICATION_JSON), 
						new InvocationCallback<String>() {
							@Override
							public void completed(String arg0) {
							}
							@Override
							public void failed(Throwable arg0) {
								arg0.printStackTrace();
							}
				});
	}

	@Override
	public void askToAcceptCall(InterpreterCallInfo interpreterCallInfo) {
		LOG.info(interpreterCallInfo);
		webTarget.path("askToAcceptCall")
				.request(MediaType.APPLICATION_JSON)
				.async()
				.post(Entity.entity(interpreterCallInfo, MediaType.APPLICATION_JSON), 
						new InvocationCallback<String>() {
					@Override
					public void completed(String arg0) {
					}
					@Override
					public void failed(Throwable arg0) {
						arg0.printStackTrace();
					}
		});
	}

	@Override
	public void interpreterOffHold(InterpreterCallInfo interpreterCallInfo) {
		LOG.info(interpreterCallInfo);
		webTarget.path("interpreterOffHold")
				.request(MediaType.APPLICATION_JSON)
				.async()
				.post(Entity.entity(interpreterCallInfo, MediaType.APPLICATION_JSON), 
						new InvocationCallback<String>() {
					@Override
					public void completed(String arg0) {
					}
					@Override
					public void failed(Throwable arg0) {
						arg0.printStackTrace();
					}
		});
	}
	
	@Override
	public void disconnect(InterpreterCallInfo interpreterCallInfo) {
		LOG.info(interpreterCallInfo);
		webTarget.path("disconnect")
				.request(MediaType.APPLICATION_JSON)
				.async()
				.post(Entity.entity(interpreterCallInfo, MediaType.APPLICATION_JSON), 
						new InvocationCallback<String>() {
					@Override
					public void completed(String arg0) {
					}
					@Override
					public void failed(Throwable arg0) {
						arg0.printStackTrace();
					}
		});
	}

	@Override
	public void callIncoming(InterpreterCallInfo interpreterCallInfo) {
		LOG.info(interpreterCallInfo);
		webTarget.path("callIncoming")
				.request(MediaType.APPLICATION_JSON)
				.async()
				.post(Entity.entity(interpreterCallInfo, MediaType.APPLICATION_JSON), 
						new InvocationCallback<String>() {
					@Override
					public void completed(String arg0) {
					}
					@Override
					public void failed(Throwable arg0) {
						arg0.printStackTrace();
					}
		});

	}

	@Override
	public void agentDisconnected(InterpreterCallInfo interpreterCallInfo) {
		LOG.info(interpreterCallInfo);
		webTarget.path("agentDisconnected")
				.request(MediaType.APPLICATION_JSON)
				.async()
				.post(Entity.entity(interpreterCallInfo, MediaType.APPLICATION_JSON), 
						new InvocationCallback<String>() {
					@Override
					public void completed(String arg0) {
					}
					@Override
					public void failed(Throwable arg0) {
						arg0.printStackTrace();
					}
		});
	}

	@Override
	public void interpreterNotAcceptingCalls(
			InterpreterCallInfo interpreterCallInfo) {
		LOG.info(interpreterCallInfo);
		webTarget.path("interpreterNotAcceptingCalls")
				.request(MediaType.APPLICATION_JSON)
				.async()
				.post(Entity.entity(interpreterCallInfo, MediaType.APPLICATION_JSON), 
						new InvocationCallback<String>() {
					@Override
					public void completed(String arg0) {
					}
					@Override
					public void failed(Throwable arg0) {
						arg0.printStackTrace();
					}
		});
	}

	@Override
	public void interpreterLogout(InterpreterCallInfo interpreterCallInfo) {
		LOG.info(interpreterCallInfo);
		webTarget.path("interpreterLogout")
				.request(MediaType.APPLICATION_JSON)
				.async()
				.post(Entity.entity(interpreterCallInfo, MediaType.APPLICATION_JSON), 
						new InvocationCallback<String>() {
					@Override
					public void completed(String arg0) {
					}
					@Override
					public void failed(Throwable arg0) {
						arg0.printStackTrace();
					}
		});
	}

	@Override
	public void callStatusUpdate(InterpreterCallInfo interpreterCallInfo) {
		LOG.info(interpreterCallInfo);
		webTarget.path("callStatusUpdate")
				.request(MediaType.APPLICATION_JSON)
				.async()
				.post(Entity.entity(interpreterCallInfo, MediaType.APPLICATION_JSON), 
						new InvocationCallback<String>() {
					@Override
					public void completed(String arg0) {
					}
					@Override
					public void failed(Throwable arg0) {
						arg0.printStackTrace();
					}
		});
	}
}
