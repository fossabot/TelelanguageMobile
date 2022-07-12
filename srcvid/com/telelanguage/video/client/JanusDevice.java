package com.telelanguage.video.client;

import com.google.gwt.core.client.JavaScriptObject;

public class JanusDevice extends JavaScriptObject {
	protected JanusDevice() { }
	public final native String getLabel() /*-{
		return this.label;
	}-*/;
	public final native String getDeviceId() /*-{
		return this.deviceId;
	}-*/;
	public final native String getKind() /*-{
		return this.kind;
	}-*/;
}
