package com.telelanguage.video.client;

import com.google.gwt.core.client.JavaScriptObject;

public interface JanusVideoRoomListener {
	void videoRoomPluginSuccess();
	void videoRoomPluginError(String error);
	void videoRoomPluginConsentDialog(Boolean on);
	void videoRoomPluginJoinedRoom(String roomId, String myId);
	void videoRoomNewParticipant(String id, String display);
	void videoRoomSlowlink(String uplink, String nacks);
	void videoRoomLocalStream(JavaScriptObject stream);
	void videoRoomOnCleanup();
}
