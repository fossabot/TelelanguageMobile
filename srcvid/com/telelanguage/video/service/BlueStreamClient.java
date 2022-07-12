package com.telelanguage.video.service;

import java.io.IOException;
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
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;


public class BlueStreamClient  {
	private Client client;
	WebTarget webTarget;
	
	public class LoggingFilter implements ClientRequestFilter {

	    public void filter(ClientRequestContext requestContext) throws IOException {
	        System.out.println(Entity.json(requestContext.getEntity()).toString());
	    }
	}
	
	public BlueStreamClient(String url) {
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
		client.register(new LoggingFilter());
		client.property(ClientProperties.CONNECT_TIMEOUT, 1000);
		client.property(ClientProperties.READ_TIMEOUT, 8000);
		webTarget = client.target(url);
	}

	public BlueStreamJWTResponse jwtUpgrade2(BlueStreamJWTRequestJwt request) {
		Response response = webTarget.path("jwt-upgrade")
			.request(MediaType.APPLICATION_JSON)
			.post(Entity.entity(request, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return null;
		} else {
			return response.readEntity(BlueStreamJWTResponse.class);
		}
	}

	public BlueStreamJWTResponse jwtUpgrade(BlueStreamJWTRequestJwt request) {
		Entity jwt = Entity.entity(request, MediaType.APPLICATION_JSON);
		Response response = webTarget.path("jwt-upgrade")
			.request(MediaType.APPLICATION_JSON)
			.post(Entity.entity(request, MediaType.APPLICATION_JSON));
		if (response.getStatus() == 204) {
			return null;
		} else {
			BlueStreamJWTResponse resp = response.readEntity(BlueStreamJWTResponse.class);
			//String s = response.readEntity(String.class);
			//System.out.println(s);
			return resp;
		}
	}
	
}
