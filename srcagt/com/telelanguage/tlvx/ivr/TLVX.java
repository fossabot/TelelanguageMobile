package com.telelanguage.tlvx.ivr;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.icoa.voice.api.Call;
import com.icoa.voice.api.VoiceApplication;
import com.icoa.voice.api.voxeo.VoxeoCall;
import com.telelanguage.tlvx.service.TLVXManager;
import com.telelanguage.tlvx.util.PropertyLoader;

public class TLVX extends VoiceApplication {
	private static Properties properties = PropertyLoader.loadProperties("tlvx.properties");
	private static boolean telmode;
	
	@Override
	public void init() {
		super.init();
		TLVXManager.setTlvxIvr(this);
		telmode = ((String)properties.getProperty("dialmode")).contains("tel");  //tel or sip
	}

	public void incomingCall(Call call) {
		TLVXManager.callSessionManager.createSession(call.getMap());
	}
	
	private Call getCall(Map<String, String> parameters) {
		Call call = null;
		call = new VoxeoCall();
		((VoxeoCall)call).addToMap(parameters);
		return call;
	} 
	
	public void destroyCall(Call call) {
		//if (call != null && call.getId() != null) callsByConnectionId.remove(call.getId());
	}

	public void createConference(Map<String, String> parameters) {
		Call call = new VoxeoCall(parameters);
		call.createConference((String)parameters.get("confname"));
	}

	public void createCall(Map<String, String> parameters) {
		if (telmode) {
			parameters.remove("callerid");
			String dest = (String)parameters.get("dest");
			if (!dest.startsWith("sip:777")) {
			dest = "tel:+1"+dest.substring(4, dest.indexOf("@"));
			}
			//if (!dest.contains("4074044249")) {
			//	dest = "sip:5000@v11.icoa.com";
			//}
			parameters.put("dest", dest);
		}
		Call call = createSipCall(parameters);
		call.connect();
	}

	public void join(Map<String, String> parameters) {
		Call call = getCall(parameters);
		call.join((String)parameters.get("id1"), (String)parameters.get("id2"), (String)parameters.get("duplex"), (String)parameters.get("termdigits"), (String)parameters.get("entertone"));
	}
	
	public void unjoin(Map<String, String> parameters) {
		Call call = getCall(parameters);
		call.unjoin((String)parameters.get("id1"), (String)parameters.get("id2"));
	}

	public void disconnect(Map<String, String> parameters) {
		Call call = getCall(parameters);
		call.hangup();
	}

	public void destroySession(Map<String, String> parameters) {
		Call call = getCall(parameters);
		if (call != null) {
			destroyCall(call);
			call.destroy();
		}
	}

	public void accept(Map<String, String> parameters) {
		Call call = getCall(parameters);
		call.answer();
	}

	public void redirect(Map<String, String> parameters) {
		System.out.println(">>> redirect (NOT IMPLEMENTED): Map: "+mapDisplay(parameters));
	}

	public void dialogStart(Map<String, String> parameters) {
		Call call = getCall(parameters);
		call.customDialog((String)parameters.get("src"));
	}
	
	public void conferenceDialogStart(String connectionId, String conferenceId, String src, String ccxmlServer, String sessionId) {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("connectionid", connectionId);
		parameters.put("conferenceid", conferenceId);
		parameters.put("sessionid", sessionId);
		parameters.put("remoteAddress", ccxmlServer);
		Call call = getCall(parameters);
		call.conferenceDialogStart(conferenceId, src);
	}
	
	@Override
	public void exitedDialog(Call call) {
		TLVXManager.callSessionManager.onDialogExit(call.getMap());
	}
	
	@Override
	public void disconnectedCall(Call call) {
		TLVXManager.callSessionManager.onConnectionDisconnected(call.getMap());
	}

	public void dialogTerminate(Map<String, String> parameters) {
		Call call = getCall(parameters);
		call.stopDialog((String)parameters.get("dialogid"));
	}

	public void destroyConference(Map<String, String> parameters) {
		Call call = getCall(parameters);
		if (call != null) call.stopConference((String)parameters.get("conferenceid"));
		else System.out.println("destroyConference call is null: "+parameters);
	}

	@Override
	public void conferenceUnJoined(Call call) {
		TLVXManager.callSessionManager.onConferenceUnjoined(call.getMap());
	}
	
	@Override
	public void sessionDestroyed(Call call) {
		TLVXManager.callSessionManager.onSessionDestroyed(call.getMap());
	};
	
	private String mapDisplay(Map<String, String> parameters) {
		String output = "";
		for (Object key: parameters.keySet()) {
			output += "\n"+ (String)key +" = "+ parameters.get(key);
		}
		return output;
	}
	
	@Override
	public void conferenceDestroyed(Call call) {
		TLVXManager.callSessionManager.onConferenceDestroyed(call.getMap());
	}
	
	@Override
	public void conferenceUnjoined(Call call) {
		TLVXManager.callSessionManager.onConferenceUnjoined(call.getMap());
	}

	@Override
	public Properties getAppProps() {
		return TLVXManager.getProperties();
	}

	@Override
	public void outgoingCall(Call call) {
		TLVXManager.callSessionManager.onConnectionProgressing(call.getMap());
	}

	@Override
	public void connectedCall(Call call) {
		TLVXManager.callSessionManager.onConnectionConnected(call.getMap());
	}

	@Override
	public void createdConference(Call call) {
		TLVXManager.callSessionManager.onConferenceCreated(call.getMap());
	}

	@Override
	public void dialogStarted(Call call) {
		TLVXManager.callSessionManager.onDialogStarted(call.getMap());
	}

	@Override
	public void dialogExit(Call call) {
		TLVXManager.callSessionManager.onDialogExit(call.getMap());
	}

	@Override
	public void conferenceJoined(Call call) {
		TLVXManager.callSessionManager.onConferenceJoined(call.getMap());
	}

	@Override
	public void conferenceErrorJoin(Call call) {
		TLVXManager.callSessionManager.onConferenceUnjoined(call.getMap());
	}

	@Override
	public void callConnectionFailed(Call call) {
		TLVXManager.callSessionManager.onConnectionFailed(call.getMap());
	}

	@Override
	public void cleanupSession() {
		TLVXManager.cleanupSession();
	}

	@Override
	public void callConnectionErrorWrongstate(Call call) {
		TLVXManager.callSessionManager.onConnectionWrongState(call.getMap());
	}

	public void recordCall(Map<String, String> parameters) {
		Call call = getCall(parameters);
		call.recordCall((String)parameters.get("connectionid"), (String)parameters.get("recordingtag"));
	}

	public void recordCallStop(Map<String, String> parameters) {
		Call call = getCall(parameters);
		call.recordCallStop((String)parameters.get("connectionid"), (String)parameters.get("recordingtag"));
	}

	@Override
	public void errorSemantic(Call call) {
		TLVXManager.callSessionManager.onErrorSemantic(call.getMap());
	}

	@Override
	public void errorDialogNotStarted(Call call) {
		TLVXManager.callSessionManager.onErrorDialogNotStarted(call.getMap());
	}

	@Override
	public void ccxmlLoaded(Call call) {
		TLVXManager.callSessionManager.ccxmlLoaded(call.getMap());
	}
}
