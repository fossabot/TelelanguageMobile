package com.telelanguage.interpreter.client;

import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class Sounds {
	
	AudioElement audio = Document.get().getElementById("sounds").cast();
	AsyncCallback<Void> playCallback;
	
	public Sounds() {
	}
	
//	public void play() {
//		audio.play();
//	}
	
//	public void play(String soundName) {
//		audio.setSrc("audio/"+soundName);
//		audio.play();
//	}
	
//	public void stop() {
//		audio.pause();
//	}

	public void play(String soundName, AsyncCallback<Void> asyncCallback) {
		playCallback = asyncCallback;
		audio.setSrc("audio/"+soundName);
		playWithCallback();
	}
	
	private void asyncCallback() {
		playCallback.onSuccess(null);
	}
	
	private native void playWithCallback() /*-{
		var that = this;
		var playPromise = $doc.getElementById('sounds').play();
		var asyncCallback = function() {
			that.@com.telelanguage.interpreter.client.Sounds::asyncCallback()();
		};
		if (playPromise !== undefined) {
			playPromise.then(asyncCallback);
		} else {
			$wnd.setTimeout(asyncCallback, 500);
		}
	}-*/;
}
