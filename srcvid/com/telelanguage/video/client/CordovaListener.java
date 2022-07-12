package com.telelanguage.video.client;

import com.google.gwt.core.client.EntryPoint;

public interface CordovaListener extends EntryPoint {
	void onPause();
	void onResume();
	void onOnline();
	void onOffline();
	void onPushRegistrationId(String registrationId);
	void onPushNotification(String name, String title, Integer count,
			String sound, String image, String additionalData);
	void onPushError(String error);
	void onDeviceReady();
	void onRotate();
}
