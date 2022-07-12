package com.telelanguage.video.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.VideoElement;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.telelanguage.video.client.websockets.CloseEvent;
import com.telelanguage.video.client.websockets.Websocket;
import com.telelanguage.video.client.websockets.WebsocketListener;

public class Tlvx_video implements EntryPoint, NavBarListener, JanusEchoTestListener, JanusVideoRoomListener, Framework7Listener, CordovaListener, WebsocketListener {

	private final VideoServiceAsync videoService = GWT.create(VideoService.class);
	Framework7 framework7;
	public static Tlvx_video thisTlvxVideo;
	private String sipAddress;
	private Janus videoTest;
	private Janus sipCall;
	private boolean inCall = false;
	private Janus videoRoom;
	private Sounds sounds = new Sounds();
	private String callId;
	private String language;
	UserInfo userInfo;
	
	private String loginToken;
	
	private boolean cordovaDeviceReady = false;
	private boolean initialCall = true;
	private boolean videoPublished = true;
	
	VideoElement sipLocalAudio = Document.get().getElementById("siplocalaudio").cast();
	VideoElement sipRemoteAudio = Document.get().getElementById("sipremoteaudio").cast();
	
	VideoElement videoAgent = Document.get().getElementById("videoagent").cast();
	VideoElement videoCustomer = Document.get().getElementById("videocustomer").cast();
	VideoElement videoInterpreter = Document.get().getElementById("videointerpreter").cast();
	
	VideoElement peerechotestvideo = Document.get().getElementById("peerechotestvideo").cast();
	VideoElement myechotestvideo = Document.get().getElementById("myechotestvideo").cast();

	DivElement bottomleftvideodiv = Document.get().getElementById("divagent").cast();
	
	DivElement callTitle = Document.get().getElementById("callStatus").cast();
	DivElement callcalling = Document.get().getElementById("callcalling").cast();
	
	ButtonElement publishButton = Document.get().getElementById("publishbutton").cast();
	
	boolean ringing = false;
	
	public static String sessionId = UUID.uuid();
	private static CustomerInfo customerInfo;
	private static Date lastHeartbeat = new Date();
	private static Websocket websocket;
	private static Timer heartBeatCheckTimer = new Timer() {
		@Override
		public void run() {
			Date now = new Date();
			long diff = now.getTime() - lastHeartbeat.getTime();
			if (diff > 60000) {
				logout("Ping timeout");
			}
		}
	};
	
	private Timer bitrateTimer = new Timer() {
		@Override
		public void run() {
			String sipBitrate = "N/A";
			if (sipCall != null) sipBitrate = sipCall.getSipBitrate();
			String videoBitrate = "N/A";
			//if (videoRoom != null) videoBitrate = videoRoom.getVideoBitrate();
			GWT.log("Bitrates: "+sipBitrate+" "+videoBitrate);
			bitrateTimer.schedule(1000);
		}
	};
	
	private Timer startSipCall = new Timer() {
		@Override
		public void run() {
			startSipCall();
		}
	};
	
	private Timer startVideoRoom = new Timer() {
		@Override
		public void run() {
			InputElement requireVideoInput = Document.get().getElementById("require_video").cast();
			videoService.getCustomerInfoByToken(sessionId, requireVideoInput.isChecked(), new AsyncCallback<CustomerInfo>() {
				@Override
				public void onSuccess(CustomerInfo result) {
					customerInfo = result;
					iosrtcGlobals(customerInfo.turnServer, customerInfo.turnUsername, customerInfo.turnPassword);
					startVideoRoom();
				}
				@Override
				public void onFailure(Throwable caught) {
					info(caught.getMessage());
				}
			});
		}
	};
	
	public void initWithLoginToken(String token) {
		videoService.getUserInfoByToken(sessionId, loginToken, new AsyncCallback<UserInfo>() {
			@Override
			public void onFailure(Throwable caught) {
				
			}
			@Override
			public void onSuccess(UserInfo result) {
				
			}
		});
	}
	
	public void initWithUsernamePassword(String username, String password, boolean rememberme) {
		videoService.getUserInfoByEmailPassword(sessionId, username, password, false, new AsyncCallback<UserInfo>() {
			@Override
			public void onFailure(Throwable caught) {
				info(caught.getMessage());
			}
			@Override
			public void onSuccess(UserInfo result) {
				framework7.back();
				userInfo = result;
				populateQuestions();
			}
		});
	}
	
	private LIElement createLIElement(String id, String label, String placeholder) {
		LIElement lie = Document.get().createLIElement();
		lie.setInnerHTML("<li class=\"item-content\">"+
						 "		<div class=\"item-inner\">"+
							"			<div class=\"item-title label\">"+label+"</div>"+
							"			<div class=\"item-input\">"+
							"				<input id=\""+id+"\" type=\"text\""+
							"					placeholder=\""+placeholder+"\">"+
							"			</div>"+
							"		</div>"+
							"	</li>");
		return lie;
	}
	
	private List<String> getQuestionInputs() {
		List<String> questionInputs = new ArrayList<String>();
		for (int i=0; i<userInfo.questionId.size();i++) {
			InputElement question = Document.get().getElementById("question"+i).cast();
			questionInputs.add(question.getValue());
		}
		return questionInputs;
	}
	
	private void populateQuestions() {
		Document.get().getElementById("loginmenu").getStyle().setDisplay(Display.NONE);
		Document.get().getElementById("videotestli").getStyle().clearDisplay();
		InputElement accesscode = Document.get().getElementById("accesscode").cast();
		accesscode.setValue(userInfo.accessCode);
		LIElement language = Document.get().getElementById("languagemenuitem").cast();
		language.getStyle().clearDisplay();
		for (int i = userInfo.questionId.size()-1; i>=0; i--) {
			String questionId = userInfo.questionId.get(i);
			String questionLabel = userInfo.questionLabel.get(i);
			String questionPlaceholder = userInfo.questionPlaceholder.get(i);
			LIElement question = createLIElement("question"+questionId, questionLabel, questionPlaceholder);
			language.getParentElement().insertAfter(question, language);
		}
		if (userInfo.deptLabel != null) {
			LIElement question = createLIElement("deptcode", userInfo.deptLabel, userInfo.deptQuestion);
			language.getParentElement().insertAfter(question, language);
			if (userInfo.deptCode != null) { //hide and prepopulate
				question.getStyle().setDisplay(Display.NONE);
				InputElement deptCodeInput = Document.get().getElementById("deptcode").cast();
				deptCodeInput.setValue(userInfo.deptCode);
			}
		}
		Document.get().getElementById("callinterpreterli").getStyle().clearDisplay();
		Document.get().getElementById("require_video_li").getStyle().clearDisplay();
	}

	public void onModuleLoad() {
		Tlvx_video.thisTlvxVideo = this;
		NavBar.initUserContext(this);
		Cordova.initCordova(Tlvx_video.thisTlvxVideo);
		
		framework7 = new Framework7(this);
		framework7.initDropdown("language", "Aderi  [Ethiopian],Afghani,African Creole,Afrikaans,Akan,Akateko,Albanian,Amharic,Arabic,arabic juba,Armenian,Ashanh,Assyrian,Azeri (Turkish),Badini,Bajuni,Balochi,Bambara,Bangangte,Bantu,Basque,Bengali/Bangla,Bhutanese,Bulgarian,Burmese,Buryat,Cambodian,Cantonese,Cape Verdian,Carolinean,Cebuano,Chaldean,Chinese Hakka,Chinese Shanghaiese,Chinese Sichuan,Chinese Taiwanese,Chinese Toisanese,Chinn,Chiu-Chow,Choujo,Chuj,Chuukese,Cree,Croatian,Czech,Danish,Dari,Daula Fanti,Dinka,Dutch,Dyula,Dzongkha,Eritrean,Estonian,Ewe,Fanti,Farsi,Fijian,Finnish,Flemish,French,French Canadian,French Creole,Fukienes (Chinese),Fulani,Fuqing,Ga,Georgian,German,Gheg,Greek,Gujarati,Hakka Chinn,Hausa,Hebrew,Hindi,Hmong,Hokkien,Hunan,Hungarian,Ibo,Icelandic,Ilocano (Filipino),Indian,Indonesian,Italian,Japanese,Jawi,Jola,Kabye,Kachin,Kanjobal,Kanjoval [Myan],Kannada,Kaqchikel,Karen,Karenni,Kaya,Kazak,Kinyarwanda,Kirundi,Kiswahili,Korean,Krahn,Kunama,Kurdish,Kurmanji (Kurdish),Lao,Latvian,Lingala,Lithuanian,Luganda,Luo,Macedonian,Malayalam,Malaysian,Malinke,Maltese,Mam [Myam],Mandarin,Mandingo,Mandinka,Marathi (Indian),Marshalese,Masalit,Mashi,May May,Mende,Micromesian Pingelapese,Micronesian Kosrae,Micronesian Pohnpei,Mien,Mina (Togolese),Mirpuri,Mixteco Alto,Mixteco Bajo,Moldovan,Mongolian,Moore,Moreh,Mortlockese,Navajo Indian,Ndebele,Nepali,Norwegian,Nuer,Oriya,Oromo,Pakistani,Palau,Pan Pango,Pangasina (Filipino),Pashtu,Pokomchi,Polish,Portuguese,Portuguese Creole,Pulaar,Punjabi,Quiche,Romanian,Russian,Samoan,Sango,Serbian,Shona,Sichuomese,Sign,Singhalese,Slovak,Slovenian,Somali,Soninke,Sorani,Sosso,Spanish,Swahili,Swedish,Sylheti,Tagalog,Taiwanese,Tajik,Tamil,Tatar,Telugu,Temene,Thai,Tibetan,Tidem Chinn,Tigre,Tigrinya,Tongan,Trique,Tshiluba,Turkish,Turkman,Twi,Ukrainian,Urdu,Uzbeck,Video Arabic,Video Cantonese,Video French Creole,Video German,Video Korean,Video Mandarin,Video Nepali,Video Polish,Video Portuguese,Video Russian,Video Somali,Video Spanish,Video Ukrainian,Video Vietnamese,Vietnamese,Visayan (Filipino),Welsh,Wolof,Yiddish,Yoruba,Zomi,Zulu");

		if (Cordova.noCordova() || (Cordova.deviceReadyCalled && sipCall == null)) {
			Document.get().getElementById("videotestli").removeClassName("disabled");
			Document.get().getElementById("callinterpreterli").removeClassName("disabled");
			
			if (loginToken != null) {
				initWithLoginToken(loginToken);
			} else {
				Document.get().getElementById("loginmenu").getStyle().clearDisplay();
				framework7.open("myaccount");
			}
		}
		
		ButtonElement loginButton = Document.get().getElementById("loginbutton").cast();
		Event.sinkEvents(loginButton, Event.ONCLICK);
	    Event.setEventListener(loginButton, new EventListener() {
	        @Override
	        public void onBrowserEvent(Event event) {
	        	InputElement loginUsername = Document.get().getElementById("loginusername").cast();
	        	InputElement loginPassword = Document.get().getElementById("loginpassword").cast();
	        	if (loginUsername != null && loginPassword != null && loginUsername.getValue().length()>3 && loginPassword.getValue().length()>3) {
	        		initWithUsernamePassword(loginUsername.getValue(), loginPassword.getValue(), false);
	        	} else {
	        		info("You must enter your username and password.");
	        	}
	        }
	    });		
		
		ButtonElement clearCache = Document.get().getElementById("clearCache").cast();
		Event.sinkEvents(clearCache, Event.ONCLICK);
	    Event.setEventListener(clearCache, new EventListener() {
	        @Override
	        public void onBrowserEvent(Event event) {
	        	clearCacheReload();
	        }
	    });
		
		ButtonElement buttonElement = Document.get().getElementById("fullScreenButton").cast();
		Event.sinkEvents(buttonElement, Event.ONCLICK);
	    Event.setEventListener(buttonElement, new EventListener() {
	        @Override
	        public void onBrowserEvent(Event event) {
	        	requestFullScreen();
	        }
	    });
	    
		Event.sinkEvents(publishButton, Event.ONCLICK);
	    Event.setEventListener(publishButton, new EventListener() {
	        @Override
	        public void onBrowserEvent(Event event) {
	        	publishUnpublishMyVideo();
	        }
	    });
	    
	    if (!"firefox".equals(getAdapterBrowser())) {
	    	//framework7.showPopup(".popup-firefox");
	    }
	    
	    peerechotestvideo.getStyle().setProperty("maxWidth", "100%");
	    peerechotestvideo.getStyle().setProperty("maxHeight", "100%");
	    peerechotestvideo.getStyle().setProperty("margin", "auto");
	    peerechotestvideo.getStyle().setProperty("position", "absolute");
	    peerechotestvideo.getStyle().setProperty("marginTop", "44px");	    
	    peerechotestvideo.getStyle().setProperty("top", "0");
	    peerechotestvideo.getStyle().setProperty("left", "0");
	    peerechotestvideo.getStyle().setProperty("right", "0");
	    peerechotestvideo.getStyle().setProperty("bottom", "0");
	    
	    videoInterpreter.getStyle().setProperty("maxWidth", "100%");
	    videoInterpreter.getStyle().setProperty("maxHeight", "100%");
	    videoInterpreter.getStyle().setProperty("margin", "auto");
	    videoInterpreter.getStyle().setProperty("position", "absolute");
	    videoInterpreter.getStyle().setProperty("marginTop", "44px");	    
	    videoInterpreter.getStyle().setProperty("top", "0");
	    videoInterpreter.getStyle().setProperty("left", "0");
	    videoInterpreter.getStyle().setProperty("right", "0");
	    videoInterpreter.getStyle().setProperty("bottom", "0");
	    
	    DivElement video_contain = videoInterpreter.getParentElement().cast();
	    video_contain.getStyle().setProperty("top", "0");
	    video_contain.getStyle().setProperty("left", "0");
	    video_contain.getStyle().setProperty("width", "100%");
	    /* position: absolute;
top: 0;
width: 100%;
height: 200%;
overflow: hidden !important; */
	    
	    if (Document.get().getElementById("videotestbutton") != null) {
		    ButtonElement testVideoButton = Document.get().getElementById("videotestbutton").cast();
			Event.sinkEvents(testVideoButton, Event.ONCLICK);
		    Event.setEventListener(testVideoButton, new EventListener() {
		        @Override
		        public void onBrowserEvent(Event event) {
		        	startEchoTest();
		        }
		    });
		    
		    ButtonElement callButton = Document.get().getElementById("callbutton").cast();
			Event.sinkEvents(callButton, Event.ONCLICK);
		    Event.setEventListener(callButton, new EventListener() {
		        @Override
		        public void onBrowserEvent(Event event) {
					log("videoroom.js init");
					inCall=true;
					ringing = true;
					videoAgent.setAutoplay(true);
					videoCustomer.setAutoplay(true);
					videoInterpreter.setAutoplay(true);
					startVideoRoom.schedule(1000);
		        }
		    });
	    }
	}

	public native String getAdapterBrowser() /*-{
		var isOpera = (!!window.opr && !!opr.addons) || !!window.opera || navigator.userAgent.indexOf(' OPR/') >= 0;
		var isFirefox = typeof InstallTrigger !== 'undefined';
		var isSafari = Object.prototype.toString.call(window.HTMLElement).indexOf('Constructor') > 0 || (function (p) { return p.toString() === "[object SafariRemoteNotification]"; })(!window['safari'] || safari.pushNotification);
		//var isEdge = !isIE && !!window.StyleMedia;
		var isChrome = !!window.chrome && !!window.chrome.webstore;
		var isBlink = (isChrome || isOpera) && !!window.CSS;
		if (isFirefox) return 'firefox';
		if (isChrome) return 'chrome';
	}-*/;
	
	private native void clearCacheReload() /*-{
		$wnd.location.reload(true);
	}-*/;
	
	private native void requestFullScreen() /*-{
	  var element = $doc.documentElement;

	  if (element.requestFullscreen) {
	    element.requestFullscreen();
	  } else if (element.mozRequestFullScreen) {
	    element.mozRequestFullScreen();
	  } else if (element.webkitRequestFullscreen) {
	    element.webkitRequestFullscreen();
	  } else if (element.msRequestFullscreen) {
	    element.msRequestFullscreen();
	  }
	}-*/;
	
	public static native void log(String msg) /*-{
		$wnd.console.log(msg);
	}-*/;
	
	/*
		Workflow:
		1) Call Interpreter
		2) Start Calling Sound
		3) Register SIP Endpoint
		4) Notify TLVX new call
		5) Answer SIP Call/Audio connected
		6) Join Video Room
	 */
	
	@Override
	public void pageChange(String toPage, String fromPage) {
		log("pageChange: "+toPage+" <- "+fromPage);
		switch(toPage){
		case "#videotest": 
			log("echotest.js init");
			peerechotestvideo.getStyle().setZIndex(2);
			myechotestvideo.getStyle().setZIndex(3);
			redrawVideos();
			startEchoTest();
			break;
		case "#call":
			log("videoroom.js init");
			inCall=true;
			ringing = true;
			videoAgent.setAutoplay(true);
			//videoAgent.setAutoplay(true);
			videoCustomer.setAutoplay(true);
			//videoCustomer.setAutoplay(true);
			videoInterpreter.setAutoplay(true);
			//videoInterpreter.setAutoplay(true);
			startinsomnia();
			startVideoRoom.schedule(1000);
//			sounds.play("default-initial.wav", new AsyncCallback<Void>() {
//				@Override
//				public void onFailure(Throwable caught) {
//				}
//				@Override
//				public void onSuccess(Void result) {
//					startVideoRoom.schedule(1000);
//				}
//			});
			//videoAgent.getStyle().setZIndex(3);
			//videoCustomer.getStyle().setZIndex(3);
			//videoInterpreter.getStyle().setZIndex(2);
			redrawVideos();
			break;
		default: log("no action");
		}
		
		switch(fromPage){
		case "#videotest": 
			log("echotest.js destroy");
			peerechotestvideo.getStyle().setZIndex(-1);
			myechotestvideo.getStyle().setZIndex(-1);
			redrawVideos();
			stopEchoTest();
			//Window.Location.reload();
			reloadTrue();
			break;
		case "#call":
			log("videoroom.js destroy");
			if (videoRoom != null) {
				videoRoom.destroy();
			}
			if (sipCall != null) {
				sipCall.destroy();
			}
			inCall = false;
			websocket.close();
			stopInsomnia();
			videoAgent.getStyle().setZIndex(-1);
			videoCustomer.getStyle().setZIndex(-1);
			videoInterpreter.getStyle().setZIndex(-1);
			callcalling.getStyle().clearDisplay();
			Document.get().getElementById("callcalling").getStyle().clearDisplay();
			videoInterpreter.setPoster("");
			redrawVideos();
//			videoAgent.pause();
//			videoCustomer.pause();
//			videoInterpreter.pause();
//			sounds.stop();
			//Window.Location.reload(true);
			reloadTrue();
			break;
		default: log("no action");
		}
	}
	
	private void replaceVideos() {
		DivElement divagent = videoAgent.getParentElement().cast();
		divagent.removeAllChildren();
		divagent.setInnerHTML("<video class=\"rounded centered videoleft\" id=\"videoagent\" style=\"z-index: -3;\" autoplay muted=\"muted\"/>");
		DivElement divcustomer = videoCustomer.getParentElement().cast();
		divcustomer.removeAllChildren();
		divcustomer.setInnerHTML("<video class=\"rounded centered\" id=\"videocustomer\" style=\"z-index: -3; position:absolute; bottom: 20px; right: 20px;\" width=\"20%\" height=\"20%\" autoplay muted=\"muted\"/>");
		DivElement divInterpreter = videoInterpreter.getParentElement().cast();
		divInterpreter.removeAllChildren();
		divInterpreter.setInnerHTML("<video class=\"video\" id=\"videointerpreter\" style=\"z-index: -3;\" muted=\"muted\" autoplay/>");
		videoAgent = Document.get().getElementById("videoagent").cast();
		videoCustomer = Document.get().getElementById("videocustomer").cast();
		videoInterpreter = Document.get().getElementById("videointerpreter").cast();

	}

	private native void reloadTrue() /*-{
		if (typeof ($wnd.device) != "undefined"
				&& typeof ($wnd.device.platform) != "undefined"
				&& $wnd.device.platform === 'iOS') {
		} else {
			$wnd.location.reload(true);
		}
	}-*/;
	
	private native void redrawVideos() /*-{
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
	
	private void startVideoRoom() {
		final Tlvx_video that = this;
		if (Websocket.isSupported()) {
			if (Window.Location.getProtocol().contains("file")) {
				websocket = new Websocket(/*customerInfo.wsUrl+*/"wss://video1.telelanguage.com/ws/video?sid="+sessionId);				
			} else if (Window.Location.getProtocol().contains("https")) {
				websocket = new Websocket(/*customerInfo.wsUrl+*/"wss://"+Window.Location.getHost()+"/ws/video?sid="+sessionId);				
			} else {
				websocket = new Websocket(/*customerInfo.wsUrl+*/"ws://"+Window.Location.getHost()+"/ws/video?sid="+sessionId);
			}
			websocket.addListener(Tlvx_video.thisTlvxVideo);
			websocket.open();
		}
		// TODO check for video on
		InputElement requireVideoInput = Document.get().getElementById("require_video").cast();
		if (requireVideoInput.isChecked()) {
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
					videoRoom = null;
					//Document.get().getElementById("callcalling").getStyle().clearDisplay();
				}
			}, customerInfo.janusApiUrl, customerInfo.turnServer, customerInfo.turnUsername, customerInfo.turnPassword); //"https://janus.icoa.com:8089/janus");
		} else {
			videoCustomer.getStyle().setZIndex(3);
			videoCustomer.setPoster("https://video1.telelanguage.com/img/audiocall.png");
			redrawVideos();
			startSipCall();
		}
	};
	
	private void publishUnpublishMyVideo() {
		if (videoRoom != null) {
			if (videoPublished) {
				videoRoom.videoRoomUnpublishOwnFeed();
				videoPublished = false;
				publishButton.setInnerText("Show My Video");
			} else {
				videoRoom.videoRoomPublishOwnFeed(false);
				videoPublished = true;
				publishButton.setInnerText("Hide My Video");
			}
		}
	}
	
	private void startEchoTest() {
		final Tlvx_video that = this;
		videoService.getCustomerInfoByToken(sessionId, false, new AsyncCallback<CustomerInfo>() {
			@Override
			public void onSuccess(CustomerInfo result) {
				customerInfo = result;
				iosrtcGlobals(customerInfo.turnServer, customerInfo.turnUsername, customerInfo.turnPassword);
				videoTest = new Janus(new JanusListener() {
					@Override
					public void initSuccess() {
						log("videoTest initSuccess()");
						videoTest.echoPluginAttach(that);
					}
					@Override
					public void error(String reason) {
						log("videoTest error: "+reason);
					}
					@Override
					public void destroyed() {
						log("videoTest destroyed");
						videoTest = null;
					}
				}, customerInfo.janusApiUrl, customerInfo.turnServer, customerInfo.turnUsername, customerInfo.turnPassword); //, "https://janus.icoa.com:8089/janus");
			}
			@Override
			public void onFailure(Throwable caught) {
				info(caught.getMessage());
			}
		});
	};
	
	private void stopEchoTest() {
		videoTest.destroy();
	};

	@Override
	public void navBarLoaded(boolean loggedIn) {
	}


	@Override
	public void echoPluginSuccess() {
		log("echoPluginSuccess:");
		videoTest.echoStartTest();
	}

	@Override
	public void echoPluginError(String error) {
		log("echoPluginError:"+error);
	}

	@Override
	public void echoPluginConsentDialog(Boolean on) {
		log("echoPluginConsentDialog:"+on);
	}

	@Override
	public void echoPluginDone() {
		log("echoPluginDone");
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
//			websocket.addListener(Tlvx_video.thisTlvxVideo);
//			websocket.open();
//		}
//		heartBeatCheckTimer.schedule(60000);
	}

	@Override
	public void onOnline() {
		log("onOnline");
		clearCacheReload();
//		if (Websocket.isSupported()) {
//			websocket = new Websocket(customerInfo.wsUrl+"?sid="+sessionId);
//			websocket.addListener(Tlvx_video.thisTlvxVideo);
//			websocket.open();
//		}
//		heartBeatCheckTimer.schedule(60000);
	}

	@Override
	public void onOffline() {
		log("onOffline");
		// Create offline page instead?
		clearCacheReload();
	}

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

	private native void iosrtcGlobals(String turnServer, String turnUsername, String turnPassword) /*-{
		$wnd.registerGlobals(turnServer, turnUsername, turnPassword);
	}-*/;
	
	private void startSipCall() {
		info("Call Progressing . . .");
		final Tlvx_video that = this;
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
						InputElement accessCodeInput = Document.get().getElementById("accesscode").cast();
						InputElement languageInput = Document.get().getElementById("language").cast();
						InputElement requireVideoInput = Document.get().getElementById("require_video").cast();
						List<String> questionsInput = getQuestionInputs();
						String deptCode = null;
						InputElement deptCodeInput = Document.get().getElementById("deptcode").cast();
						if (deptCodeInput != null) deptCode = deptCodeInput.getValue();
						videoService.call(sessionId, customerInfo.room, accessCodeInput.getValue(), languageInput.getValue(), "", requireVideoInput.isChecked(), questionsInput, deptCode, new AsyncCallback<Void>() {
							@Override
							public void onSuccess(Void result) {
								bitrateTimer.schedule(1000);
							}
							@Override
							public void onFailure(Throwable caught) {
								info("Call failed: "+caught.getMessage());
							}
						});
					}

					@Override
					public void sipHangup(String username, String reason) {
						log("sipHangup:"+username+","+reason);
						inCall = false;
						info(" Call Disconnected ");
						if (framework7 != null) framework7.back();
					}

					@Override
					public void sipAccepted(String username, String reason) {
						log("sipAccepted:"+username+","+reason);
						callTitle.setInnerText(interpreterLanguage+" Call");
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
				sipCall = null;
			}
		}, customerInfo.janusApiUrl, customerInfo.turnServer, customerInfo.turnUsername, customerInfo.turnPassword);
	}
	
	@Override
	public void onDeviceReady() {
		//iosrtcGlobals();
	}

	@Override
	public void videoRoomPluginSuccess() {
		log("videoRoomPluginSuccess");
		videoRoom.videoRoomRegisterUsername("customer", "videocustomer", customerInfo.room);
	}
	
	public void videoRoomLocalStream(JavaScriptObject stream) {
		log("videoRoomLocalStream:"+stream);
		if (sipCall == null) {
			startSipCall.schedule(1000);
		}
	};

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
			Document.get().getElementById("callcalling").getStyle().setDisplay(Display.NONE);
			videoInterpreter.setPoster("img/connecting.gif");
			videoInterpreter.getStyle().setZIndex(2);
			videoRoom.addNewParticipant(id, display, "videointerpreter", customerInfo.room);

		}
		if ("agent".equals(display)) {
			Document.get().getElementById("callcalling").getStyle().setDisplay(Display.NONE);
			videoInterpreter.getStyle().setZIndex(2);
			videoRoom.addNewParticipant(id, display, "videoagent", customerInfo.room);
		}
		redrawVideos();
	}
	
	@Override
	public void videoRoomSlowlink(String uplink, String nacks) {
		log("videoRoomSlowlink: "+uplink+", "+nacks);
	}
	
	private static void logout(String string) {
		
	}

	@Override
	public void onClose(CloseEvent event) {
		GWT.log("onClose");
		try {
			logout("Connection Lost");
		} catch (Exception e) {
			GWT.log("Error onClose", e);
		}
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
	
	private void setPosters(String data) {
		GWT.log("setPosters "+data);
		String agentOnCall = getDataValue(data, "agentOnCall");
		if ("true".equals(agentOnCall)) {
			videoAgent.setAttribute("poster", "img/teleagent.png");
		} else {
			if (videoAgent.hasAttribute("poster")) videoAgent.removeAttribute("poster");
		}
		String interpreterOnCall = getDataValue(data, "interpreterOnCall");
		if ("true".equals(interpreterOnCall)) {
			videoInterpreter.setAttribute("poster", "img/teleinterpreter.png");
		} else {
			if (videoInterpreter.hasAttribute("poster")) videoInterpreter.removeAttribute("poster");
		}
		redrawVideos();
	}
	
	String interpreterLanguage = "";
	
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
			switch(VideoMessages.valueOf(type)) {
			case status:  
				language = getDataValue(data, "language");
				if (language != null && !language.equals("null")) interpreterLanguage = language+" ";
				if ("true".equals(getDataValue(data, "onHold"))) {
					callTitle.setInnerText(interpreterLanguage+" On Hold");
				} else {
					callTitle.setInnerText(interpreterLanguage+" Call");
				}
				break;
			case onhold:
				callId = getDataValue(data, "callId");
				language = getDataValue(data, "language");
				if (language != null && !language.equals("null")) interpreterLanguage = language+" ";
				interpreterHoldOverlay(true);
				setPosters(data);
//				if (!ringing) {
//				sounds.play("default-hold.wav", new AsyncCallback<Void>() {
//					@Override
//					public void onSuccess(Void result) {
////						if (videoRoom == null) {
////							startVideoRoom();
////						}
//					}
//					@Override
//					public void onFailure(Throwable caught) {
//					}
//				});
//				}
				callTitle.setInnerText(interpreterLanguage+" On Hold");
				callcalling.getStyle().setDisplay(Display.NONE);
				break;
			case offhold:
				ringing = false;
				callId = getDataValue(data, "callId");
				language = getDataValue(data, "language");
				if (language != null && !language.equals("null")) interpreterLanguage = language+" ";
				interpreterHoldOverlay(false);
				setPosters(data);
//				sounds.play("silence.wav", new AsyncCallback<Void>() {
//					@Override
//					public void onSuccess(Void result) {
////						if (videoRoom == null) {
////							startVideoRoom();
////						}
//						sipCall.sipSpeakerOutput();
//					}
//					@Override
//					public void onFailure(Throwable caught) {
//					}
//				});
				//Document.get().getElementById("callcalling").getStyle().setDisplay(Display.NONE);
//				if (videoRoom == null) {
//					startVideoRoom();
//				}
				callTitle.setInnerText(interpreterLanguage+" Call");
				callcalling.getStyle().setDisplay(Display.NONE);
				break;
			case callIncoming:
				break;
			case askToAcceptCall:
				break;
			case disconnect:
				inCall=false;
				ringing = true;
				sipCall.hangup();
				callTitle.setInnerText("Disconnected");
				break;
			case statusChanged:
				setPosters(data);
				break;
			case playvideo:
				videoRoom.videoRoomPublishOwnFeed(false);
				videoPublished = true;
				publishButton.setInnerText("Hide My Video");
				break;
			case pausevideo:
				videoRoom.videoRoomUnpublishOwnFeed();
				videoPublished = false;
				publishButton.setInnerText("Show My Video");
				break;
			case Logout:
				logout(data);
				break;
			case ErrorMessage:
				info(data);
				break;
			case ping:
				websocket.send("ping:"+sessionId);
				lastHeartbeat = new Date();
				heartBeatCheckTimer.schedule(60000);
				break;
			case SessionIdRequest:
				websocket.send(VideoMessages.SessionIdResponse.toString()+":"+sessionId);
				break;
			default: System.out.println("NOT IMPLEMENTED IN CLIENT");
			}
		}
	}
	
	private void interpreterHoldOverlay(boolean onHold) {
		//Window.alert("interpreterHoldOverlay: "+onHold);
		VideoElement videoInterpreter = Document.get().getElementById("videointerpreter").cast();
		VideoElement videoInterpreterOverlay = Document.get().getElementById("videointerpreteroverlay").cast();
		DivElement viParent = videoInterpreter.getParentElement().cast();
		if (onHold) {
			if (videoInterpreterOverlay == null) {
				videoInterpreterOverlay = Document.get().createVideoElement();
				videoInterpreterOverlay.setId("videointerpreteroverlay");
				videoInterpreterOverlay.setPoster("https://video1.telelanguage.com/img/onhold.gif");
				videoInterpreterOverlay.setAttribute("style", videoInterpreter.getAttribute("style"));
				viParent.insertFirst(videoInterpreterOverlay);
				videoInterpreter.getStyle().setZIndex(-3);
				videoInterpreter.getStyle().setDisplay(Display.NONE);
				redrawVideos();
			}
		} else {
			if (videoInterpreterOverlay != null) {
				videoInterpreterOverlay.removeFromParent();
				videoInterpreter.getStyle().setZIndex(2);
				videoInterpreter.getStyle().clearDisplay();
				redrawVideos();
			}
		}
	}
	
	public native void observeInterpreter() /*-{
		if (typeof($wnd.device) != "undefined" && typeof($wnd.device.platform) != "undefined" && $wnd.device.platform === 'iOS') {
			var videointerpreter = $doc.getElementById('videointerpreter');
			$wnd.cordova.plugins.iosrtc.observeVideo(videointerpreter);
		}
	}-*/;
	
	private void info(String data) {
		if (framework7 != null) framework7.showToast("<p>"+data+"</p>");
		else GWT.log("INFO: "+data);
	}

	private static String getDataValue(String data, String string) {
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

	@Override
	public void videoRoomOnCleanup() {
		
	}

	@Override
	public void onRotate() {
		// TODO Auto-generated method stub
		
	}
}
