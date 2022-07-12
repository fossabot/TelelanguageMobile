package com.telelanguage.video.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.VideoElement;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.telelanguage.video.client.websockets.CloseEvent;
import com.telelanguage.video.client.websockets.Websocket;
import com.telelanguage.video.client.websockets.WebsocketListener;

public class Base implements EntryPoint, WebsocketListener, JanusVideoRoomListener, CordovaListener {
	
	private final VideoServiceAsync videoService = GWT.create(VideoService.class);
	public static String sessionId = UUID.uuid();
	UserInfo userInfo;
	private CustomerInfo customerInfo;
	private Websocket websocket;
	private Janus videoRoom;
	private Janus sipCall;
	private boolean videoCall = false;
	VideoElement videoCustomer = Document.get().getElementById("videocustomer").cast();
	VideoElement videoInterpreter = Document.get().getElementById("videointerpreter").cast();
	private boolean inCall = false;
	enum GUIPage { internetconnectingpage, loginpage, getinterpreterpage, connectingpage, interpreterpage, agentpage, onholdpage, videopage, linkpage, disconnectingpage, getcredpage, requestinterpreterpage };
	EnumSet<GUIPage> callButtonPages = EnumSet.of(GUIPage.connectingpage, GUIPage.interpreterpage, GUIPage.agentpage, GUIPage.onholdpage, GUIPage.videopage);
	private boolean connectedYet = false;
	private boolean localRequestCredentials = false;
	private boolean localRequestInterpreter = false;
	private JavaScriptObject bs;
	private boolean bsConnected = false;
	private String callId="";
	private boolean canStartRecording = false;
	private boolean recordingStarted = false;
	private boolean isBluestreamCall = false;
	private String callSessionId = "";
	private long callStartTime = 0;
	
	private Timer startVideoRoom = new Timer() {
		@Override
		public void run() {
			videoService.getCustomerInfoByToken(sessionId, videoCall, new AsyncCallback<CustomerInfo>() {
				@Override
				public void onSuccess(CustomerInfo result) {
					customerInfo = result;
					iosrtcGlobals(customerInfo.turnServer, customerInfo.turnUsername, customerInfo.turnPassword);
					startVideoRoom();
				}
				@Override
				public void onFailure(Throwable caught) {
					error(caught);
				}
			});
		}
	};
	
	private Timer reloadPageTimer = new Timer() {
		@Override
		public void run() {
			if (!isBluestreamCall)
			Window.Location.reload();
		}
	};

	private Timer startSipCall = new Timer() {
		@Override
		public void run() {
			startSipCall();
		}
	};

	private boolean isLanguageSet() {
		SelectElement languageInput = Document.get().getElementById("languageselect").cast();
		return languageInput.getSelectedIndex() > 0;
	}
	
	private boolean isBluestreamlanguage() {
		SelectElement languageInput = Document.get().getElementById("languageselect").cast();
		String lang = languageInput.getOptions().getItem(languageInput.getSelectedIndex()).getValue();
		return ("BoostlingoSign".equals(lang) || "BoostlingoASL".equals(lang));
	}
	
	private void updateConnectingLanguage() {
		SelectElement languageInput = Document.get().getElementById("languageselect").cast();
		String language = languageInput.getOptions().getItem(languageInput.getSelectedIndex()).getValue();
		if ("ASL".equals(language)) language = "Sign";
		SpanElement connectingSpan = Document.get().getElementById("connectingtospan").cast();
		String an = "A";
		if (language.startsWith("E") || language.startsWith("A") || language.startsWith("I") || language.startsWith("0") ) {
			an = "An";
		}
		connectingSpan.setInnerHTML("You're Being Connected<br/>To "+an+" "+language+" Interpreter");
	}
	
	private void prepBlueStreamCall(final String jwtVal, final String callExpertiseId) {
		if (videoRoom != null) {
			videoRoom.videoRoomHangup();
			videoRoom.destroy();
			videoRoom = null;
		}
		if (sipCall != null) {
			sipCall.hangup();
			sipCall.destroy();
			sipCall = null;
		}
		videoService.getCustomerInfoByToken(sessionId, videoCall, new AsyncCallback<CustomerInfo>() {
			@Override
			public void onFailure(Throwable caught) {
				error(caught);
			}
			@Override
			public void onSuccess(CustomerInfo result) {
				customerInfo = result;
				iosrtcGlobals(result.turnServer, result.turnUsername, result.turnPassword);
				bluestreamCall(jwtVal, callExpertiseId, result.turnServer, result.turnPassword, result.turnUsername);
			}
		});
	}
	
	private native void bluestreamCall(String jwtVal, String callExpertiseId, String turnServer, String turnPassword, String turnUsername) /*-{
		var baseInstance = this;
		$wnd.$('#videointerpreter').remove();
		$wnd.$('#videopage').prepend('<video playsinline class="videointerpreter" id="videointerpreter" style="z-index: -3; object-fit: fill;" autoplay></video>');
		$wnd.$.getScript("https://telelanguage.bluestreamhealth.com/js/bluestream-api.js", function( data, textStatus, jqxhr ) {
			var iceS = [{url: turnServer,
								credential: turnPassword,
								username: turnUsername
							}];
			var bh = new $wnd.Bluestream({
			    jwt: jwtVal,
			    host: 'https://telelanguage.bluestreamhealth.com',
			    enableLogging: true,
			    provider: true,
			    iceServers: iceS
			});
			
			baseInstance.@com.telelanguage.video.client.Base::bs = bh;
			//var localVideo = $doc.getElementById('videocustomer');
			//var remoteVideo = $doc.getElementById('videointerpreter');
			bh.on('localStreamSuccess', function(stream) {
			    //bh.attachLocalStream(localVideo);
			    var element = $doc.getElementById('videocustomer');
				if (typeof($wnd.device) != "undefined" && typeof($wnd.device.platform) != "undefined" && $wnd.device.platform === 'iOS') {
					element.src = URL.createObjectURL(stream);
				} else {
					element.srcObject = stream;
				}
			});
			bh.on('remoteStreamSuccess', function(stream) {
				var element = $doc.getElementById('videointerpreter');
				if (typeof($wnd.device) != "undefined" && typeof($wnd.device.platform) != "undefined" && $wnd.device.platform === 'iOS') {
					element.src = URL.createObjectURL(stream);
				} else {
					element.srcObject = stream;
				}
			    //bh.attachRemoteStream(remoteVideo);
			    baseInstance.@com.telelanguage.video.client.Base::bsRemoteStreamSuccess()();
			});
			bh.on('callAccepted', function(data) {
				baseInstance.@com.telelanguage.video.client.Base::bsConnect(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(data.callId, data.remoteExpertData.id, data.remoteExpertData.name);
			});
			bh.on('callCancelled', function(data) {
				//baseInstance.@com.telelanguage.video.client.Base::bsCallFailed()();
			});
			bh.on('expertEndCall', function(stream) {
				//baseInstance.@com.telelanguage.video.client.Base::bsExpertEndedCall()();
			});
			bh.on('providerEndCall', function(stream) {
				//baseInstance.@com.telelanguage.video.client.Base::bsProviderEndCall()();
			});
			bh.on('callInit', function(stream) {
			});
			bh.on('incomingMessage', function(stream) {
			});
			bh.on('serverEndCall', function(stream) {
			});
			bh.on('disconnected', function(stream) {
				baseInstance.@com.telelanguage.video.client.Base::bsDisconnected()();
			});
			bh.call(callExpertiseId);
		});
	}-*/;
	
	private native void bsHideLocal() /*-{
		var baseInstance = this;
		baseInstance.@com.telelanguage.video.client.Base::bs.hideLocal();
	}-*/;
	
	private native void bsShowLocal() /*-{
		var baseInstance = this;
		baseInstance.@com.telelanguage.video.client.Base::bs.showLocal();
	}-*/;
	
	private native void bsMuteLocal() /*-{
		var baseInstance = this;
		baseInstance.@com.telelanguage.video.client.Base::bs.muteLocal();
	}-*/;
	
	private native void bsUnmuteLocal() /*-{
		var baseInstance = this;
		baseInstance.@com.telelanguage.video.client.Base::bs.unmuteLocal();
	}-*/;
	
	private native void bsEndCall() /*-{
		var baseInstance = this;
		baseInstance.@com.telelanguage.video.client.Base::bs.endCall();
	}-*/;
	
	private native void bsSendMessage(String message) /*-{
		var baseInstance = this;
		baseInstance.@com.telelanguage.video.client.Base::bs.sendMessage(message);
	}-*/;
	
	private void submitVriCred() {
		try {
    	InputElement email = Document.get().getElementById("vri-cred-email").cast();
    	InputElement name = Document.get().getElementById("vri-cred-name").cast();
    	InputElement phone = Document.get().getElementById("vri-cred-phone").cast();
    	InputElement org = Document.get().getElementById("vri-cred-org").cast();
    	SelectElement cust = Document.get().getElementById("vri-cred-customer").cast();
    	
    	if (email.getValue().length()<3 && phone.getValue().length()<3) {
    		info("We need your email, name and phone number.");
    		return;
    	}
    	
    	setPage(GUIPage.loginpage);
    	String custString = cust.getOptions().getItem(cust.getSelectedIndex()).getText();
    	videoService.vriCredSubmit(email.getValue(), name.getValue(), phone.getValue(), org.getValue(), custString, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				error(caught);
			}
			@Override
			public void onSuccess(Void result) {
				info("Thank you!  Your request has been submitted.");
			}
    	});
		} catch (Exception e) {
			error(e);
		}
	}
	
	private native int getTimeZone() /*-{
		var d = new Date();
		var n = d.getTimezoneOffset();
		return n;
	}-*/;
	
	private void submitSchedInt() {
		try {
	    	InputElement email = Document.get().getElementById("sched-int-email").cast();
	    	InputElement name = Document.get().getElementById("sched-int-name").cast();
	    	InputElement phone = Document.get().getElementById("sched-int-phone").cast();
	    	InputElement org = Document.get().getElementById("sched-int-org").cast();
	    	SelectElement language = Document.get().getElementById("sched-int-lang").cast();
	    	SelectElement type = Document.get().getElementById("sched-int-type").cast();
	    	InputElement datetime = Document.get().getElementById("sched-int-datetime").cast();
	    	String userInfoEmail = "";
	    	String userInfoCode = "";
	    	Integer timeZone = getTimeZone();
	    	if (userInfo != null) {
	    		userInfoEmail = userInfo.email;
	    		userInfoCode = userInfo.accessCode;
	    	}
	    	setPage(GUIPage.getinterpreterpage);
	    	String languageString = language.getOptions().getItem(language.getSelectedIndex()).getValue();
	    	String typeString = type.getOptions().getItem(type.getSelectedIndex()).getValue();
	    	videoService.schedIntSubmit(userInfoEmail, userInfoCode, email.getValue(), name.getValue(), phone.getValue(), org.getValue(), languageString, typeString, datetime.getValue(), new Date(), timeZone, new AsyncCallback<Void>() {
				@Override
				public void onFailure(Throwable caught) {
					error(caught);
				}
				@Override
				public void onSuccess(Void result) {
					
				}
	    	});
		} catch (Exception e) {
			error(e);
		}
	}

	@Override
	public void onModuleLoad() {
		Cordova.initCordova(this);
		DivElement loginButton = Document.get().getElementById("loginbutton").cast();
		Event.sinkEvents(loginButton, Event.ONCLICK);
	    Event.setEventListener(loginButton, new EventListener() {
	        @Override
	        public void onBrowserEvent(Event event) {
	        	InputElement loginUsername = Document.get().getElementById("loginusername").cast();
	        	InputElement loginPassword = Document.get().getElementById("loginpassword").cast();
	        	DivElement remembercheckbox = Document.get().getElementById("remembercheckbox").cast();
	        	if (loginUsername != null && loginPassword != null && loginUsername.getValue().length()>3 && loginPassword.getValue().length()>3) {
	        		initWithUsernamePassword(loginUsername.getValue(), loginPassword.getValue(), remembercheckbox.hasClassName("fa-check-square"));
	        	} else {
	        		info("You must enter your username and password.");
	        	}
	        }
	    });
	    
		DivElement videobutton = Document.get().getElementById("videobutton").cast();
		Event.sinkEvents(videobutton, Event.ONCLICK);
	    Event.setEventListener(videobutton, new EventListener() {
	        @Override
	        public void onBrowserEvent(Event event) {
	        	if (!isLanguageSet()) {
	        		info("You must select a language first.");
	        		return;
	        	}
	        	updateConnectingLanguage();
	        	videoCall = true;
				inCall=true;
				connectedYet = false;
				DivElement videocontrolbutton = Document.get().getElementById("videocontrolbutton").cast();
				videocontrolbutton.removeClassName("hide");
				SpanElement videolabel = Document.get().getElementById("videolabel").cast();
				videolabel.removeClassName("hide");
				setPage(GUIPage.connectingpage);
				videoCustomer.setAutoplay(true);
				videoInterpreter.setAutoplay(true);
				startinsomnia();
				startVideoRoom.schedule(1000);
	        }
	    });
	    
		DivElement phonebutton = Document.get().getElementById("phonebutton").cast();
		Event.sinkEvents(phonebutton, Event.ONCLICK);
	    Event.setEventListener(phonebutton, new EventListener() {
	        @Override
	        public void onBrowserEvent(Event event) {
	        	if (!isLanguageSet()) {
	        		info("You must select a language first.");
	        		return;
	        	}
	        	updateConnectingLanguage();
	        	videoCall = false;
				inCall=true;
				connectedYet = false;
				DivElement videocontrolbutton = Document.get().getElementById("videocontrolbutton").cast();
				videocontrolbutton.addClassName("hide");
				SpanElement videolabel = Document.get().getElementById("videolabel").cast();
				videolabel.addClassName("hide");
				setPage(GUIPage.connectingpage);
				videoCustomer.setAutoplay(true);
				videoInterpreter.setAutoplay(true);
				startinsomnia();
				startVideoRoom.schedule(1000);
	        }
	    });
	    
	    DivElement schedInterpretPage = Document.get().getElementById("requestinterpreterpage").cast();
	    if (schedInterpretPage != null) localRequestInterpreter = true;

	    if (null != Document.get().getElementById("sched-int-submit")) {
			DivElement schedIntSubmit = Document.get().getElementById("sched-int-submit").cast();
			Event.sinkEvents(schedIntSubmit, Event.ONCLICK);
		    Event.setEventListener(schedIntSubmit, new EventListener() {
		        @Override
		        public void onBrowserEvent(Event event) {
		        	submitSchedInt();
		        }
		    });

			DivElement schedIntCheckButton = Document.get().getElementById("sched-int-check-button").cast();
			Event.sinkEvents(schedIntCheckButton, Event.ONCLICK);
		    Event.setEventListener(schedIntCheckButton, new EventListener() {
		        @Override
		        public void onBrowserEvent(Event event) {
		        	submitSchedInt();
		        }
		    });

			DivElement schedIntCancel = Document.get().getElementById("sched-int-cancel").cast();
			Event.sinkEvents(schedIntCancel, Event.ONCLICK);
		    Event.setEventListener(schedIntCancel, new EventListener() {
		        @Override
		        public void onBrowserEvent(Event event) {
		        	setPage(GUIPage.getinterpreterpage);
		        }
		    });
	
		    DivElement vriCredPage = Document.get().getElementById("getcredpage").cast();
		    if (vriCredPage != null) localRequestCredentials = true;
		    
			DivElement vriCredCheckButton = Document.get().getElementById("vri-cred-check-button").cast();
			Event.sinkEvents(vriCredCheckButton, Event.ONCLICK);
		    Event.setEventListener(vriCredCheckButton, new EventListener() {
		        @Override
		        public void onBrowserEvent(Event event) {
		        	submitVriCred();
		        }
		    });
	
			DivElement vriCredSubmit = Document.get().getElementById("vri-cred-submit").cast();
			Event.sinkEvents(vriCredSubmit, Event.ONCLICK);
		    Event.setEventListener(vriCredSubmit, new EventListener() {
		        @Override
		        public void onBrowserEvent(Event event) {
		        	submitVriCred();
		        }
		    });
	
			DivElement vriCredCancel = Document.get().getElementById("vri-cred-cancel").cast();
			Event.sinkEvents(vriCredCancel, Event.ONCLICK);
		    Event.setEventListener(vriCredCancel, new EventListener() {
		        @Override
		        public void onBrowserEvent(Event event) {
		        	setPage(GUIPage.loginpage);
		        }
		    });
		    
			DivElement alreadycustomer = Document.get().getElementById("alreadycustomer").cast();
			Event.sinkEvents(alreadycustomer, Event.ONCLICK);
		    Event.setEventListener(alreadycustomer, new EventListener() {
		        @Override
		        public void onBrowserEvent(Event event) {
		        	if (localRequestCredentials) {
		        		setPage(GUIPage.getcredpage);
		        	} else {
			        	DivElement iframe = Document.get().getElementById("iframe").cast();
			        	setPage(GUIPage.linkpage);
			        	iframe.setInnerHTML("<iframe src=\""+userInfo.alreadyCustomerLink+"\" scrolling=\"yes\" style=\"width:100%;height:100%\" frameborder=\"0\"></iframe>");
			        	//Window.open(userInfo.alreadyCustomerLink, "_system", null);
		        	}
		        }
		    });
		    
			DivElement openaccount = Document.get().getElementById("openaccount").cast();
			Event.sinkEvents(openaccount, Event.ONCLICK);
		    Event.setEventListener(openaccount, new EventListener() {
		        @Override
		        public void onBrowserEvent(Event event) {
		        	if (localRequestCredentials) {
		        		setPage(GUIPage.getcredpage);
		        	} else {
			        	DivElement iframe = Document.get().getElementById("iframe").cast();
			        	setPage(GUIPage.linkpage);
			        	iframe.setInnerHTML("<iframe src=\""+userInfo.openAnAccountLink+"\" scrolling=\"yes\" style=\"width:100%;height:100%\" frameborder=\"0\"></iframe>");
			        	//Window.open(userInfo.openAnAccountLink, "_system", null);
		        	}
		        }
		    });
		    
			DivElement preschedule = Document.get().getElementById("preschedule").cast();
			Event.sinkEvents(preschedule, Event.ONCLICK);
		    Event.setEventListener(preschedule, new EventListener() {
		        @Override
		        public void onBrowserEvent(Event event) {
		        	if (localRequestInterpreter) {
		        		setPage(GUIPage.requestinterpreterpage);
		        	} else {
			        	DivElement iframe = Document.get().getElementById("iframe").cast();
			        	setPage(GUIPage.linkpage);
			        	iframe.setInnerHTML("<iframe src=\""+userInfo.preScheduleInterpreterLink+"\"  scrolling=\"yes\" style=\"width:100%;height:100%\" frameborder=\"0\"></iframe>");
			        	//Window.open(userInfo.preScheduleInterpreterLink, "_system", null);
		        	}
		        }
		    });
	    }
	    
		DivElement logout = Document.get().getElementById("logout").cast();
		Event.sinkEvents(logout, Event.ONCLICK);
	    Event.setEventListener(logout, new EventListener() {
	        @Override
	        public void onBrowserEvent(Event event) {
	        	videoService.logout(sessionId, new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						error(caught);
					}
					@Override
					public void onSuccess(Void result) {
			        	InputElement loginUsername = Document.get().getElementById("loginusername").cast();
			        	loginUsername.setValue("");
			        	InputElement loginPassword = Document.get().getElementById("loginpassword").cast();
			        	loginPassword.setValue("");
						setPage(GUIPage.loginpage);
					}
	        	});
	        }
	    });
	    
		DivElement hangupbutton = Document.get().getElementById("hangupbutton").cast();
		Event.sinkEvents(hangupbutton, Event.ONCLICK);
	    Event.setEventListener(hangupbutton, new EventListener() {
	        @Override
	        public void onBrowserEvent(Event event) {
	        	if (isBluestreamlanguage()) {
	        		bsEndCall();
	        	}
	        	customerHangup();
	        	setPage(GUIPage.disconnectingpage);
	        }
	    });
	    
		DivElement micbutton = Document.get().getElementById("micbutton").cast();
		Event.sinkEvents(micbutton, Event.ONCLICK);
	    Event.setEventListener(micbutton, new EventListener() {
	        @Override
	        public void onBrowserEvent(Event event) {
	        	ImageElement micbutton = Document.get().getElementById("micbutton").cast();
	        	SpanElement miclabel = Document.get().getElementById("miclabel").cast();
	        	if (micbutton.getSrc().contains("img/micsquare.png")) {
	        		micbutton.setSrc("img/micslash.png");
	        		miclabel.setInnerHTML("Unmute");
	        		sipCall.sipPluginMute();
	        		if (isBluestreamlanguage()) {
	        			bsMuteLocal();
	        		}
	        	} else {
	        		micbutton.setSrc("img/micsquare.png");
	        		miclabel.setInnerHTML("Mute");
	        		sipCall.sipPluginUnmute();
	        		if (isBluestreamlanguage()) {
	        			bsUnmuteLocal();
	        		}
	        	}
	        }
	    });
	    
	    DivElement videocontrolbutton = Document.get().getElementById("videocontrolbutton").cast();
		Event.sinkEvents(videocontrolbutton, Event.ONCLICK);
	    Event.setEventListener(videocontrolbutton, new EventListener() {
	        @Override
	        public void onBrowserEvent(Event event) {
	        	ImageElement videocontrolbutton = Document.get().getElementById("videocontrolbutton").cast();
	        	SpanElement videolabel = Document.get().getElementById("videolabel").cast();
	        	if (videocontrolbutton.getSrc().contains("img/videosquare.png")) {
	        		videocontrolbutton.setSrc("img/videoslash.png");
	        		videolabel.setInnerHTML("Unhide");
	        		if (isBluestreamlanguage()) {
	        			bsHideLocal();
	        		} else {
	        			videoRoom.videoRoomUnpublishOwnFeed();
	        		}
	        	} else {
	        		videocontrolbutton.setSrc("img/videosquare.png");
	        		videolabel.setInnerHTML("Hide");
	        		if (isBluestreamlanguage()) {
	        			bsShowLocal();
	        		} else {
	        			videoRoom.videoRoomPublishOwnFeed(false);
	        		}
	        	}
	        }
	    });
	    
	    DivElement remembercheckbox = Document.get().getElementById("remembercheckbox").cast();
	    Event.sinkEvents(remembercheckbox, Event.ONCLICK);
	    Event.setEventListener(remembercheckbox, new EventListener() {
	        @Override
	        public void onBrowserEvent(Event event) {
	        	DivElement remembercheckbox = Document.get().getElementById("remembercheckbox").cast();
	        	if (remembercheckbox.hasClassName("fa-check-square")) {
	        		remembercheckbox.removeClassName("fa-check-square");
	        		remembercheckbox.addClassName("fa-square");
	        	} else {
	        		remembercheckbox.removeClassName("fa-square");
	        		remembercheckbox.addClassName("fa-check-square");
	        	}
	        }
	    });
	    
	    Window.addResizeHandler(new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				resizeContent();
			}
	    });
	    
	    videoService.getUserInfoByToken(sessionId, "", new AsyncCallback<UserInfo>() {
			@Override
			public void onFailure(Throwable caught) {
				error(caught);
			}
			@Override
			public void onSuccess(UserInfo result) {
				userInfo = result;
				DivElement marketingtext = Document.get().getElementById("marketingtext").cast();
				marketingtext.setInnerText(userInfo.marketingInfo);
				if (userInfo.accessCode != null) {
					showQuestionsPage();
				} else {
					setPage(GUIPage.loginpage);
				}
			}
		});
	}
	
	public void initWithUsernamePassword(String username, String password, boolean rememberme) {
		videoService.getUserInfoByEmailPassword(sessionId, username, password, rememberme, new AsyncCallback<UserInfo>() {
			@Override
			public void onFailure(Throwable caught) {
				error(caught);
			}
			@Override
			public void onSuccess(UserInfo result) {
				userInfo = result;
				DivElement marketingtext = Document.get().getElementById("marketingtext").cast();
				marketingtext.setInnerText(userInfo.marketingInfo);
				showQuestionsPage();
			}
		});
	}
	
	public void showQuestionsPage() {
		setPage(GUIPage.getinterpreterpage);
		clearQuestions();
		addQuestions();
	}
	
	private void clearQuestions() {
		DivElement questions = Document.get().getElementById("questions").cast();
		for (int i=questions.getChildCount()-1; i>1; i--) {
			DivElement question = questions.getChild(i).cast();
			if (question != null && question.hasClassName("question")) {
				question.removeFromParent();
			}
		}
	}
	
	private DivElement createQuestion(String id, String label, String placeholder) {
		DivElement divElement = Document.get().createDivElement();
		divElement.setClassName("question");
		divElement.setInnerHTML("<input id=\""+id+"\" type=\"text\" placeholder=\""+label+"\">");
		return divElement;
	}
	
	private void addQuestions() {
		DivElement questions = Document.get().getElementById("questions").cast();
		if (userInfo.deptLabel != null) {
			DivElement question = createQuestion("deptcode", userInfo.deptLabel, userInfo.deptQuestion);
			questions.appendChild(question);
			if (userInfo.deptCode != null) { //hide and prepopulate
				question.getStyle().setDisplay(Display.NONE);
				InputElement deptCodeInput = Document.get().getElementById("deptcode").cast();
				deptCodeInput.setValue(userInfo.deptCode);
			}
		}
		for (int i = userInfo.questionId.size()-1; i>=0; i--) {
			String questionId = userInfo.questionId.get(i);
			String questionLabel = userInfo.questionLabel.get(i);
			String questionPlaceholder = userInfo.questionPlaceholder.get(i);
			DivElement question = createQuestion("question"+questionId, questionLabel, questionPlaceholder);
			questions.appendChild(question);
		}
	}
	
	private List<String> getQuestionInputs() {
		List<String> questionInputs = new ArrayList<String>();
		for (int i=0; i<userInfo.questionId.size();i++) {
			InputElement question = Document.get().getElementById("question"+i).cast();
			if (question != null) questionInputs.add(question.getValue());
		}
		return questionInputs;
	}
	
	private void clearQuestionInputs() {
		for (int i=0; i<userInfo.questionId.size();i++) {
			InputElement question = Document.get().getElementById("question"+i).cast();
			if (question != null) question.setValue("");
		}
	}
	
	private void startVideoRoom() {
		final Base that = this;
		//if (Websocket.isSupported()) {
		//Window.alert("wss starting");
			if (Window.Location.getProtocol().contains("file")) {
				websocket = new Websocket(/*customerInfo.wsUrl+*/"wss://video.telelanguage.com/ws/video?sid="+sessionId);
				//websocket = new Websocket(/*customerInfo.wsUrl+*/"wss://video-dev.telelanguage.com/ws/video?sid="+sessionId);				
			} else if (Window.Location.getProtocol().contains("https")) {
				websocket = new Websocket(/*customerInfo.wsUrl+*/"wss://"+Window.Location.getHost()+"/ws/video?sid="+sessionId);				
			} else {
				websocket = new Websocket(/*customerInfo.wsUrl+*/"ws://"+Window.Location.getHost()+"/ws/video?sid="+sessionId);
			}
			websocket.addListener(that);
			websocket.open();
		//}
		if (videoRoom != null) {
			videoRoom.videoRoomHangup();
			videoRoom.destroy();
			videoRoom = null;
		}
		if (videoCall) {
			videoRoom = new Janus(new JanusListener() {
				@Override
				public void initSuccess() {
					log("videoRoom initSuccess()");
					videoCustomer.getStyle().setZIndex(3);
					videoRoom.videoRoomPluginAttach(that);
				}
				@Override
				public void error(String reason) {
					log("videoRoom error: "+reason);
				}
				@Override
				public void destroyed() {
					log("videoRoom destroyed");
					//videoRoom = null;
				}
			}, customerInfo.janusApiUrl, customerInfo.turnServer, customerInfo.turnUsername, customerInfo.turnPassword); //"https://janus.icoa.com:8089/janus");
		} else {
			videoCustomer.getStyle().setZIndex(3);
			//videoCustomer.setPoster("https://video1.telelanguage.com/img/audiocall.png");
			redrawVideos();
			startSipCall();
		}
	};
	
	private void startSipCall() {
		info("Call Progressing . . .");
		final Base that = this;
		if (sipCall != null) {
			sipCall.destroy();
			sipCall = null;
		}
		sipCall = new Janus(new JanusListener() {
			@Override
			public void initSuccess() {
				log("sipCall initSuccess()");
				sipCall.sipPluginAttach(new JanusSipListener() {
					@Override
					public void sipPluginSuccess() {
						log("sipPluginSuccess "+customerInfo);
						sipCall.registerSipEndpoint(customerInfo.sipAddress, "299", customerInfo.sipProxy);
					}

					@Override
					public void sipPluginError(String error) {
						log("sipPluginError:"+error);
					}

					@Override
					public void sipPluginConsentDialog(Boolean on) {
						log("sipPluginConsentDialog:"+on);
					}

					@Override
					public void sipIncomingCall(String username, Boolean audio, Boolean video) {
						log("sipIncomingCall:"+username+","+audio+","+video);
						if (inCall) sipCall.answer();
						else sipCall.hangup();
					}

					@Override
					public void sipRegistered(String username) {
						log("sipRegistered:"+username);
						if (inCall && !connectedYet) {
							SelectElement languageInput = Document.get().getElementById("languageselect").cast();
							String language = "";
							if (languageInput.getSelectedIndex() > 0) {
								language = languageInput.getOptions().getItem(languageInput.getSelectedIndex()).getLabel();
								if ("ASL".equals(language)) language="Sign";
							}
							String gender = "";
							SelectElement genderselect = Document.get().getElementById("genderselect").cast();
							if (genderselect.getSelectedIndex() == 1) gender = "M";
							if (genderselect.getSelectedIndex() == 2) gender = "F";
							List<String> questionsInput = getQuestionInputs();
							String deptCode = null;
							InputElement deptCodeInput = Document.get().getElementById("deptcode").cast();
							if (deptCodeInput != null) deptCode = deptCodeInput.getValue();
							videoService.call(sessionId, customerInfo.room, userInfo.accessCode, language, gender, videoCall, questionsInput, deptCode, new AsyncCallback<Void>() {
								@Override
								public void onSuccess(Void result) {
	//								bitrateTimer.schedule(1000);
								}
								@Override
								public void onFailure(Throwable caught) {
									info("Call failed: "+caught.getMessage());
								}
							});
						}
					}

					@Override
					public void sipHangup(String username, String reason) {
						log("sipHangup:"+username+","+reason);
						inCall = false;
						if (!isBluestreamCall)
						info(" Call Disconnected ");
						that.hangupCall();
//						if (framework7 != null) framework7.back();
					}

					@Override
					public void sipAccepted(String username, String reason) {
						log("sipAccepted:"+username+","+reason);
//						callTitle.setInnerText(interpreterLanguage+" Call");
						//sipCall.sipPluginRecord(callId);
						canStartRecording = true;
					}

					@Override
					public void sipDetached() {
						log("sipDetached");
					}

					@Override
					public void sipRegistrationFailed(String reason) {
						log("sipRegistrationFailed:"+reason);
					}
				});
			}
			@Override
			public void error(String reason) {
				log("sipCall error: "+reason);
			}
			@Override
			public void destroyed() {
				log("sipCall destroyed");
				//sipCall = null;
			}
		}, customerInfo.janusApiUrl, customerInfo.turnServer, customerInfo.turnUsername, customerInfo.turnPassword);
	}
	
	private void customerHangup() {
		String callSessionId = getDataValue(lastData, "callSessionId");
		videoService.hangup(sessionId, callSessionId, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				error(caught);
				hangupCall();
			}
			@Override
			public void onSuccess(Void result) {
				hangupCall();
			}
		});
	}
	
	private void hangupCall() {
		log("videoroom.js destroy");
		if (videoRoom != null) {
			videoRoom.videoRoomHangup();
			videoRoom.destroy();
		}
		if (sipCall != null) {
			sipCall.hangup();
			sipCall.destroy();
		}
		if (!isBluestreamCall) {
			inCall = false;
			websocket.close();
			stopInsomnia();
			videoCustomer.getStyle().setZIndex(-1);
			videoInterpreter.getStyle().setZIndex(-1);
			redrawVideos();
			reloadPageTimer.schedule(1500);
		}
	}
	
	private String getDataValue(String data, String string) {
		if (data.indexOf(string+"=") > -1) {
			String value = data.substring(data.indexOf(string+"=")+string.length()+1);
			if (value.indexOf(",")> -1) 
				value = value.substring(0, value.indexOf(","));
			else
				value = value.substring(0, value.indexOf("}"));
			return value;
		}
		return "";
	}
	
	String lastData="";
	
	public void processMessages(List<String> messages) {
		for (Object message: messages) {
			String msg = (String) message;
			if (msg.indexOf(":")==-1) {
				GWT.log("onMessage (NOT IMPLEMENTED): "+msg);
				return;
			}
			String type = msg.substring(0, msg.indexOf(":"));
			String data = msg.substring(msg.indexOf(":")+1);
			GWT.log("onMessage: "+type+" data: "+data);
			if ((callId == null || callId.equals(""))
				&& !"".equals(getDataValue(data, "callId"))) {
				callId = getDataValue(data, "callId");
			}
			if (!recordingStarted && canStartRecording && callId != null && !callId.equals("")) {
				recordingStarted = true;
				sipCall.sipPluginRecord(callId);
			}
			switch(VideoMessages.valueOf(type)) {
			case status:  
			case onhold:
			case offhold:
			case statusChanged:
				lastData = data;
				setGUI(data);
			case callIncoming:
			case askToAcceptCall:
				//callId = getDataValue(data, "callId");
				break;
			case disconnect:
				hangupCall();
				break;
			case playvideo:
				{
					ImageElement videocontrolbutton = Document.get().getElementById("videocontrolbutton").cast();
					videocontrolbutton.setSrc("img/videosquare.png");
				}
				videoRoom.videoRoomPublishOwnFeed(false);
				break;
			case pausevideo:
				{
					ImageElement videocontrolbutton = Document.get().getElementById("videocontrolbutton").cast();
					videocontrolbutton.setSrc("img/videoslash.png");
				}
				videoRoom.videoRoomUnpublishOwnFeed();
				break;
			case Logout:
				break;
			case ErrorMessage:
				info(data);
				break;
			case ping:
				websocket.send("ping:"+sessionId);
				videoService.ping(new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						error(caught);
					}
					@Override
					public void onSuccess(Void result) {
					}
				});
				break;
			case SessionIdRequest:
				websocket.send(VideoMessages.SessionIdResponse.toString()+":"+sessionId);
				break;
			case bluestreamInitiateCall:
				isBluestreamCall = true;
				videoService.getBluestreamJWT(new AsyncCallback<String>() {
					@Override
					public void onFailure(Throwable caught) {
						error(caught);
					}
					@Override
					public void onSuccess(String jwtVal) {
						info("Calling . . .");
						//prepBlueStreamCall(jwtVal, "c3aba42e-bdfc-443a-8b8b-f89e01dcbacf");  //test
						prepBlueStreamCall(jwtVal, "4c6f48e2-da00-4e2e-9d40-53c816159226");  //live
					}
				});
				break;
			default: System.out.println("NOT IMPLEMENTED IN CLIENT");
			}
		}
	}
	
	String onHold = null;
	
	private void setGUI(String data) {
		GWT.log("setGUI "+data);
		GUIPage showPage = GUIPage.getinterpreterpage;
		if (inCall) {
			String tempOnHold = getDataValue(data, "onHold");
			if ("true".equals(tempOnHold) || "false".equals(tempOnHold)) onHold = tempOnHold;
			onHold = getDataValue(data, "onHold");
			if (connectedYet && "true".equals(onHold)) {
				showPage = GUIPage.onholdpage;
			} else {
				String interpreterOnCall = getDataValue(data, "interpreterOnCall");
				if ("true".equals(interpreterOnCall)) {
					connectedYet = true;
					String isVideo = getDataValue(data, "isVideo");
					DivElement videocontrolbutton = Document.get().getElementById("videocontrolbutton").cast();
					SpanElement videolabel = Document.get().getElementById("videolabel").cast();
					if (!videoCall || "false".equals(isVideo)) {
						showPage = GUIPage.interpreterpage;
						videocontrolbutton.addClassName("hide");
						videolabel.addClassName("hide");
					} else {
						showPage = GUIPage.videopage;
						videocontrolbutton.removeClassName("hide");
						videolabel.removeClassName("hide");
					}
				} else {
					if (bsConnected) {
						showPage = GUIPage.videopage;
					} else {
						showPage = GUIPage.connectingpage;
					}
				}
			}
			String agentOnCall = getDataValue(data, "agentOnCall");
			if ("true".equals(agentOnCall)) {
				if ("true".equals(onHold)) {
					connectedYet = true;
					showPage = GUIPage.onholdpage;
				} else {
					connectedYet = true;
					showPage = GUIPage.agentpage;
				}
			}
		}
		setPage(showPage);
	}
	
	private void bsRemoteStreamSuccess() {
		bsConnected = true;
		setPage(GUIPage.videopage);
	};
	
	private void bsConnect(String bsCallId, String bsInterpreterId, String bsInterpreterName) {
		setPage(GUIPage.connectingpage);
		// TODO BSTODO send interpreter Connected message to TLVX events
		callSessionId = getDataValue(lastData, "callSessionId");
		callStartTime = new Date().getTime();
		videoService.blueStreamConnected(sessionId, callSessionId, bsCallId, bsInterpreterId, bsInterpreterName, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				error(caught);
			}
			@Override
			public void onSuccess(Void result) {
			}
		});
	}
	
	private void bsDisconnected() {
		isBluestreamCall = false;
		long callDuration = (new Date().getTime() - callStartTime) / 1000;
		videoCustomer.getStyle().setZIndex(-1);
		videoInterpreter.getStyle().setZIndex(-1);
		redrawVideos();
		videoService.blueStreamDisconnected(sessionId, callSessionId, callDuration, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				error(caught);
			}
			@Override
			public void onSuccess(Void result) {
				hangupCall();
			}
		});
	}
	
	private void setPage(GUIPage page) {
		for (GUIPage clearpage : GUIPage.values()) {
			DivElement pagediv = Document.get().getElementById(clearpage.toString()).cast();
			if (pagediv != null) {
				if (clearpage == page) {
					pagediv.removeClassName("hide");
					if (callButtonPages.contains(page)) {
						DivElement callcontrolbuttonsdiv = Document.get().getElementById("callcontrolbuttons").cast();
						pagediv.appendChild(callcontrolbuttonsdiv);
					}
					if (page == GUIPage.getinterpreterpage) {
						clearQuestionInputs();
					}
				} else {
					pagediv.addClassName("hide");
				}
			}
		}
		resizeContent();
		redrawVideos();
	}
	
	private void resizeContent() {
		
//	    DivElement content = Document.get().getElementById("content").cast();
//	    if (!content.hasClassName("hide")) {
//	    	int contentSize = Window.getClientHeight() - content.getAbsoluteTop();
//	    	content.getStyle().setHeight(contentSize, Unit.PX);
//	    }
	    
//	    Window.alert("test");
//	    
//	    DivElement getinterpreterpage = Document.get().getElementById("getinterpreterpage").cast();
//	    if (!getinterpreterpage.hasClassName("hide")) {
//	    	Window.alert("test2");
////	    	DivElement getinterpreterform = Document.get().getElementById("getinterpreterform").cast();
////	    	int contentSize = Window.getClientHeight() - getinterpreterform.getAbsoluteTop();
////	    	getinterpreterform.getStyle().setHeight(contentSize, Unit.PX);
//	    }
	}
	
	public void error(Throwable caught) {
		if (caught instanceof StatusCodeException) {
			StatusCodeException sce = (StatusCodeException) caught;
			if (sce.getStatusCode() == 0) {
				info("Network is unreachable.");
			} else {
				info("Error: "+sce.getStatusCode()+": "+sce.getStatusText());
			}
		} else {
			info(caught.getMessage());
		}
	}

	public native void info(String msg) /*-{
		$wnd.toastr.info(msg);
	}-*/;
	
	private native void iosrtcGlobals(String turnServer, String turnUsername, String turnPassword) /*-{
		$wnd.registerGlobals(turnServer, turnUsername, turnPassword);
	}-*/;
	
	public native void log(String msg) /*-{
		$wnd.console.log(msg);
	}-*/;
	
	private void redrawVideos() {
		redrawVideosNative();
	}
	
	private native void redrawVideosNative() /*-{
		if (typeof($wnd.device) != "undefined" && typeof($wnd.device.platform) != "undefined" && $wnd.device.platform === 'iOS') {
			$wnd.cordova.plugins.iosrtc.refreshVideos();
		}
	}-*/;

	private native void stopInsomnia() /*-{
		if (typeof($wnd.device) != "undefined" && typeof($wnd.device.platform) != "undefined" && $wnd.device.platform === 'iOS') {
			$wnd.plugins.insomnia.allowSleepAgain();
		}
	}-*/;
	
	private native void startinsomnia() /*-{
		if (typeof($wnd.device) != "undefined" && typeof($wnd.device.platform) != "undefined" && $wnd.device.platform === 'iOS') {
			$wnd.plugins.insomnia.keepAwake();
		}
	}-*/;

	@Override
	public void onClose(CloseEvent event) {
		GWT.log("onClose");
	}

	@Override
	public void onMessage(String msg) {
		log("onMessage: "+msg);
		try {
			List<String> messages = new ArrayList<String>();
			messages.add(msg);
			processMessages(messages);
		} catch (Exception e) {
			GWT.log("Error onMessage", e);
		}
	}

	@Override
	public void onOpen() {
		GWT.log("TLVX Video connection opened.");
	}

	@Override
	public void onPause() {
		log("onPause");
	}

	@Override
	public void onResume() {
		log("onResume");
		clearCacheReload();
//		if (Websocket.isSupported()) {
//			websocket = new Websocket(customerInfo.wsUrl+"?sid="+sessionId);
//			websocket.addListener(this);
//			websocket.open();
//		}
	}

	@Override
	public void onOnline() {
		log("onOnline");
		clearCacheReload();
//		if (Websocket.isSupported()) {
//			websocket = new Websocket(customerInfo.wsUrl+"?sid="+sessionId);
//			websocket.addListener(this);
//			websocket.open();
//		}
	}

	@Override
	public void onOffline() {
		log("onOffline");
		info("Lost internet connection.");
		//clearCacheReload();
	}
	
	private native void clearCacheReload() /*-{
		$wnd.location.reload(true);
	}-*/;

	@Override
	public void onPushRegistrationId(String registrationId) {
		log("onPushRegistrationId");
	}

	@Override
	public void onPushNotification(String name, String title, Integer count,
			String sound, String image, String additionalData) {
		log("onPushNotification");
	}

	@Override
	public void onPushError(String error) {
		log("onPushError");
	}

	@Override
	public void onDeviceReady() {
		log("onDeviceReady");
	}
	
	@Override
	public void onRotate() {
		log("onRotate");
		redrawVideos();
	}

	@Override
	public void videoRoomPluginSuccess() {
		log("videoRoomPluginSuccess");
		videoRoom.videoRoomRegisterUsername("customer", "videocustomer", customerInfo.room);
	}

	@Override
	public void videoRoomPluginError(String error) {
		log("videoRoomPluginError: "+error);
	}

	@Override
	public void videoRoomPluginConsentDialog(Boolean on) {
		log("videoRoomPluginConsentDialog: "+on);
	}

	@Override
	public void videoRoomPluginJoinedRoom(String roomId, String myId) {
		log("videoRoomPluginJoinedRoom: "+roomId+", "+myId);
		videoRoom.videoRoomPublishOwnFeed(false);
	}

	@Override
	public void videoRoomNewParticipant(String id, String display) {
		log("videoRoomNewParticipant: "+id+", "+display);
		if ("interpreter".equals(display)) {
			//videoInterpreter.setPoster("img/connecting.gif");
			//videoInterpreter.getStyle().setZIndex(2);
			videoRoom.addNewParticipant(id, display, "videointerpreter", customerInfo.room);
			String callSessionId = getDataValue(lastData, "callSessionId");
			videoService.videoSessionStarted(sessionId, callSessionId, new AsyncCallback<Void>() {
				@Override
				public void onFailure(Throwable caught) {
					error(caught);
				}
				@Override
				public void onSuccess(Void result) {
				}
			});
		}
		if ("agent".equals(display)) {
			//videoInterpreter.getStyle().setZIndex(2);
			videoRoom.addNewParticipant(id, display, "videoagent", customerInfo.room);
		}
		setGUI(lastData);
		redrawVideos();
	}

	@Override
	public void videoRoomSlowlink(String uplink, String nacks) {
		log("videoRoomSlowlink: "+uplink+", "+nacks);
	}

	@Override
	public void videoRoomLocalStream(JavaScriptObject stream) {
		log("videoRoomLocalStream:"+stream);
		if (sipCall == null) {
			startSipCall.schedule(1000);
		}
	}

	@Override
	public void videoRoomOnCleanup() {
		log("videoRoomOnCleanup");
	}
}
