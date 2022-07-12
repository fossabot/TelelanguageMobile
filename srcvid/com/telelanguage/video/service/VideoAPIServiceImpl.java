package com.telelanguage.video.service;

import java.util.HashMap;
import java.util.Map;

import com.telelanguage.videoapi.VideoAPIService;
import com.telelanguage.videoapi.VideoCallInfo;

public class VideoAPIServiceImpl implements VideoAPIService {

	@Override
	public void videoCallOnHold(VideoCallInfo videoCallInfo) {
		System.out.println(videoCallInfo.callId+" "+videoCallInfo.callSessionId+": videoCallOnHold: "+videoCallInfo);
		Map<String, String> data = getData(videoCallInfo);
		TLVXManager.videoCustomerManager.sessionIdToVideoCustomerInfo.put(videoCallInfo.interpreterInfo.sessionId, videoCallInfo.interpreterInfo);
		//InterpreterServiceImpl.setAttribute(interpreterCallInfo.interpreterInfo.email, "ii", interpreterCallInfo.interpreterInfo);
		TLVXManager.videoCustomerManager.dispatchInterpreterMessage(videoCallInfo.interpreterInfo, data, "onhold");
	}

	@Override
	public void videoCallOffHold(VideoCallInfo videoCallInfo) {
		System.out.println(videoCallInfo.callId+" "+videoCallInfo.callSessionId+" videoCallOffHold: "+videoCallInfo);
		Map<String, String> data = getData(videoCallInfo);
		TLVXManager.videoCustomerManager.sessionIdToVideoCustomerInfo.put(videoCallInfo.interpreterInfo.sessionId, videoCallInfo.interpreterInfo);
		//InterpreterServiceImpl.setAttribute(interpreterCallInfo.interpreterInfo.email, "ii", interpreterCallInfo.interpreterInfo);
		TLVXManager.videoCustomerManager.dispatchInterpreterMessage(videoCallInfo.interpreterInfo, data, "offhold");
	}

	@Override
	public void disconnect(VideoCallInfo videoCallInfo) {
		System.out.println(videoCallInfo.callId+" "+videoCallInfo.callSessionId+" disconnect: "+videoCallInfo);
		Map<String, String> data = getData(videoCallInfo);
		TLVXManager.videoCustomerManager.sessionIdToVideoCustomerInfo.put(videoCallInfo.interpreterInfo.sessionId, videoCallInfo.interpreterInfo);
		//InterpreterServiceImpl.setAttribute(interpreterCallInfo.interpreterInfo.email, "ii", interpreterCallInfo.interpreterInfo);
		TLVXManager.videoCustomerManager.dispatchInterpreterMessage(videoCallInfo.interpreterInfo, data, "disconnect");
	}

	@Override
	public void callIncoming(VideoCallInfo videoCallInfo) {
		System.out.println(videoCallInfo.callId+" "+videoCallInfo.callSessionId+" callIncoming: "+videoCallInfo);
		Map<String, String> data = getData(videoCallInfo);
		TLVXManager.videoCustomerManager.sessionIdToVideoCustomerInfo.put(videoCallInfo.interpreterInfo.sessionId, videoCallInfo.interpreterInfo);
		//InterpreterServiceImpl.setAttribute(interpreterCallInfo.interpreterInfo.email, "ii", interpreterCallInfo.interpreterInfo);
		TLVXManager.videoCustomerManager.dispatchInterpreterMessage(videoCallInfo.interpreterInfo, data, "callIncoming");
	}

	@Override
	public void statusChanged(VideoCallInfo videoCallInfo) {
		System.out.println(videoCallInfo.callId+" "+videoCallInfo.callSessionId+" statusChanged: "+videoCallInfo);
		Map<String, String> data = getData(videoCallInfo);
		TLVXManager.videoCustomerManager.sessionIdToVideoCustomerInfo.put(videoCallInfo.interpreterInfo.sessionId, videoCallInfo.interpreterInfo);
		//InterpreterServiceImpl.setAttribute(interpreterCallInfo.interpreterInfo.email, "ii", interpreterCallInfo.interpreterInfo);
		TLVXManager.videoCustomerManager.dispatchInterpreterMessage(videoCallInfo.interpreterInfo, data, "statusChanged");
	}
	
	private Map<String, String> getData(VideoCallInfo videoCallInfo) {
		Map<String, String> data = new HashMap<String, String>();
		data.put("callId", ""+videoCallInfo.callId);
		data.put("agentOnCall", ""+videoCallInfo.agentOnCall);
		data.put("interpreterOnCall", ""+videoCallInfo.interpreterOnCall);
		data.put("thirdPartyOnCall", ""+videoCallInfo.thirdPartyOnCall);
		data.put("onHold", ""+videoCallInfo.onHold);
		data.put("callId", ""+videoCallInfo.interpreterInfo.callId);
		data.put("roomNumber", ""+videoCallInfo.interpreterInfo.roomNumber);
		data.put("language", ""+videoCallInfo.language);
		data.put("interpreterValidated", ""+videoCallInfo.interpreterValidated);
		data.put("callSessionId", ""+videoCallInfo.callSessionId);
		data.put("isVideo", ""+videoCallInfo.isVideo);
		System.out.println(data);
		return data;
	}

	@Override
	public void playVideoRequest(VideoCallInfo videoCallInfo) {
		System.out.println(videoCallInfo.callId+" "+videoCallInfo.callSessionId+" playVideoRequest: "+videoCallInfo);
		Map<String, String> data = getData(videoCallInfo);
		TLVXManager.videoCustomerManager.sessionIdToVideoCustomerInfo.put(videoCallInfo.interpreterInfo.sessionId, videoCallInfo.interpreterInfo);
		TLVXManager.videoCustomerManager.dispatchInterpreterMessage(videoCallInfo.interpreterInfo, data, "playvideo");
	}

	@Override
	public void pauseVideoRequest(VideoCallInfo videoCallInfo) {
		System.out.println(videoCallInfo.callId+" "+videoCallInfo.callSessionId+" pauseVideoRequest: "+videoCallInfo);
		Map<String, String> data = getData(videoCallInfo);
		TLVXManager.videoCustomerManager.sessionIdToVideoCustomerInfo.put(videoCallInfo.interpreterInfo.sessionId, videoCallInfo.interpreterInfo);
		TLVXManager.videoCustomerManager.dispatchInterpreterMessage(videoCallInfo.interpreterInfo, data, "pausevideo");
	}

	@Override
	public void bluestreamInitiateCall(VideoCallInfo videoCallInfo) {
		System.out.println(videoCallInfo.callId+" "+videoCallInfo.callSessionId+" bluestreamInitiateCall: "+videoCallInfo);
		Map<String, String> data = getData(videoCallInfo);
		TLVXManager.videoCustomerManager.sessionIdToVideoCustomerInfo.put(videoCallInfo.interpreterInfo.sessionId, videoCallInfo.interpreterInfo);
		TLVXManager.videoCustomerManager.dispatchInterpreterMessage(videoCallInfo.interpreterInfo, data, "bluestreamInitiateCall");
	}

	@Override
	public void bluestreamOnHold(VideoCallInfo videoCallInfo) {
		System.out.println(videoCallInfo.callId+" "+videoCallInfo.callSessionId+" bluestreamOnHold: "+videoCallInfo);

	}

	@Override
	public void bluestreamOffHold(VideoCallInfo videoCallInfo) {
		System.out.println(videoCallInfo.callId+" "+videoCallInfo.callSessionId+" bluestreamOffHold: "+videoCallInfo);

	}

	@Override
	public void bluestreamHangup(VideoCallInfo videoCallInfo) {
		System.out.println(videoCallInfo.callId+" "+videoCallInfo.callSessionId+" bluestreamHangup: "+videoCallInfo);

	}
}
