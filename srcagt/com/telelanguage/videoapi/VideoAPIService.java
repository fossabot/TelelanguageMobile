package com.telelanguage.videoapi;

public interface VideoAPIService {
	void callIncoming(VideoCallInfo videoCallInfo);
	void videoCallOnHold(VideoCallInfo videoCallInfo);
	void videoCallOffHold(VideoCallInfo videoCallInfo);
	void disconnect(VideoCallInfo videoCallInfo);
	void statusChanged(VideoCallInfo videoCallInfo);
	void playVideoRequest(VideoCallInfo videoCallInfo);
	void pauseVideoRequest(VideoCallInfo videoCallInfo);
	void bluestreamInitiateCall(VideoCallInfo videoCallInfo);
	void bluestreamOnHold(VideoCallInfo videoCallInfo);
	void bluestreamOffHold(VideoCallInfo videoCallInfo);
	void bluestreamHangup(VideoCallInfo videoCallInfo);
}
