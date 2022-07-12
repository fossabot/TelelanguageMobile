package com.telelanguage.video.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.shared.GWT;

public class Janus {
	private boolean scriptLoaded = false;
	private boolean janusInited = false;
	private boolean registered = false;
	private String hostname;
	private JanusListener listener;
	private JanusEchoTestListener echoTestListener;
	private JanusSipListener sipListener;
	private JanusVideoRoomListener videoRoomListener;
	private JavaScriptObject janus;
	private JavaScriptObject sipcall;
	private JavaScriptObject siplocalstream;
	private JavaScriptObject jsep;
	
	private JavaScriptObject echotest;
	private JavaScriptObject echojsep;
	
	private JavaScriptObject videoroom;
	private JavaScriptObject videoroomjsep;
	private String videoroomusername;
	private String videoroomlocalelementid;
	private JavaScriptObject feeds;
	private JavaScriptObject bitrateTimer;
	public String id;
	private String turnServer;
	private String turnUsername;
	private String turnPassword;
	private boolean isDestroyed = false;
	
	public Janus(JanusListener listener, String hostname, String turnServer, String turnUsername, String turnPassword) {
		this.listener = listener;
		this.hostname = hostname;
		this.turnServer = turnServer;
		this.turnUsername = turnUsername;
		this.turnPassword = turnPassword;
		initJanus(hostname);
	}
	
	private void initSuccess() {
		janusInited = true;
		listener.initSuccess();
	}
	
	private void error(String error) {
		listener.error(error);
	}
	
	private void destroyed() {
		isDestroyed = true;
		listener.destroyed();
	}
	
	public void destroy() {
		if (isDestroyed) {
			GWT.log("destroy called when isDestoryed");
			return;
		}
		janusDestroy();
	}
	//'turn:v18.icoa.com:3478?transport=udp' 'janus' 'janus'
	private native void initJanus(String server) /*-{
		var janusInstance = this;
		//$wnd.Janus.init({ callback: function() {
		$wnd.Janus.init({ debug: "all", callback: function() {
			janusInstance.@com.telelanguage.video.client.Janus::janus = new $wnd.Janus(
				{
					server: server,
					iceServers:  [{url: janusInstance.@com.telelanguage.video.client.Janus::turnServer,
									credential: janusInstance.@com.telelanguage.video.client.Janus::turnPassword,
									username: janusInstance.@com.telelanguage.video.client.Janus::turnUsername
								}],
					success: function() {
						janusInstance.@com.telelanguage.video.client.Janus::initSuccess()();
					},
					error: function(error) {
						janusInstance.@com.telelanguage.video.client.Janus::error(Ljava/lang/String;)(error);
					},
					destroyed: function() {
						janusInstance.@com.telelanguage.video.client.Janus::destroyed()();
					}
				}
			);
		}});
	}-*/;
	
	private native void janusDestroy() /*-{
		var janusInstance = this;
		try {
			janusInstance.@com.telelanguage.video.client.Janus::janus.destroy();
		} catch(e) {
		}
	}-*/;
	
	/* ECHO PLUGIN METHODS */
	
	public void echoPluginAttach(JanusEchoTestListener echoTestListener) {
		if (isDestroyed) {
			GWT.log("echoPluginAttach called when isDestoryed");
			return;
		}
		this.echoTestListener = echoTestListener;
		echoPluginAttachJanus();
	}

	private native void echoPluginAttachJanus() /*-{
		var janusInstance = this;
		
		janusInstance.@com.telelanguage.video.client.Janus::janus.attach(
			{
				plugin: "janus.plugin.echotest",
				success: function(pluginHandle) {
					janusInstance.@com.telelanguage.video.client.Janus::echotest = pluginHandle;
					janusInstance.@com.telelanguage.video.client.Janus::echoPluginSuccess()();
				},
				error: function(error) {
					janusInstance.@com.telelanguage.video.client.Janus::echoPluginError(Ljava/lang/String;)(error);
				},
				consentDialog: function(on) {
					janusInstance.@com.telelanguage.video.client.Janus::echoPluginConsentDialog(Ljava/lang/Boolean;)(on);
				},
				onmessage: function(msg, jsep) {
					var error = msg["error"];
					if(error != null && error != undefined) {
						janusInstance.@com.telelanguage.video.client.Janus::echoPluginError(Ljava/lang/String;)(error);
					}
					if(jsep !== undefined && jsep !== null) {
						janusInstance.@com.telelanguage.video.client.Janus::echotest.handleRemoteJsep({jsep: jsep});
					}
					var result = msg["result"];
					if(result !== null && result !== undefined) {
						if(result === "done") {
							janusInstance.@com.telelanguage.video.client.Janus::echoPluginDone()();
						}
					}
				},
				onlocalstream: function(stream) {
					var myvid = $doc.getElementById('myechotestvideo');
					myvid.srcObject = stream;
					if (typeof($wnd.device) != "undefined" && typeof($wnd.device.platform) != "undefined" && $wnd.device.platform === 'iOS') {
						//$wnd.iosrtc.observeVideo(myvid);
						myvid.src = URL.createObjectURL(stream);
					} else {
						myvid.srcObject = stream;
					}
				},
				onremotestream: function(stream) {
					var remotevid = $doc.getElementById('peerechotestvideo');
					if (typeof($wnd.device) != "undefined" && typeof($wnd.device.platform) != "undefined" && $wnd.device.platform === 'iOS') {
						//$wnd.iosrtc.observeVideo(remotevid);
						remotevid.src = URL.createObjectURL(stream);
					} else {
						remotevid.srcObject = stream;
					}
				},
				oncleanup: function() {
				}
			}
		);
	}-*/;

	public void echoStartTest() {
		if (isDestroyed) {
			GWT.log("echoStartTest called when isDestoryed");
			return;
		}
		echoStartTestJanus();
	}
	
	public native void echoStartTestJanus() /*-{
		var janusInstance = this;
		var body = { "audio": false, "video": true};
		janusInstance.@com.telelanguage.video.client.Janus::echotest.send({"message": body});
		janusInstance.@com.telelanguage.video.client.Janus::echotest.createOffer(
			{
				media: { data: false },	// Let's negotiate data channels as well
				success: function(jsep) {
					janusInstance.@com.telelanguage.video.client.Janus::echotest.send({"message": body, "jsep": jsep});
				},
				error: function(error) {
					$wnd.Janus.log("echoStartTestJanus error:" + JSON.stringify(error));
				}
			});
	}-*/;
	
	private void echoPluginSuccess() {
		echoTestListener.echoPluginSuccess();
	}
	
	private void echoPluginError(String error) {
		echoTestListener.echoPluginError(error);
	}
	
	private void echoPluginConsentDialog(Boolean on) {
		echoTestListener.echoPluginConsentDialog(on);
	}
	
	private void echoPluginDone() {
		echoTestListener.echoPluginDone();
	}

	/* SIP */
	
	public void sipPluginAttach(JanusSipListener sipListener) {
		this.sipListener = sipListener;
		sipPluginAttachJanus();
	}
	
	private native void sipPluginAttachJanus() /*-{
		var janusInstance = this;
		janusInstance.@com.telelanguage.video.client.Janus::janus.attach(
			{
				plugin: "janus.plugin.sip",
				success: function(pluginHandle) {
					janusInstance.@com.telelanguage.video.client.Janus::sipcall = pluginHandle;
					janusInstance.@com.telelanguage.video.client.Janus::sipPluginSuccess()();
				},
				error: function(error) {
					janusInstance.@com.telelanguage.video.client.Janus::sipPluginError(Ljava/lang/String;)($wnd.JSON.stringify(error));
				},
				consentDialog: function(on) {
					janusInstance.@com.telelanguage.video.client.Janus::sipPluginConsentDialog(Ljava/lang/Boolean;)(on);
				},
				onmessage: function(msg, jsep) {
					var error = msg["error"];
					if(error != null && error != undefined) {
						janusInstance.@com.telelanguage.video.client.Janus::sipPluginError(Ljava/lang/String;)($wnd.JSON.stringify(error));
					}
					var result = msg["result"];
					if(result !== null && result !== undefined && result["event"] !== undefined && result["event"] !== null) {
						var event = result["event"];
						if(event === 'registered') {
							janusInstance.@com.telelanguage.video.client.Janus::sipPluginMessageRegistered(Ljava/lang/String;)(result["username"]);
						} else if(event === 'registration_failed') {
							janusInstance.@com.telelanguage.video.client.Janus::sipPluginMessageRegistrationFailed(Ljava/lang/String;)(result["reason"]);
						} else if(event === 'calling') {
							janusInstance.@com.telelanguage.video.client.Janus::sipPluginMessageCalling()();
						} else if(event === 'incomingcall') {
							var doAudio = true, doVideo = true;
							if(jsep !== null && jsep !== undefined) {
								doAudio = (jsep.sdp.indexOf("m=audio ") > -1);
								doVideo = (jsep.sdp.indexOf("m=video ") > -1);
								janusInstance.@com.telelanguage.video.client.Janus::jsep = jsep;
								janusInstance.@com.telelanguage.video.client.Janus::sipPluginMessageIncomingCall(Ljava/lang/String;Ljava/lang/Boolean;Ljava/lang/Boolean;)(result["username"], doAudio, doVideo);
							}
						} else if(event === 'accepted') {
							if(jsep !== null && jsep !== undefined) {
								janusInstance.@com.telelanguage.video.client.Janus::sipcall.handleRemoteJsep({jsep: jsep, error: doHangup });
							}
							janusInstance.@com.telelanguage.video.client.Janus::sipPluginMessageAccepted(Ljava/lang/String;Ljava/lang/String;)(result["username"], result["reason"]);
						} else if(event === 'hangup') {
							janusInstance.@com.telelanguage.video.client.Janus::sipcall.hangup();
							janusInstance.@com.telelanguage.video.client.Janus::sipPluginMessageHangUp(Ljava/lang/String;Ljava/lang/String;)(result["username"], result["reason"]);
						}
					}
				},
				onlocalstream: function(stream) {
					janusInstance.@com.telelanguage.video.client.Janus::siplocalstream = stream;
					//attachMediaStream($wnd.$('#siplocalaudio').get(0), stream);
					//var siplocalaudio = $wnd.$('#siplocalaudio').get(0);
					var siplocalaudio = $doc.getElementById('siplocalaudio');
					//siplocalaudio.srcObject = stream;
					//siplocalaudio.src = URL.createObjectURL(stream);
					if (typeof($wnd.device) != "undefined" && typeof($wnd.device.platform) != "undefined" && $wnd.device.platform === 'iOS') {
						//$wnd.iosrtc.observeVideo(remotevid);
						siplocalaudio.src = URL.createObjectURL(stream);
					} else {
						siplocalaudio.srcObject = stream;
					}
				},
				onremotestream: function(stream) {
					//attachMediaStream($wnd.$('#sipremoteaudio').get(0), stream);
					//var sipremoteaudio = $wnd.$('#sipremoteaudio').get(0);
					var sipremoteaudio = $doc.getElementById('sipremoteaudio');
					//sipremoteaudio.srcObject = stream;
					if (typeof($wnd.device) != "undefined" && typeof($wnd.device.platform) != "undefined" && $wnd.device.platform === 'iOS') {
						//$wnd.iosrtc.observeVideo(remotevid);
						sipremoteaudio.src = URL.createObjectURL(stream);
					} else {
						sipremoteaudio.srcObject = stream;
					}
					//sipremoteaudio.src = URL.createObjectURL(stream);
				},
				oncleanup: function() {
				}
			}
		);
	}-*/;
	
	public native void sipSpeakerOutput() /*-{
		if (typeof($wnd.device) != "undefined" && typeof($wnd.device.platform) != "undefined" && $wnd.device.platform === 'iOS') {
			var sipremoteaudio = $doc.getElementById('sipremoteaudio');
			$wnd.cordova.plugins.iosrtc.observeVideo(sipremoteaudio);
		}
	}-*/;
	
	public native String getSipBitrate() /*-{
		var janusInstance = this;
		var bitrate = janusInstance.@com.telelanguage.video.client.Janus::sipcall.getBitrate();
		return bitrate;
	}-*/;
	
	private native void sipPluginDestroySession() /*-{
		var janusInstance = this;
		janusInstance.@com.telelanguage.video.client.Janus::janus.destroySession();
	}-*/;
	
	private void sipPluginSuccess() {
		sipListener.sipPluginSuccess();
	}
	
	private void sipPluginError(String error) {
		sipListener.sipPluginError(error);
	}

	private void sipPluginConsentDialog(Boolean on) {
		sipListener.sipPluginConsentDialog(on);
	}
	
	private void sipPluginMessageRegistrationFailed(String reason) {
		registered = false;
		sipListener.sipRegistrationFailed(reason);
	}
	
	private void sipPluginMessageRegistered(String username) {
		registered = true;
		sipListener.sipRegistered(username);
	}
	
	private void sipPluginMessageCalling() {
		
	}
	
	public void sipPluginMessageIncomingCall(String username, Boolean audio, Boolean video) {
		sipListener.sipIncomingCall(username, audio, video);
	}
	
	private void sipPluginMessageAccepted(String username, String reason) {
		sipListener.sipAccepted(username, reason);
	}
	
	private void sipPluginMessageHangUp(String username, String reason) {
		sipListener.sipHangup(username, reason);
	}
	
	public void registerSipEndpoint(String username, String secret, String server) {
		if (isDestroyed) {
			GWT.log("registerSipEndpoint called when isDestoryed");
			return;
		}
		sipPluginRegisterSipEndpointJanus(username, secret, server);
	}
	
	private native void sipPluginRegisterSipEndpointJanus(String username, String secret, String server) /*-{
		var janusInstance = this;
		janusInstance.@com.telelanguage.video.client.Janus::sipcall.send({"message": {
			"request" : "register",
			"username" : username,
			"secret" : secret,
			"proxy" : server
		} });
	}-*/;
	
	public void answer() {
		if (isDestroyed) {
			GWT.log("answer called when isDestoryed");
			return;
		}
		sipPluginAnswer();
	}
	
	private native void sipPluginAnswer() /*-{
		var janusInstance = this;
		janusInstance.@com.telelanguage.video.client.Janus::sipcall.createAnswer({
			jsep: janusInstance.@com.telelanguage.video.client.Janus::jsep,
			media: { video: false},
			success: function(jsep) {
				var body = { "request": "accept" };
				janusInstance.@com.telelanguage.video.client.Janus::sipcall.send({"message": body, "jsep": jsep});
			},
			error: function(error) {
				janusInstance.@com.telelanguage.video.client.Janus::sipPluginError(Ljava/lang/String;)($wnd.JSON.stringify(error));
			}
		});
	}-*/;
	
	public native void sipPluginHold() /*-{
		var janusInstance = this;
		janusInstance.@com.telelanguage.video.client.Janus::sipcall.send({message: {request: "hold"}});
	}-*/;
	
	public native void sipPluginRecord(String key) /*-{
		var janusInstance = this;
		var r = {
			"request": "recording",
			"action": "start",
			"audio": true,
			"video": false,
			"peer_audio": true,
			"peer_video": false,
		    "filename": "/opt/record/" + key
		};
		janusInstance.@com.telelanguage.video.client.Janus::sipcall.send({
			"message": r,
			error: function(reason) { GWT.log("record failed: "+ reason); },
			success: function() { 
				// callback recordingSuccess to listener
			}
        });
	}-*/;
	
	public native void sipPluginStopRecording() /*-{
		var janusInstance = this;
		var r = {
			"request": "configure",
			"record": false
		};
		janusInstance.@com.telelanguage.video.client.Janus::sipcall.send({
			"message": r,
			error: function(reason) { GWT.log("stopRecording failed: "+ reason); },
			success: function() { 
				// callback success to listener
			}
		});
	}-*/;

	public native void sipPluginUnhold() /*-{
		var janusInstance = this;
		janusInstance.@com.telelanguage.video.client.Janus::sipcall.send({message: {request: "unhold"}});
	}-*/;
	
	public native void sipPluginMute() /*-{
		var janusInstance = this;
		janusInstance.@com.telelanguage.video.client.Janus::siplocalstream.getAudioTracks()[0].enabled = false;
	}-*/;
	
	public native void sipPluginUnmute() /*-{
		var janusInstance = this;
		janusInstance.@com.telelanguage.video.client.Janus::siplocalstream.getAudioTracks()[0].enabled = true;
	}-*/;

	public void unregisterSip() {
		if (janusInited) {
			sipPluginDetach();
			janusInited = false;
		}
	}
	
	private native void sipPluginDetach() /*-{
		var janusInstance = this;
		janusInstance.@com.telelanguage.video.client.Janus::sipcall.detach({
			success: function(jsep) {
				janusInstance.@com.telelanguage.video.client.Janus::sipPluginSuccessfulDetach()();
			},
			error: function(error) {
				janusInstance.@com.telelanguage.video.client.Janus::sipPluginError(Ljava/lang/String;)($wnd.JSON.stringify(error));
			}
		});
	}-*/;
	
	public void sipPluginSuccessfulDetach() {
		sipListener.sipDetached();
	}

	public void hangup() {
		if (isDestroyed) {
			GWT.log("hangup called when isDestoryed");
			return;
		}
		sipDoHangup();
	}
	
	private native void sipDoHangup() /*-{
		var janusInstance = this;
		var hangup = { "request": "hangup" };
		janusInstance.@com.telelanguage.video.client.Janus::sipcall.send({"message": hangup});
		janusInstance.@com.telelanguage.video.client.Janus::sipcall.hangup();
	}-*/;

	public void sendDTMF(String dtmf) {
		if (isDestroyed) {
			GWT.log("sendDTMF called when isDestoryed");
			return;
		}
		sipDoDTMF(dtmf);
	}
	
	private native void sipDoDTMF(String dtmf) /*-{
		var janusInstance = this;
		janusInstance.@com.telelanguage.video.client.Janus::sipcall.dtmf({dtmf: { tones: dtmf}});
	}-*/;
	
	/* VIDEO ROOM */
	
	public void videoRoomPluginAttach(JanusVideoRoomListener videoRoomListener) {
		this.videoRoomListener = videoRoomListener;
		videoRoomPluginAttachJanus();
	}
	
	private native void videoRoomPluginAttachJanus() /*-{
		var janusInstance = this;
		janusInstance.@com.telelanguage.video.client.Janus::feeds = [];
		janusInstance.@com.telelanguage.video.client.Janus::bitrateTimer = [];
		janusInstance.@com.telelanguage.video.client.Janus::janus.attach(
			{
				plugin: "janus.plugin.videoroom",
				success: function(pluginHandle) {
					janusInstance.@com.telelanguage.video.client.Janus::videoroom = pluginHandle;
					janusInstance.@com.telelanguage.video.client.Janus::videoRoomPluginSuccess()();
				},
				error: function(error) {
					janusInstance.@com.telelanguage.video.client.Janus::videoRoomPluginError(Ljava/lang/String;)($wnd.JSON.stringify(error));
				},
				consentDialog: function(on) {
					janusInstance.@com.telelanguage.video.client.Janus::videoRoomPluginConsentDialog(Ljava/lang/Boolean;)(on);
				},
				mediaState: function(medium, on) {
					$wnd.Janus.debug("Janus " + (on ? "started" : "stopped") + " receiving our " + medium);
				},
				webrtcState: function(on) {
					$wnd.Janus.debug("Janus says our WebRTC PeerConnection is " + (on ? "up" : "down") + " now");
				},
				onmessage: function(msg, jsep) {
					$wnd.Janus.debug(" ::: Got a videoroom message (publisher) :::");
					$wnd.Janus.debug($wnd.JSON.stringify(msg));
					var event = msg["videoroom"];
					$wnd.Janus.debug("Event: " + event);
					if(event != undefined && event != null) {
						if(event === "joined") {
							// Publisher/manager created, negotiate WebRTC and attach to existing feeds, if any
							myid = msg["id"];
							$wnd.Janus.log("Successfully joined room " + msg["room"] + " with ID " + myid);
							janusInstance.@com.telelanguage.video.client.Janus::videoRoomPluginJoinedRoom(Ljava/lang/String;Ljava/lang/String;)(msg["room"], myid);
							//janusInstance.@com.telelanguage.video.client.Janus::janusVideoRoomPublishOwnFeed(Z)(true);
							// Any new feed to attach to?
							if(msg["publishers"] !== undefined && msg["publishers"] !== null) {
								var list = msg["publishers"];
								$wnd.Janus.debug("Got a list of available publishers/feeds:");
								$wnd.Janus.debug(list);
								for(var f in list) {
									var id = list[f]["id"];
									var display = list[f]["display"];
									$wnd.Janus.debug("  >> [" + id + "] " + display);
									janusInstance.@com.telelanguage.video.client.Janus::newRemoteFeedNotification(Ljava/lang/String;Ljava/lang/String;)(id, display)
								}
							}
						} else if(event === "destroyed") {
							// The room has been destroyed
							$wnd.Janus.warn("The room has been destroyed!");
						} else if(event === "event") {
							// Any new feed to attach to?
							if(msg["publishers"] !== undefined && msg["publishers"] !== null) {
								var list = msg["publishers"];
								$wnd.Janus.debug("Got a list of available publishers/feeds:");
								$wnd.Janus.debug(list);
								for(var f in list) {
									var id = list[f]["id"];
									var display = list[f]["display"];
									$wnd.Janus.debug("  >> [" + id + "] " + display);
									janusInstance.@com.telelanguage.video.client.Janus::newRemoteFeedNotification(Ljava/lang/String;Ljava/lang/String;)(id, display)
								}
							} else if(msg["leaving"] !== undefined && msg["leaving"] !== null) {
								// One of the publishers has gone away?
								var leaving = msg["leaving"];
								$wnd.Janus.log("Publisher left: " + leaving);
								var remoteFeed = null;
								for(var i=1; i<6; i++) {
									if(janusInstance.@com.telelanguage.video.client.Janus::feeds[i] != null && janusInstance.@com.telelanguage.video.client.Janus::feeds[i] != undefined && janusInstance.@com.telelanguage.video.client.Janus::feeds[i].rfid == leaving) {
										remoteFeed = janusInstance.@com.telelanguage.video.client.Janus::feeds[i];
										break;
									}
								}
								if(remoteFeed != null) {
									$wnd.Janus.debug("Feed " + remoteFeed.rfid + " (" + remoteFeed.rfdisplay + ") has left the room, detaching");
									//$('#remote'+remoteFeed.rfindex).empty().hide();
									//$('#videoremote'+remoteFeed.rfindex).empty();
									janusInstance.@com.telelanguage.video.client.Janus::feeds[remoteFeed.rfindex] = null;
									remoteFeed.detach();
								}
							} else if(msg["unpublished"] !== undefined && msg["unpublished"] !== null) {
								// One of the publishers has unpublished?
								var unpublished = msg["unpublished"];
								$wnd.Janus.log("Publisher left: " + unpublished);
								if(unpublished === 'ok') {
									// That's us
									janusInstance.@com.telelanguage.video.client.Janus::videoroom.hangup();
									return;
								}
								var remoteFeed = null;
								for(var i=1; i<6; i++) {
									if(janusInstance.@com.telelanguage.video.client.Janus::feeds[i] != null && janusInstance.@com.telelanguage.video.client.Janus::feeds[i] != undefined && janusInstance.@com.telelanguage.video.client.Janus::feeds[i].rfid == unpublished) {
										remoteFeed = janusInstance.@com.telelanguage.video.client.Janus::feeds[i];
										break;
									}
								}
								if(remoteFeed != null) {
									$wnd.Janus.debug("Feed " + remoteFeed.rfid + " (" + remoteFeed.rfdisplay + ") has left the room, detaching");
									//$('#remote'+remoteFeed.rfindex).empty().hide();
									//$('#videoremote'+remoteFeed.rfindex).empty();
									janusInstance.@com.telelanguage.video.client.Janus::feeds[remoteFeed.rfindex] = null;
									remoteFeed.detach();
								}
							} else if(msg["error"] !== undefined && msg["error"] !== null) {
								$wnd.Janus.debug(msg["error"]);
							}
						}
					}
					if(jsep !== undefined && jsep !== null) {
						$wnd.Janus.debug("Handling SDP as well...");
						$wnd.Janus.debug(jsep);
						janusInstance.@com.telelanguage.video.client.Janus::videoroom.handleRemoteJsep({jsep: jsep});
					}
				},
				onlocalstream: function(stream) {
					$wnd.Janus.debug("videoroom local stream");
					var eleid = janusInstance.@com.telelanguage.video.client.Janus::videoroomlocalelementid;
					//var siplocalaudio = $wnd.$('#'+eleid).get(0);
					var element = $doc.getElementById(eleid);
					if (typeof($wnd.device) != "undefined" && typeof($wnd.device.platform) != "undefined" && $wnd.device.platform === 'iOS') {
						element.src = URL.createObjectURL(stream);
					} else {
						element.srcObject = stream;
					}
					janusInstance.@com.telelanguage.video.client.Janus::videoRoomLocalStream(Lcom/google/gwt/core/client/JavaScriptObject;)(stream);
				},
				onremotestream: function(stream) {
				},
				oncleanup: function() {
					janusInstance.@com.telelanguage.video.client.Janus::videoRoomOnCleanup()();
				},
				slowleak: function(uplink, nacks) {
					janusInstance.@com.telelanguage.video.client.Janus::videoRoomSlowlink(Ljava/lang/String;Ljava/lang/String;)($wnd.JSON.stringify(uplink), $wnd.JSON.stringify(nacks));
				}
			}
		);
	}-*/;
	
	private void videoRoomOnCleanup() {
		videoRoomListener.videoRoomOnCleanup();
	}
	
	private void videoRoomLocalStream(JavaScriptObject stream) {
		videoRoomListener.videoRoomLocalStream(stream);
	}
	
	private void videoRoomPluginSuccess() {
		videoRoomListener.videoRoomPluginSuccess();
	}
	
	private void videoRoomPluginError(String error) {
		videoRoomListener.videoRoomPluginError(error);
	}

	private void videoRoomPluginConsentDialog(Boolean on) {
		videoRoomListener.videoRoomPluginConsentDialog(on);
	}
	
	private void videoRoomPluginJoinedRoom(String roomId, String myId) {
		videoRoomListener.videoRoomPluginJoinedRoom(roomId, myId);
	}
	
	public void videoRoomRegisterUsername(String username, String elementid, int room) {
		if (isDestroyed) {
			GWT.log("videoRoomRegisterUsername called when isDestoryed");
			return;
		}
		videoroomusername = username;
		this.videoroomlocalelementid = elementid;
		janusVideoRoomRegisterUsername(videoroomusername, room);
	}
	
	private native void janusVideoRoomRegisterUsername(String username, int room) /*-{
		var janusInstance = this;
		var register = { "request": "join", "room": room, "ptype": "publisher", "display": username };
		janusInstance.@com.telelanguage.video.client.Janus::videoroom.send({"message": register});
	}-*/;
	
	public void videoRoomPublishOwnFeed(boolean useAudio) {
		if (isDestroyed) {
			GWT.log("videoRoomPublishOwnFeed called when isDestoryed");
			return;
		}
		janusVideoRoomPublishOwnFeed(useAudio);
	}
	
	private native void janusVideoRoomPublishOwnFeed(boolean useAudio) /*-{
		var janusInstance = this;
		janusInstance.@com.telelanguage.video.client.Janus::videoroom.createOffer(
		{
			iceServers:  [{url: janusInstance.@com.telelanguage.video.client.Janus::turnServer,
									credential: janusInstance.@com.telelanguage.video.client.Janus::turnPassword,
									username: janusInstance.@com.telelanguage.video.client.Janus::turnUsername
								}],
			media: { audioRecv: false, videoRecv: false, audioSend: useAudio, videoSend: true},	// Publishers are sendonly
			success: function(jsep) {
				$wnd.Janus.debug("Got publisher SDP!");
				$wnd.Janus.debug(jsep);
				var publish = { "request": "configure", "audio": useAudio, "video": true};
				janusInstance.@com.telelanguage.video.client.Janus::videoroom.send({"message": publish, "jsep": jsep});
			},
			error: function(error) {
				$wnd.Janus.error("janusVideoRoomPublishOwnFeed error:", error);
				if (useAudio) {
					 janusInstance.@com.telelanguage.video.client.Janus::videoRoomPublishOwnFeed(Z)(false);
				} else {
					$wnd.Janus.debug("WebRTC error... " + $wnd.JSON.stringify(error));
				}
			}
		});
	}-*/;
	
	public void videoRoomUnpublishOwnFeed() {
		if (isDestroyed) {
			GWT.log("videoRoomUnpublishOwnFeed called when isDestoryed");
			return;
		}
		janusVideoRoomUnpublishOwnFeed();
	}
	
	private native void janusVideoRoomUnpublishOwnFeed() /*-{
		var janusInstance = this;
		var unpublish = { "request": "unpublish" };
		janusInstance.@com.telelanguage.video.client.Janus::videoroom.send({"message": unpublish});
	}-*/;
	
	private native void janusVideoRoomUnmuteAudio() /*-{
		var janusInstance = this;
		janusInstance.@com.telelanguage.video.client.Janus::videoroom.unmuteAudio();
	}-*/;
	
	private native void janusVideoRoomMuteAudio() /*-{
		var janusInstance = this;
		janusInstance.@com.telelanguage.video.client.Janus::videoroom.muteAudio();
	}-*/;
	
	private void newRemoteFeedNotification(String id, String display) {
		videoRoomListener.videoRoomNewParticipant(id, display);
	}
	
	private void videoRoomSlowlink(String uplink, String nacks) {
		videoRoomListener.videoRoomSlowlink(uplink, nacks);
	}
	
	public void addNewParticipant(String id, String display, String videoelementid, int room) {
		if (isDestroyed) {
			GWT.log("addNewParticipant called when isDestoryed");
			return;
		}
		janusVideoRoomNewRemoteFeed(id, display, videoelementid, room);
	}
	
	private native void janusVideoRoomNewRemoteFeed(String id, String display, String videoelementid, int room) /*-{
		// A new feed has been published, create a new plugin handle and attach to it as a listener
		var remoteFeed = null;
		var janusInstance = this;
		janusInstance.@com.telelanguage.video.client.Janus::janus.attach(
		{
			plugin: "janus.plugin.videoroom",
			success: function(pluginHandle) {
				remoteFeed = pluginHandle;
				$wnd.Janus.log("Plugin attached! (" + remoteFeed.getPlugin() + ", id=" + remoteFeed.getId() + ")");
				$wnd.Janus.log("  -- This is a subscriber");
				// We wait for the plugin to send us an offer
				var listen = { "request": "join", "room": room, "ptype": "listener", "feed": id };
				remoteFeed.send({"message": listen});
			},
			error: function(error) {
				$wnd.Janus.error("  -- Error attaching plugin..." + error);
			},
			onmessage: function(msg, jsep) {
				$wnd.Janus.debug(" ::: Got a message (listener) :::");
				$wnd.Janus.debug(JSON.stringify(msg));
				var event = msg["videoroom"];
				$wnd.Janus.debug("Event: " + event);
				if(event != undefined && event != null) {
					if(event === "attached") {
						// Subscriber created and attached
						for(var i=1;i<6;i++) {
							if(janusInstance.@com.telelanguage.video.client.Janus::feeds[i] === undefined || janusInstance.@com.telelanguage.video.client.Janus::feeds[i] === null) {
								janusInstance.@com.telelanguage.video.client.Janus::feeds[i] = remoteFeed;
								remoteFeed.rfindex = i;
								break;
							}
						}
						remoteFeed.rfid = msg["id"];
						remoteFeed.rfdisplay = msg["display"];
						$wnd.Janus.log("Successfully attached to feed " + remoteFeed.rfid + " (" + remoteFeed.rfdisplay + ") in room " + msg["room"]);
					} else if(msg["error"] !== undefined && msg["error"] !== null) {
						$wnd.Janus.debug(msg["error"]);
					} else {
						// What has just happened?
					}
				}
				if(jsep !== undefined && jsep !== null) {
					$wnd.Janus.debug("Handling SDP as well...");
					$wnd.Janus.debug(jsep);
					// Answer and attach
					remoteFeed.createAnswer(
						{
							jsep: jsep,
							media: { audioSend: false, videoSend: false },	// We want recvonly audio/video
							success: function(jsep) {
								$wnd.Janus.debug("Got SDP!");
								$wnd.Janus.debug(jsep);
								var body = { "request": "start", "room": room };
								remoteFeed.send({"message": body, "jsep": jsep});
							},
							error: function(error) {
								$wnd.Janus.error("janusVideoRoomNewRemoteFeed error:" + JSON.stringify(error));
							}
						});
				}
			},
			webrtcState: function(on) {
				$wnd.Janus.log("Janus says this WebRTC PeerConnection (feed #" + remoteFeed.rfindex + ") is " + (on ? "up" : "down") + " now");
			},
			onlocalstream: function(stream) {
				// The subscriber stream is recvonly, we don't expect anything here
			},
			onremotestream: function(stream) {
				$wnd.Janus.debug("Remote feed #" + remoteFeed.rfindex);
				//attachMediaStream($('#remotevideo'+remoteFeed.rfindex).get(0), stream);
				var element = $doc.getElementById(videoelementid);
				if (typeof($wnd.device) != "undefined" && typeof($wnd.device.platform) != "undefined" && $wnd.device.platform === 'iOS') {
					element.src = URL.createObjectURL(stream);
				} else {
					element.srcObject = stream;
				}
//				var sipremoteaudio = $wnd.$('#'+videoelementid).get(0);
//				sipremoteaudio.srcObject = stream;
			},
			oncleanup: function() {
				$wnd.Janus.log(" ::: Got a cleanup notification (remote feed " + id + ") :::");
				if(janusInstance.@com.telelanguage.video.client.Janus::bitrateTimer[remoteFeed.rfindex] !== null && janusInstance.@com.telelanguage.video.client.Janus::bitrateTimer[remoteFeed.rfindex] !== null) 
					clearInterval(janusInstance.@com.telelanguage.video.client.Janus::bitrateTimer[remoteFeed.rfindex]);
				janusInstance.@com.telelanguage.video.client.Janus::bitrateTimer[remoteFeed.rfindex] = null;
			},
			slowLink: function() {
				$wnd.Janus.log("slowLink");
			}
		});
	}-*/;

	public void videoRoomHangup() {
		if (isDestroyed) {
			GWT.log("hangup called when isDestoryed");
			return;
		}
		videoRoomDoHangup();
	}
	
	private native void videoRoomDoHangup() /*-{
		var janusInstance = this;
		var hangup = { "request": "hangup" };
		janusInstance.@com.telelanguage.video.client.Janus::videoroom.send({"message": hangup});
		janusInstance.@com.telelanguage.video.client.Janus::videoroom.hangup();
	}-*/;
}
