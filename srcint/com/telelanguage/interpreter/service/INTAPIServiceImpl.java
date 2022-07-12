package com.telelanguage.interpreter.service;

import java.util.HashMap;
import java.util.Map;

import com.telelanguage.api.INTAPIService;
import com.telelanguage.api.InterpreterCallInfo;
import com.telelanguage.interpreter.server.MessageWebSocket;

public class INTAPIServiceImpl implements INTAPIService {

	@Override
	public void interpreterOnHold(InterpreterCallInfo interpreterCallInfo) {
		System.out.println("interpreterOnHold: "+interpreterCallInfo);
		Map<String, String> data = new HashMap<String, String>();
		data.put("agentOnCall", ""+interpreterCallInfo.agentOnCall);
		data.put("missedCalls", ""+interpreterCallInfo.missedCalls);
		data.put("onHold", ""+interpreterCallInfo.onHold);
		data.put("callId", ""+interpreterCallInfo.callId);
		data.put("videoJanusServer", ""+interpreterCallInfo.videoJanusServer);
		data.put("videoJanusRoomNumber", ""+interpreterCallInfo.roomNumber);
		data.put("language", ""+interpreterCallInfo.language);
		data.put("interpreterValidated", ""+interpreterCallInfo.interpreterValidated);
		if (interpreterCallInfo.instructionsText != null)
		data.put("instructionsText", interpreterCallInfo.instructionsText.replaceAll(",", ""));
		for (int i=0; i<interpreterCallInfo.thirdPartyId.size(); i++) {
			data.put("thirdPartyId"+i, interpreterCallInfo.thirdPartyId.get(i));
			data.put("thirdPartyNumber"+i, interpreterCallInfo.thirdPartyNumber.get(i));
		}
		data.put("dialingThirdParty", ""+interpreterCallInfo.dialingThirdParty);
		MessageWebSocket.emailToInterpreterInfo.put(interpreterCallInfo.interpreterInfo.email, interpreterCallInfo.interpreterInfo);
		//InterpreterServiceImpl.setAttribute(interpreterCallInfo.interpreterInfo.email, "ii", interpreterCallInfo.interpreterInfo);
		TLVXManager.interpreterManager.dispatchInterpreterMessage(interpreterCallInfo.interpreterInfo, data, "onhold");
	}

	@Override
	public void interpreterOffHold(InterpreterCallInfo interpreterCallInfo) {
		System.out.println("interpreterOffHold: "+interpreterCallInfo);
		Map<String, String> data = new HashMap<String, String>();
		data.put("agentOnCall", ""+interpreterCallInfo.agentOnCall);
		data.put("missedCalls", ""+interpreterCallInfo.missedCalls);
		data.put("onHold", ""+interpreterCallInfo.onHold);
		data.put("callId", ""+interpreterCallInfo.callId);
		data.put("videoJanusServer", ""+interpreterCallInfo.videoJanusServer);
		data.put("videoJanusRoomNumber", ""+interpreterCallInfo.roomNumber);
		data.put("language", ""+interpreterCallInfo.language);
		data.put("interpreterValidated", ""+interpreterCallInfo.interpreterValidated);
		data.put("playConfIn", ""+interpreterCallInfo.interpreterBrowserSound);
		if (interpreterCallInfo.instructionsText != null)
		data.put("instructionsText", interpreterCallInfo.instructionsText.replaceAll(",", ""));
		data.put("dialingThirdParty", ""+interpreterCallInfo.dialingThirdParty);
		for (int i=0; i<interpreterCallInfo.thirdPartyId.size(); i++) {
			data.put("thirdPartyId"+i, interpreterCallInfo.thirdPartyId.get(i));
			data.put("thirdPartyNumber"+i, interpreterCallInfo.thirdPartyNumber.get(i));
		}
		MessageWebSocket.emailToInterpreterInfo.put(interpreterCallInfo.interpreterInfo.email, interpreterCallInfo.interpreterInfo);
		//InterpreterServiceImpl.setAttribute(interpreterCallInfo.interpreterInfo.email, "ii", interpreterCallInfo.interpreterInfo);
		TLVXManager.interpreterManager.dispatchInterpreterMessage(interpreterCallInfo.interpreterInfo, data, "offhold");
	}

	@Override
	public void askToAcceptCall(InterpreterCallInfo interpreterCallInfo) {
		System.out.println("askToAcceptCall: "+interpreterCallInfo);
		Map<String, String> data = new HashMap<String, String>();
		data.put("agentOnCall", ""+interpreterCallInfo.agentOnCall);
		data.put("missedCalls", ""+interpreterCallInfo.missedCalls);
		data.put("onHold", ""+interpreterCallInfo.onHold);
		data.put("callId", ""+interpreterCallInfo.callId);
		data.put("videoJanusServer", ""+interpreterCallInfo.videoJanusServer);
		data.put("videoJanusRoomNumber", ""+interpreterCallInfo.roomNumber);
		data.put("language", ""+interpreterCallInfo.language);
		data.put("interpreterValidated", ""+interpreterCallInfo.interpreterValidated);
		for (int i=0; i<interpreterCallInfo.thirdPartyId.size(); i++) {
			data.put("thirdPartyId"+i, interpreterCallInfo.thirdPartyId.get(i));
			data.put("thirdPartyNumber"+i, interpreterCallInfo.thirdPartyNumber.get(i));
		}
		data.put("dialingThirdParty", ""+interpreterCallInfo.dialingThirdParty);
		MessageWebSocket.emailToInterpreterInfo.put(interpreterCallInfo.interpreterInfo.email, interpreterCallInfo.interpreterInfo);
		//InterpreterServiceImpl.setAttribute(interpreterCallInfo.interpreterInfo.email, "ii", interpreterCallInfo.interpreterInfo);
		TLVXManager.interpreterManager.dispatchInterpreterMessage(interpreterCallInfo.interpreterInfo, data, "askToAcceptCall");
	}

	@Override
	public void disconnect(InterpreterCallInfo interpreterCallInfo) {
		System.out.println("disconnect: "+interpreterCallInfo);
		Map<String, String> data = new HashMap<String, String>();
		data.put("agentOnCall", ""+interpreterCallInfo.agentOnCall);
		data.put("missedCalls", ""+interpreterCallInfo.missedCalls);
		data.put("onHold", ""+interpreterCallInfo.onHold);
		data.put("callId", ""+interpreterCallInfo.callId);
		data.put("videoJanusServer", ""+interpreterCallInfo.videoJanusServer);
		data.put("videoJanusRoomNumber", ""+interpreterCallInfo.roomNumber);
		data.put("language", ""+interpreterCallInfo.language);
		data.put("interpreterValidated", ""+interpreterCallInfo.interpreterValidated);
		if (interpreterCallInfo.thirdPartyId != null)
		for (int i=0; i<interpreterCallInfo.thirdPartyId.size(); i++) {
			if (interpreterCallInfo.thirdPartyId.get(i) != null)
			data.put("thirdPartyId"+i, interpreterCallInfo.thirdPartyId.get(i));
			if (interpreterCallInfo.thirdPartyNumber.get(i) != null)
			data.put("thirdPartyNumber"+i, interpreterCallInfo.thirdPartyNumber.get(i));
		}
		data.put("dialingThirdParty", ""+interpreterCallInfo.dialingThirdParty);
		MessageWebSocket.emailToInterpreterInfo.put(interpreterCallInfo.interpreterInfo.email, interpreterCallInfo.interpreterInfo);
		//InterpreterServiceImpl.setAttribute(interpreterCallInfo.interpreterInfo.email, "ii", interpreterCallInfo.interpreterInfo);
		TLVXManager.interpreterManager.dispatchInterpreterMessage(interpreterCallInfo.interpreterInfo, data, "disconnect");
	}

	@Override
	public void callIncoming(InterpreterCallInfo interpreterCallInfo) {
		System.out.println("callIncoming: "+interpreterCallInfo);
		Map<String, String> data = new HashMap<String, String>();
		data.put("agentOnCall", ""+interpreterCallInfo.agentOnCall);
		data.put("missedCalls", ""+interpreterCallInfo.missedCalls);
		data.put("onHold", ""+interpreterCallInfo.onHold);
		data.put("callId", ""+interpreterCallInfo.callId);
		data.put("videoJanusServer", ""+interpreterCallInfo.videoJanusServer);
		data.put("videoJanusRoomNumber", ""+interpreterCallInfo.roomNumber);
		data.put("language", ""+interpreterCallInfo.language);
		data.put("interpreterValidated", ""+interpreterCallInfo.interpreterValidated);
		for (int i=0; i<interpreterCallInfo.thirdPartyId.size(); i++) {
			data.put("thirdPartyId"+i, interpreterCallInfo.thirdPartyId.get(i));
			data.put("thirdPartyNumber"+i, interpreterCallInfo.thirdPartyNumber.get(i));
		}
		data.put("dialingThirdParty", ""+interpreterCallInfo.dialingThirdParty);
		MessageWebSocket.emailToInterpreterInfo.put(interpreterCallInfo.interpreterInfo.email, interpreterCallInfo.interpreterInfo);
		//InterpreterServiceImpl.setAttribute(interpreterCallInfo.interpreterInfo.email, "ii", interpreterCallInfo.interpreterInfo);
		TLVXManager.interpreterManager.dispatchInterpreterMessage(interpreterCallInfo.interpreterInfo, data, "callIncoming");
	}

	@Override
	public void agentDisconnected(InterpreterCallInfo interpreterCallInfo) {
		System.out.println("agentDisconnected: "+interpreterCallInfo);
		Map<String, String> data = new HashMap<String, String>();
		data.put("agentOnCall", ""+interpreterCallInfo.agentOnCall);
		data.put("missedCalls", ""+interpreterCallInfo.missedCalls);
		data.put("onHold", ""+interpreterCallInfo.onHold);
		data.put("callId", ""+interpreterCallInfo.callId);
		data.put("videoJanusServer", ""+interpreterCallInfo.videoJanusServer);
		data.put("videoJanusRoomNumber", ""+interpreterCallInfo.roomNumber);
		data.put("language", ""+interpreterCallInfo.language);
		data.put("interpreterValidated", ""+interpreterCallInfo.interpreterValidated);
		for (int i=0; i<interpreterCallInfo.thirdPartyId.size(); i++) {
			data.put("thirdPartyId"+i, interpreterCallInfo.thirdPartyId.get(i));
			data.put("thirdPartyNumber"+i, interpreterCallInfo.thirdPartyNumber.get(i));
		}
		data.put("dialingThirdParty", ""+interpreterCallInfo.dialingThirdParty);
		MessageWebSocket.emailToInterpreterInfo.put(interpreterCallInfo.interpreterInfo.email, interpreterCallInfo.interpreterInfo);
		//InterpreterServiceImpl.setAttribute(interpreterCallInfo.interpreterInfo.email, "ii", interpreterCallInfo.interpreterInfo);
		TLVXManager.interpreterManager.dispatchInterpreterMessage(interpreterCallInfo.interpreterInfo, data, "agentDisconnected");
	}

	@Override
	public void interpreterNotAcceptingCalls(
			InterpreterCallInfo interpreterCallInfo) {
		System.out.println("interpreterNotAcceptingCalls: "+interpreterCallInfo);
		Map<String, String> data = new HashMap<String, String>();
		data.put("agentOnCall", ""+interpreterCallInfo.agentOnCall);
		data.put("missedCalls", ""+interpreterCallInfo.missedCalls);
		data.put("onHold", ""+interpreterCallInfo.onHold);
		data.put("callId", ""+interpreterCallInfo.callId);
		data.put("videoJanusServer", ""+interpreterCallInfo.videoJanusServer);
		data.put("videoJanusRoomNumber", ""+interpreterCallInfo.roomNumber);
		data.put("language", ""+interpreterCallInfo.language);
		data.put("interpreterValidated", ""+interpreterCallInfo.interpreterValidated);
		for (int i=0; i<interpreterCallInfo.thirdPartyId.size(); i++) {
			data.put("thirdPartyId"+i, interpreterCallInfo.thirdPartyId.get(i));
			data.put("thirdPartyNumber"+i, interpreterCallInfo.thirdPartyNumber.get(i));
		}
		data.put("dialingThirdParty", ""+interpreterCallInfo.dialingThirdParty);
		MessageWebSocket.emailToInterpreterInfo.put(interpreterCallInfo.interpreterInfo.email, interpreterCallInfo.interpreterInfo);
		//InterpreterServiceImpl.setAttribute(interpreterCallInfo.interpreterInfo.email, "ii", interpreterCallInfo.interpreterInfo);
		TLVXManager.interpreterManager.dispatchInterpreterMessage(interpreterCallInfo.interpreterInfo, data, "interpreterNotAcceptingCalls");
	}

	@Override
	public void interpreterLogout(InterpreterCallInfo interpreterCallInfo) {
		System.out.println("interpreterNotAcceptingCalls: "+interpreterCallInfo);
		Map<String, String> data = new HashMap<String, String>();
		MessageWebSocket.emailToInterpreterInfo.put(interpreterCallInfo.interpreterInfo.email, interpreterCallInfo.interpreterInfo);
		TLVXManager.interpreterManager.dispatchInterpreterMessage(interpreterCallInfo.interpreterInfo, data, "Logout");
	}

	@Override
	public void callStatusUpdate(InterpreterCallInfo interpreterCallInfo) {
		System.out.println("callStatusUpdate: "+interpreterCallInfo);
		Map<String, String> data = new HashMap<String, String>();
		data.put("agentOnCall", ""+interpreterCallInfo.agentOnCall);
		data.put("missedCalls", ""+interpreterCallInfo.missedCalls);
		data.put("onHold", ""+interpreterCallInfo.onHold);
		data.put("callId", ""+interpreterCallInfo.callId);
		data.put("videoJanusServer", ""+interpreterCallInfo.videoJanusServer);
		data.put("videoJanusRoomNumber", ""+interpreterCallInfo.roomNumber);
		data.put("language", ""+interpreterCallInfo.language);
		data.put("interpreterValidated", ""+interpreterCallInfo.interpreterValidated);
		for (int i=0; i<interpreterCallInfo.thirdPartyId.size(); i++) {
			data.put("thirdPartyId"+i, interpreterCallInfo.thirdPartyId.get(i));
			data.put("thirdPartyNumber"+i, interpreterCallInfo.thirdPartyNumber.get(i));
		}
		data.put("dialingThirdParty", ""+interpreterCallInfo.dialingThirdParty);
		MessageWebSocket.emailToInterpreterInfo.put(interpreterCallInfo.interpreterInfo.email, interpreterCallInfo.interpreterInfo);
		//InterpreterServiceImpl.setAttribute(interpreterCallInfo.interpreterInfo.email, "ii", interpreterCallInfo.interpreterInfo);
		TLVXManager.interpreterManager.dispatchInterpreterMessage(interpreterCallInfo.interpreterInfo, data, "callStatusUpdate");
	}
}
