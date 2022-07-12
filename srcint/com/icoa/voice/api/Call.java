package com.icoa.voice.api;

import java.util.Map;

public interface Call {
	Map<String, String> getMap();
	String getId();
	void answer();
	void connect();
	void hangup();
	void stopDialog(String dialogId);
	void createConference(String conferenceId);
	void join(String id1, String id2, String duplex, String string);
	void unjoin(String id1, String id2);
	void stopConference(String confId);
	void destroy();
	void customDialog(String url);
	void recordCall(String connectionid, String recordingtag);
	void conferenceDialogStart(String conferenceId, String src);
	void recordCallStop(String connectionid, String recordingtag);
}
