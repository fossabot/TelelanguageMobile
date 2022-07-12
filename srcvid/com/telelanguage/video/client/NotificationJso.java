package com.telelanguage.video.client;

import com.google.gwt.core.client.JavaScriptObject;

public class NotificationJso extends JavaScriptObject {
	protected NotificationJso() {
		
	}
	public final native String getName() /*-{
	return this.name;
}-*/;
public final native String getTitle() /*-{
	return this.title;
}-*/;
public final native Integer getCount() /*-{
	return this.count;
}-*/;
public final native String getSound() /*-{
	return this.sound;
}-*/;
public final native String getImage() /*-{
	return this.image;
}-*/;
public final native String getAdditionalData() /*-{
	return this.additionalData;
}-*/;
}
