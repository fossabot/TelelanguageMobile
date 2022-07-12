package com.icoa.voice.api.voxeo;

import java.util.HashMap;
import java.util.Map;

import com.icoa.voice.api.Call;

public class VoxeoCall implements Call {
	
	private Map<String, String> callInfo = new HashMap<String, String>();
	
	public VoxeoCall() {
	}
	
	public VoxeoCall(Map<String, String> callInfo) {
		this.callInfo = callInfo;
	}
	
	public String get(String key) {
		return callInfo.get(key);
	}
	
	@Override
	public void answer() {
		VoxeoService.answer(this);
	}

	@Override
	public void hangup() {
		VoxeoService.hangup(this);
	}

	@Override
	public void customDialog(String url) {
		VoxeoService.customDialog(this, url);
	}
	
	@Override
	public void conferenceDialogStart(String conferenceId, String src) {
		VoxeoService.conferenceDialog(this, conferenceId, src);
	}

	public void put(String key, String value) {
		callInfo.put(key, value);
	}

	public void putAll(Map<String, String[]> callInfo) {
		for(String key : callInfo.keySet()) {
			try {
				String value = callInfo.get(key)[0];
			this.callInfo.put(key, value);
			} catch (Exception e) {
				System.out.println("key: "+key+": "+e.getMessage());
			}
		}
	}

	@Override
	public Map<String, String> getMap() {
		return callInfo;
	}

	@Override
	public String getId() {
		return callInfo.get("connectionid");
	}

	@Override
	public void createConference(String conferenceName) {
		VoxeoService.createConference(this, conferenceName);
		
	}

	@Override
	public void connect() {
		VoxeoService.connectCall(this);
	}

	@Override
	public void stopDialog(String dialogId) {
		VoxeoService.stopDialog(this, dialogId);
	}

	@Override
	public void join(String id1, String id2, String duplex, String termdigits, String entertone) {
		VoxeoService.join(this, id1, id2, duplex, termdigits, entertone);
	}
	
	@Override
	public void unjoin(String id1, String id2) {
		VoxeoService.unjoin(this, id1, id2);
	}

	@Override
	public void stopConference(String confId) {
		VoxeoService.stopConference(this, confId);
	}

	@Override
	public void destroy() {
		VoxeoService.destroySession(this);
	}

	public void addToMap(Map<String, String> parameters) {
		callInfo.putAll(parameters);
	}

	@Override
	public void recordCall(String connectionid, String recordingtag) {
		VoxeoService.recordCall(this, connectionid, recordingtag);
	}

	@Override
	public void recordCallStop(String connectionid, String recordingtag) {
		VoxeoService.recordCallStop(this, connectionid, recordingtag);
	}
}