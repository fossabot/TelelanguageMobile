package com.telelanguage.video.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.rpc.RpcRequestBuilder;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class Cordova {
	static CordovaListener entryPoint;
	static boolean deviceReadyCalled = false;
	static String registrationId = null;
	static JavaScriptObject push;
	
	public static void initCordova(CordovaListener entryPoint) {
		Cordova.entryPoint = entryPoint;
		if (noCordova()) deviceReady();
		else exportOnDeviceReady();
	}
	
	public static native void exportOnDeviceReady() /*-{
		var listener = $entry(function() {
			@com.telelanguage.video.client.Cordova::deviceReady()();
		});
		$doc.addEventListener("deviceready", listener, false);
	}-*/;
	
    public static native boolean noCordova() /*-{
    	return !( $doc.URL.indexOf( 'http://' ) === -1 && $doc.URL.indexOf( 'https://' ) === -1);
	}-*/;
    
    public static native String getDeviceUuid() /*-{
    	try {
			return $wnd.device.uuid;
    	} catch (err) {
    		return null;
    	}
	}-*/;
    
    public static native String getDevicePlatform() /*-{
    	try {
    		return $wnd.device.platform;
    	} catch (err) {
    		return null;
    	}
    }-*/;
    
    public static native void setBadgeCount(int count) /*-{
    	@com.telelanguage.video.client.Cordova::push.setApplicationIconBadgeNumber(function() {
			//$wnd.console.log('success');
		}, function() {
			//console.log('error');
		}, count);
    }-*/;
	
	public static void prepareService(ServiceDefTarget service, final String moduleUrl, String relativeServiceUrl) {
		service.setServiceEntryPoint(moduleUrl + relativeServiceUrl);
		service.setRpcRequestBuilder(new RpcRequestBuilder() {
			@Override
			protected void doFinish(RequestBuilder rb) {
				super.doFinish(rb);
				rb.setHeader(MODULE_BASE_HEADER, moduleUrl);
			}
		});
	}
	
    public static native void setValue(String key, String value) /*-{
    	if ($wnd.localStorage != undefined) {
			$wnd.localStorage.setItem(key, value);
    	}
	}-*/;
    
    public static native String getValue(String key) /*-{
    	if ($wnd.localStorage != undefined) {
			return $wnd.localStorage.getItem(key);
    	} else return null;
	}-*/;
    
    public static native void clearValues() /*-{
		$wnd.localStorage.clear();
	}-*/;
	
	public static native void registerCallbacks() /*-{
		//$wnd.alert("regsiterCallbacks");
		var pauseListener = $entry(function() {
			@com.telelanguage.video.client.Cordova::onPause()();
		});
		$doc.addEventListener("pause", pauseListener, false);
		var resumeListener = $entry(function() {
			@com.telelanguage.video.client.Cordova::onResume()();
		});
		$doc.addEventListener("resume", resumeListener, false);
		var offlineListener = $entry(function() {
			@com.telelanguage.video.client.Cordova::onOffline()();
		});
		$doc.addEventListener("offline", offlineListener, false);
		var onlineListener = $entry(function() {
			@com.telelanguage.video.client.Cordova::onOnline()();
		});
		$doc.addEventListener("online", onlineListener, false);
		var orientationListener = $entry(function() {
			@com.telelanguage.video.client.Cordova::onRotate()();
		});
		$wnd.addEventListener("orientationchange", orientationListener, false);
	}-*/;
	
	private static void onPause() {
		entryPoint.onPause();
	}
	
	private static void onResume() {
		entryPoint.onResume();
	}
	
	private static void onOffline() {
		entryPoint.onOffline();
	}
	
	private static void onOnline() {
		entryPoint.onOnline();
	}
	
	private static void onRotate() {
		entryPoint.onRotate();
	}
	
	public static void deviceReady() {
		if (deviceReadyCalled) return;
		deviceReadyCalled=true;
		entryPoint.onDeviceReady();
		registerCallbacks();
		pushInit();
		//Cordova.setBadgeCount(0);
	}
	
	private static native void pushInit() /*-{
		try {
			@com.telelanguage.video.client.Cordova::push = $wnd.PushNotification.init({ android: {senderID: 926268023541},
	         ios: {alert: true, badge: true, sound: true, clearBadge: true}, windows: {} } );
	
		    @com.telelanguage.video.client.Cordova::push.on('registration', function(data) {
		        // data.registrationId
		        @com.telelanguage.video.client.Cordova::registrationId = data.registrationId;
		        @com.telelanguage.video.client.Cordova::pushRegistration(Ljava/lang/String;)(data.registrationId);
		    });
		
		    @com.telelanguage.video.client.Cordova::push.on('notification', function(data) {
		        // data.message,
		        // data.title,
		        // data.count,
		        // data.sound,
		        // data.image,
		        // data.additionalData
		        @com.telelanguage.video.client.Cordova::pushNotification(Lcom/telelanguage/video/client/NotificationJso;)(data);
		    });
		
		    @com.telelanguage.video.client.Cordova::push.on('error', function(e) {
		        // e.message
		        @com.telelanguage.video.client.Cordova::pushError(Ljava/lang/String;)(e.message);
		    });
		}
		catch(err)
		{
		}
	}-*/;

	public static void pushRegistration(String registrationId) {
		entryPoint.onPushRegistrationId(registrationId);
	}
	
	public static void pushNotification(NotificationJso data) {
		entryPoint.onPushNotification(data.getName(), data.getTitle(), data.getCount(), data.getSound(), data.getImage(), data.getAdditionalData());
	}
	
	public static void pushError(String error) {
		entryPoint.onPushError(error);
	}
}
