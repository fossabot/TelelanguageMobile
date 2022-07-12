package com.icoa.webrtc;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;
import org.json.JSONObject;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.RandomBasedGenerator;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class JanusService {
	
	Map<String, AsyncCallback<String>> callbacks = new HashMap<String, AsyncCallback<String>>();
	private static RandomBasedGenerator uuidGenerator = Generators.randomBasedGenerator();
	ClientEndpointConfig wsc = ClientEndpointConfig.Builder.create().preferredSubprotocols(Arrays.asList("janus-protocol")).build();
	ClientManager client = ClientManager.createClient();
	CountDownLatch messageLatch;
	Session session;
	URI uri;

	public JanusService(URI uri) {
		messageLatch = new CountDownLatch(1);
		this.uri = uri;
	}
	
	private void onMessage(String message) {
		
	}
	
	public static String grabNewUuid() {
		return uuidGenerator.generate().toString().replaceAll("-", "");
	}
	
	private void connect(final AsyncCallback<Void> openCallback) {
		try {
			client.connectToServer(new Endpoint() {
				@Override
				public void onOpen(Session sess, EndpointConfig config) {
		            try {
		            	session = sess;
		                session.addMessageHandler(new MessageHandler.Whole<String>() {
		                    @Override
		                    public void onMessage(String message) {
		                        System.out.println("Received message: "+message);
		                        messageLatch.countDown();
		                        onMessage(message);
		                    }
		                });
		                openCallback.onSuccess(null);
		            } catch (Exception e) {
		            	openCallback.onFailure(e);
		            }
				}
			}, wsc, uri);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    private void createSession(AsyncCallback<String> createCallback) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("janus", JanusMessageType.create);
            obj.put("transaction", grabNewUuid());
            session.getBasicRemote().sendText(obj.toString());
        } catch (Exception ex) {
        	createCallback.onFailure(ex);
        }
    }
	
	public static JanusService janus;
	
	public static void main(String[] args) {
		try {
			janus = new JanusService(new URI("ws://v21.icoa.com:8188"));
			
			janus.connect(new AsyncCallback<Void>() {
				@Override
				public void onFailure(Throwable caught) {
					caught.printStackTrace();
				}

				@Override
				public void onSuccess(Void result) {
					janus.createSession(new AsyncCallback<String>() {
						@Override
						public void onFailure(Throwable caught) {
							System.out.println("Connect failure: "+caught);
							caught.printStackTrace();
						}
						@Override
						public void onSuccess(String result) {
							System.out.println("Connect success: "+result);
							//janus.close();
						}
					});
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		while(true)
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}
