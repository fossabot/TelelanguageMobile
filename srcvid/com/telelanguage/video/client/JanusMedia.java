package com.telelanguage.video.client;

import com.google.gwt.core.client.JavaScriptObject;

public class JanusMedia extends JavaScriptObject {
	protected JanusMedia(){}

	/* { 
				audioRecv: false, 
				videoRecv: false, 
				audioSend: useAudio, 
				videoSend: true,
				audio: {
					deviceId: {
						exact: audioId
					}
				},
				video: {
					deviceId: {
						exact: videoId
					}
				}
			} */
	
	public final void setDefaultValues() {
		setAudioRecv(false);
		setVideoRecv(false);
		setAudioSend(true);
		setVideoSend(true);
	}
	
	public final native void setAudioRecv(boolean value) /*-{
		this.audioRecv = value;
	}-*/;

	public final native void setVideoRecv(boolean value) /*-{
		this.videoRecv = value;
	}-*/;

	public final native void setAudioSend(boolean value) /*-{
		this.audioSend = value;
	}-*/;

	public final native void setVideoSend(boolean value) /*-{
		this.videoSend = value;
	}-*/;

	public final native void setAudioDeviceId(String value) /*-{
		this.audio = {
			deviceId : {
				exact : value
			}
		}
	}-*/;
	
	public final native void setVideoDeviceId(String value) /*-{
		this.video = {
			deviceId : {
				exact : value
			}
		}
	}-*/;
}
