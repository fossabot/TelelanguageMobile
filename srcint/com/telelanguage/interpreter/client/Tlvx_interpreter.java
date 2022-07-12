package com.telelanguage.interpreter.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.VideoElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.media.client.Audio;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.telelanguage.interpreter.client.websockets.Websocket;
import com.telelanguage.interpreter.client.websockets.WebsocketListener;

public class Tlvx_interpreter implements EntryPoint, JanusSipListener, WebsocketListener {
	public static final InterpreterServiceAsync interpreterService = GWT.create(InterpreterService.class);
	
	private static FlowPanel header = new FlowPanel();
	private static FlowPanel center = new FlowPanel();
	private static HorizontalPanel agentContainer = new HorizontalPanel();
	private static HorizontalPanel connectedContainer = new HorizontalPanel();
	private static HorizontalPanel dialingContainer = new HorizontalPanel();
	private static HorizontalPanel fillContainer = new HorizontalPanel();
	private static HorizontalPanel videoContainer = new HorizontalPanel();
	private static FormPanel loginForm = new FormPanel();
	private static FlowPanel interpretersPanel = new FlowPanel();
	private static FlowPanel actionPanel = new FlowPanel();
	private static Button loginButton = new Button("Login");
	private static Grid statusBar = new Grid(1,5);
	private static Caller agento = new Caller(Caller.TYPE.Agent);
	private static Caller customer = new Caller(Caller.TYPE.Client);
	private static Caller interpreter = new Caller(Caller.TYPE.Intreperter);
	private static List<Caller> thirdPartyList = new ArrayList<Caller>();
	static {
		for (int i=0; i<4; i++) {
			Caller thirdParty = new Caller(Caller.TYPE.ThridParty);
			//thirdParty.setHangupVisibility(false);
			thirdPartyList.add(thirdParty);
		}
	}
	private static Image connecting = new Image("images/arrows.png");
	private static Caller thirdPartyConnecting = new Caller(Caller.TYPE.ThridParty);
	static {
		thirdPartyConnecting.setHangupVisibility(false);
	}
	private static Button takeCallsButton = new Button();
	private static Button testVideoButton = new Button();
	private static Button enableVideoButton = new Button();
	private static Button rejectCallButton = new Button();
	private static Button muteCallButton = new Button();
	private static Button requestAgentButton = new Button();
	private static Button hangupButton = new Button();
	private static Button acceptCallButton = new Button();
	private static boolean acceptedManualCall = false;
	private static boolean acceptedConnectCall = false;
	private static boolean rejectedManualCall = false;
	private static boolean rejectedConnectCall = false;
	private static boolean sipIncoming = false;
	private static boolean askToAccept = false;
	private static boolean interpretingMethodSelected = false;
	private static TextBox accessCode = new TextBox();
	private static MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
	private static SuggestBox languageCombo = new SuggestBox(oracle);
	private static ListBox interpreterGender = new ListBox();
	private static FlowPanel additionalQuestionsLeft = new FlowPanel();
	private static FlowPanel additionalQuestionsRight = new FlowPanel();
    private static Label customerNameLabel = new Label();
    private static FlowPanel lastReasonLabel = new FlowPanel();
    private static boolean accessCodeValidated = false;
    private static FlowPanel actionToolBar = new FlowPanel();
    private static List<String> languages;
    DialogBox phoneNumberBox;
    DialogBox testVideoBox;
    DialogBox enableVideoBox;
    TextBox phoneNumber;
    private static boolean updatedNumber = false;
    private static boolean priorityCall = false;
    //private static boolean sendAceeptResponse = false;
    private static Label notTakingCallsLabel = new Label("Not Available to Take Calls");
    private static String priorThridPartyNumber = "";
	private static Websocket websocket;
	private static Sounds sounds = new Sounds();
	public static Janus sipCall;
	public static Janus videoRoom;
	public static String janusVideoRoomServer;
	public static String janusVideoRoomNumber;
	private static Video interpreterVideo = new Video("interpreter");
	private static Video customerVideo = new Video("customer");
	private static Video agentVideo = new Video("agent");
	
	private static Janus testVideo;
	private static HorizontalPanel testVideoContainer = new HorizontalPanel();
	private static Video testLocalVideo = new Video("testlocal");
	private static Video testRemoteVideo = new Video("testremote");
    
    private static boolean webPhoneEnabled = false;
    private static boolean videoEnabled = false;
    private static boolean videoOnly = false;
    private static boolean forceVideoOnly = false;
    private static boolean callMuted = false;
    private static boolean onHold = false;
    private static boolean agentOnCall = false;
    private static String webrtcUrl;
    private static String turnServer;
    private static String turnUsername;
    private static String turnPassword;
    private static String sipRegistrationServer;
    private static Date lastHeartbeat = new Date();
    private static String missedCalls = "0";
    private static boolean allowVideo = false;
    private static boolean timedReject = false;
    
    private static boolean agentRequested = false;
    
    public static String sessionId = UUID.uuid();
    public static String videoRoomID = null;
    
    public static String wsUrl;
    
    public static boolean sendDesktopNotifications = false;
    private JavaScriptObject notification;
    
    private static Timer rejectTimer = new Timer() {
		@Override
		public void run() {
			timedReject = true;
			rejectCallAction("timer reject (c)");
		}
    };
    
	private static Timer heartBeatCheckTimer = new Timer() {
		@Override
		public void run() {
			Date now = new Date();
			long diff = now.getTime() - lastHeartbeat.getTime();
			if (diff > 60000) {
				logout("The server is not responding, you have been logged out, please log in again.");
			}
		}
	};
	
	public static String callId = "";
	public static String name = "";
	public static String email = "";
	public static boolean callActive = false;
	public static Tlvx_interpreter thisTlvx;

	static String numberMode = "";
	
	static Audio noAgentsSound; 
	static Audio priorityCallSound;

	protected static String phonenumber;
	
	public void onModuleLoad() {
		thisTlvx = this;
		setUpBanner();
		setUpLoginForm();
		setUpAgentPanel();
		
		noAgentsSound = Audio.createIfSupported();
		if (noAgentsSound != null) noAgentsSound.setSrc("telelanguage/sounds/noagents.mp3");
		priorityCallSound = Audio.createIfSupported();
		priorityCallSound.setSrc("telelanguage/sounds/prioritycall.mp3");
		
		showLoginForm();
		
		interpreterService.getRelease(new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
				error(caught);
			}
			@Override
			public void onSuccess(String result) {
				Label releaseLabel = new Label(result);
				RootPanel.get("release").add(releaseLabel);
			}
		});
		
	    if (!"firefox".equals(getAdapterBrowser())) {
	    	//notFirefoxBox();
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
	
	public native String getPermission() /*-{
		return Notification.permission;
	}-*/;
	
	public native void requestPermission() /*-{
		var thisInstance = this;
		if ("Notification" in $wnd) {
		    $wnd.Notification.requestPermission().then(function(result) {
		    	thisInstance.@com.telelanguage.interpreter.client.Tlvx_interpreter::permissionGranted(Ljava/lang/String;)(result);
		    });
		}
	}-*/;
	
	public native void showNotification(String language) /*-{
		var thisInstance = this;
		if (language == null) language = '';
		if (Notification.permission == 'granted') {
			try {
				thisInstance.@com.telelanguage.interpreter.client.Tlvx_interpreter::notification = 
				new $wnd.Notification(language+' Incoming Call', 
					{ 
						body: 'You have an incoming '+language+' call.  Click to answer.',
						icon: 'images/logo.png'
					} 
				);
				thisInstance.@com.telelanguage.interpreter.client.Tlvx_interpreter::notification.onclick = function(){
					try
				    {
				        $wnd.focus();
				        thisInstance.@com.telelanguage.interpreter.client.Tlvx_interpreter::notification.close();
				    }
				    catch (ex) {};
					thisInstance.@com.telelanguage.interpreter.client.Tlvx_interpreter::acceptCall()();
				};
		       	$wnd.setTimeout(thisInstance.@com.telelanguage.interpreter.client.Tlvx_interpreter::notification.close.bind(thisInstance.@com.telelanguage.interpreter.client.Tlvx_interpreter::notification), 5000);
			}
			catch (e) {
				//$wnd.alert(e);
			}
		}
	}-*/;
	
	public void permissionGranted(String permission) {
		if (permission != null && permission.equals("grnted")) {
			sendDesktopNotifications = true;
		}
	}
	
	public void checkNotificationSettings() {
		String permission = getPermission();
		if (permission != null && permission.equals("default")) {
			requestPermission();
		} else if (permission != null) {
			permissionGranted(permission);
		}
	};
	
	public static native void showMessage(String titleString, String messageString) /*-{
	  $wnd.$.gritter.add({
	  		title: titleString,
	  		text: messageString,
	  		sticky: false,
	  		time: ''
	  });
	}-*/;

	private void setUpBanner() {
		//RootPanel.get("status").add(blink);
		//blink.setVisible(false);
	}
	
	private static void clearContent() {
		RootPanel.get("wrapper").clear();
		dialingContainer.clear();
	}

	private void setUpLoginForm() {
		FlowPanel loginBox = new FlowPanel();
		loginBox.setStyleName("login-box");
		loginForm.add(loginBox);

		FlowPanel agentTop = new FlowPanel();
		agentTop.setStyleName("agent-top");
		FlowPanel group = new FlowPanel();
		group.addStyleName("group");
		agentTop.add(group);
//		FlowPanel fl = new FlowPanel();
//		fl.setStyleName("logo");
//		group.add(fl);
		Label fr = new Label("");
		fr.setStyleName("name");
		group.add(fr);
		FlowPanel agentLog = new FlowPanel();
		agentLog.setStyleName("interpreter-log");
		agentTop.add(agentLog);
		loginBox.add(agentTop);
		
		FlowPanel form = new FlowPanel();
		form.setStyleName("login");
		form.addStyleName("blue");
		loginBox.add(form);
		
		FlowPanel emailPanel = new FlowPanel();
		emailPanel.setStyleName("loginFormLine");
		final TextBox email = new TextBox(); 
		email.setStyleName("loginBox");
	    Label emailLabel = new Label("Email");
	    emailLabel.setStyleName("loginLabel");
	    emailPanel.add(emailLabel);
	    emailPanel.add(email);
	    form.add(emailPanel);
	    
	    FlowPanel passwordPanel = new FlowPanel();
	    passwordPanel.setStyleName("loginFormLine");
	    final PasswordTextBox password = new PasswordTextBox();
	    password.setStyleName("loginBox");
		Label passwordLabel = new Label("PIN Code");
		passwordLabel.setStyleName("loginLabel");
		passwordPanel.add(passwordLabel);
		passwordPanel.add(password);
		form.add(passwordPanel);
		
		//email.setValue("kcoston@telelanguage.net");
		//password.setValue("telelanguage");
		
		FlowPanel actionP = new FlowPanel();
		actionP.setStyleName("loginActionPanel");
		form.add(actionP);
		Label forgotPassword = new Label("Forgot your password?");
		forgotPassword.setStyleName("loginForgotPassword");
		actionP.add(forgotPassword);
		
		loginButton.setStyleName("login-btn");
		loginButton.addStyleName("orange");
		loginButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				loginButton.setEnabled(false);
				loginButton.removeStyleName("red");
				if (email.getValue().length()>0 && password.getValue().length()>0) {
					Tlvx_interpreter.email = email.getValue();
					Tlvx_interpreter.phonenumber = password.getValue();
					if (!Tlvx_interpreter.phonenumber.startsWith("777")) {
						Tlvx_interpreter.phonenumber = "777"+Tlvx_interpreter.phonenumber;
					}
					interpreterService.logon(sessionId, Tlvx_interpreter.email, Tlvx_interpreter.phonenumber, new AsyncCallback<ConfigInfo>() {
						@Override
						public void onFailure(Throwable caught) {
							error(caught);
							loginButton.setEnabled(true);
						}
						@Override
						public void onSuccess(ConfigInfo result) {
							webrtcUrl = result.webRtcUrl;
							turnServer = result.turnServer;
							turnUsername = result.turnUsername;
							turnPassword = result.turnPassword;
							allowVideo = result.allowVideo;
							videoEnabled = result.videoEnabled;
							videoOnly = result.videoOnly;
							forceVideoOnly = result.forceVideoOnly;
							if (allowVideo) {
								testVideoButton.setEnabled(true);
								testVideoButton.addStyleName("orange");
								if (videoEnabled) {
									if (forceVideoOnly) {
										acceptVideo("<span>Accepting<br>Video Only</span>", "Only accepting video calls.", true, true);
									} else {
										enableVideoButton.setEnabled(true);
										enableVideoButton.addStyleName("red");
										enableVideoButton.setHTML("<span>Select<br>Video or Audio</span>");
									}
									interpreter.setVideoCall(true);
								} else {
									if (forceVideoOnly) {
										acceptVideo("<span>Accepting<br>Video Only</span>", "Only accepting video calls.", true, true);
									} else {
										enableVideoButton.setHTML("<span>Select<br>Video or Audio</span>");
										enableVideoButton.setEnabled(true);
										enableVideoButton.addStyleName("red");
										interpreter.setVideoCall(false);
									}
								}
							} else {
								testVideoButton.setEnabled(false);
								testVideoButton.setHTML("");
								testVideoButton.removeStyleName("orange");
								enableVideoButton.setHTML("");
								enableVideoButton.setEnabled(false);
								enableVideoButton.removeStyleName("red");
							}
							sipRegistrationServer = result.sipRegistrationServer;
							loginButton.setEnabled(true);
							wsUrl = result.wsUrl;
							if (webrtcUrl.length() == 0) {
								info("Your email or password is invalid,\n contact the administrator.");
							} else login(result.wsUrl);
						}
					});
				} else {
					showMessage("Invalid", "You must enter an email and a password.");
				}
			}
		});
		actionP.add(loginButton);
		
		setUpActionPanel();
		setUpVideoPanel();
		
//		yesOneButton.setStyleName("small-btn green");
//		yesOneButton.setHTML("<span>Accept Call<br>1 YES</span>");
//		actionPanel.add(yesOneButton);
//		yesOneButton.addClickHandler(new ClickHandler() {
//			@Override
//			public void onClick(ClickEvent event) {
//				Janus.sendDTMF("1");
//			}
//		});
//		noTwoButton.setStyleName("small-btn red");
//		noTwoButton.setHTML("<span>Decline Call<br/>and Log Out<br/>2 No</span>");
//		actionPanel.add(noTwoButton);
//		noTwoButton.addClickHandler(new ClickHandler() {
//			@Override
//			public void onClick(ClickEvent event) {
//				Janus.sendDTMF("2");
//				stopTakingCalls();
//			}
//		});
//		getAgentPoundbutton.setStyleName("small-btn blue");
//		getAgentPoundbutton.setHTML("<span>Get Agent<br/>#</span>");
//		actionPanel.add(getAgentPoundbutton);
//		getAgentPoundbutton.addClickHandler(new ClickHandler() {
//			@Override
//			public void onClick(ClickEvent event) {
//				Janus.sendDTMF("#");
//			}
//		});
	}
	
	private static void logout(final String message) {
		//messageTimer.cancel();
		sipCall.unregisterSip();
		sipHangup();
		stopVideoRoom();
		interpreterService.logout(sessionId, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				error(caught);
			}
			@Override
			public void onSuccess(Void result) {
				//blink.setVisible(false);
				clearContent();
				showLoginForm();
				if (message.length() == 0) info("You are now logged out.");
				else info(message);
				loginButton.setEnabled(true);
				websocket.close();
				callActive = false;
				heartBeatCheckTimer.cancel();
				//Window.Location.reload();
			}
		});
	}
	
	private void login(String wsUrl) {		
		final Tlvx_interpreter thisTlvx = this;
		interpretingMethodSelected = false;
		stopTakingCalls();
		clearContent();
		showAgentPanel();
		info("You are now logged in.");
		missedCalls = "0";
		if (Websocket.isSupported()) {
			websocket = new Websocket(wsUrl+"?sid="+sessionId);
			websocket.addListener(this);
			websocket.open();
		}
		sipCall = new Janus(new JanusListener() {
			@Override
			public void initSuccess() {
				//info("init success");
				sipCall.sipPluginAttach(thisTlvx);
			}

			@Override
			public void error(String reason) {
				info("init error "+reason);
			}

			@Override
			public void destroyed() {
				info("destroyed");
				sipCall = new Janus(this, webrtcUrl, turnServer, turnUsername, turnPassword);
			}
		}, webrtcUrl, turnServer, turnUsername, turnPassword);
	}

	private static void showLoginForm() {
		clearContent();
		RootPanel.get("wrapper").add(loginForm);
	}
	
	private void setUpAgentPanel() {
		header.getElement().setId("header");
		center.getElement().setId("center");

		FlowPanel agentTop = new FlowPanel();
		agentTop.getElement().setId("agent-top");
		agentTop.setStyleName("fl");
		agentTop.getElement().getStyle().setMarginRight(7, Unit.PX);
		FlowPanel group = new FlowPanel();
		group.addStyleName("group");
		agentTop.add(group);
//		FlowPanel fl = new FlowPanel();
//		fl.setStyleName("logo");
//		group.add(fl);
		Label fr = new Label("");
		fr.setStyleName("name");
		group.add(fr);
		FlowPanel agentLog = new FlowPanel();
		agentLog.setStyleName("agent-log");
		agentTop.add(agentLog);
		header.add(agentTop);
		
		FlowPanel btnWrap = new FlowPanel();
		header.add(btnWrap);
		btnWrap.setStyleName("btn-wrap");
		takeCallsButton.setHTML("<span>Accept<br>Calls</span>");
		takeCallsButton.setStyleName("mid-btn");
		takeCallsButton.addStyleName("blue");
		takeCallsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				VideoElement videoAgent = Document.get().getElementById("videoagent").cast();
				VideoElement videoCustomer = Document.get().getElementById("videocustomer").cast();
				VideoElement videoInterpreter = Document.get().getElementById("videointerpreter").cast();
				
				videoAgent = Document.get().getElementById("videoagent").cast();
				videoCustomer = Document.get().getElementById("videocustomer").cast();
				videoInterpreter = Document.get().getElementById("videointerpreter").cast();
				
				if (videoAgent != null) videoAgent.setAutoplay(true);
				if (videoCustomer != null) videoCustomer.setAutoplay(true);
				if (videoInterpreter != null) videoInterpreter.setAutoplay(true);
				
				AudioElement soundsAudio = Document.get().getElementById("sounds").cast();
				AudioElement siplocalaudio = Document.get().getElementById("siplocalaudio").cast();
				AudioElement sipremoteaudio = Document.get().getElementById("sipremoteaudio").cast();
				
				if (soundsAudio != null) soundsAudio.setAutoplay(true);
				if (siplocalaudio != null) siplocalaudio.setAutoplay(true);
				if (sipremoteaudio != null) sipremoteaudio.setAutoplay(true);
				
				if (takeCallsButton.getHTML().equalsIgnoreCase("<span>Accept<br>Calls</span>")) {
					if (allowVideo && !forceVideoOnly && !interpretingMethodSelected) {
						showMessage("Select Method","You must first update your interpreting method (red button).");
					} else if (!updatedNumber && !webPhoneEnabled) {
						showMessage("Warning","You must first update your phone number or enable the web phone before taking calls.");
					} else {
						interpreterService.startTakingCalls(sessionId, new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
								error(caught);
							}
							@Override
							public void onSuccess(Void x) {
								takeCallsButton.setHTML("<span>Stop Accepting<br>Calls</span>");
								interpreter.setVisible(true);
								notTakingCallsLabel.setVisible(false);
							}
						});
					}
				} else {
					stopTakingCalls();
				}
			}
		});
		btnWrap.add(takeCallsButton);

		testVideoButton.setHTML("<span>Test<br>Video</span>");
		testVideoButton.setStyleName("mid-btn");
		//testVideoButton.addStyleName("orange");
		testVideoButton.setEnabled(false);
		testVideoButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				newTestVideoBox();
			}
		});
		btnWrap.add(testVideoButton);
		enableVideoButton.setHTML("<span>Enable<br>Video</span>");
		enableVideoButton.setStyleName("mid-btn");
		//enableVideoButton.addStyleName("red");
		enableVideoButton.setEnabled(false);
		enableVideoButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				enableVideoButton.setEnabled(false);
				enableVideoButton.removeStyleName("red");
				newEnableVideoBox();
			}
		});
		btnWrap.add(enableVideoButton);
		Button logoutButton = new Button();
		logoutButton.setHTML("<span>Log Out</span>");
		logoutButton.setStyleName("mid-btn");
		logoutButton.addStyleName("purple");
		btnWrap.add(logoutButton);
		header.add(btnWrap);
		logoutButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				logout("");
			}
		});
		//header.add(customerNameLabel);
		//customerNameLabel.addStyleName("customerNameLabel");
		
		statusBar.getElement().setId("status-bar");
		statusBar.setStyleName("");
		updateStatus(null);
		center.add(statusBar);
		
		agentContainer.add(connectedContainer);
		agentContainer.add(dialingContainer);
		agentContainer.add(fillContainer);
		agentContainer.setWidth("100%");
		agentContainer.setCellWidth(fillContainer, "100%");
		agentContainer.setStyleName("agentpanelcentered");
		connectedContainer.add(interpreter);
		center.add(agentContainer);
		
		customerNameLabel.setStyleName("customerNameLabel");
		center.add(customerNameLabel);
		lastReasonLabel.setStyleName("lastReasonLabel");
		center.add(lastReasonLabel);
		notTakingCallsLabel.setStyleName("notTakingCallsLabel");
		center.add(notTakingCallsLabel);
		notTakingCallsLabel.setVisible(true);
		
	}
	
	private static void showRejectButton() {
		muteCallButton.setVisible(false);
		rejectCallButton.setVisible(true);
	}
	
	private static void showMuteButton() {
		rejectCallButton.setVisible(false);
		muteCallButton.setVisible(true);
	}
	
	private static void rejectCallAction(String reason) {
		if (rejectTimer != null) rejectTimer.cancel();
		acceptCallButton.setEnabled(false);
		rejectCallButton.setEnabled(false);
		showMuteButton();
		rejectCallButton.removeStyleName("red");
		acceptCallButton.removeStyleName("blue");
		rejectedManualCall = true;
		rejectedConnectCall = true;
		sendResponse(reason);
	}
	
	private void setUpActionPanel() {
		actionToolBar.setStyleName("btn-wrap");
		hangupButton.setStyleName("mid-btn");
		hangupButton.setHTML("<span>Hangup<br/>Call</span>");
		hangupButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (rejectCallButton.isEnabled()) {
					rejectCallAction("clicked hangup button");
					logout("Logged out as part of reject action.");
				} else {
					interpreterService.hangupRequest(sessionId, new AsyncCallback<Void>() {
						@Override
						public void onFailure(Throwable caught) {
							error(caught);
							stopVideoRoom();
						}
						@Override
						public void onSuccess(Void result) {
							stopVideoRoom();
						}
					});
				}
			}
		});
		actionToolBar.add(hangupButton);
		
		requestAgentButton.setStyleName("mid-btn");
		requestAgentButton.setHTML("<span>Request<br>Agent/<br>Dialout</span>");
		requestAgentButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				newPhoneNumberBox();
			}
		});
		actionToolBar.add(requestAgentButton);
		
		rejectCallButton.setStyleName("mid-btn");
		rejectCallButton.setHTML("<span>Reject<br>Call</span>");
		rejectCallButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				rejectCallAction("clicked reject button");
				logout("Logged out as part of reject action.");
			}
		});
		actionToolBar.add(rejectCallButton);
		
		muteCallButton.setStyleName("mid-btn green");
		muteCallButton.setHTML("<span>Mute</span>");
		muteCallButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (sipCall == null) {
					showMessage("Mute","Not currently in a call.");
					return;
				}
				if (callMuted) {
					callMuted = false;
					muteCallButton.removeStyleName("orange");
					muteCallButton.addStyleName("green");
					muteCallButton.setHTML("<span>Mute</span>");
					sipCall.sipPluginUnmute();
				} else {
					muteCallButton.removeStyleName("green");
					muteCallButton.addStyleName("orange");
					muteCallButton.setHTML("<span>Unmute</span>");
					sipCall.sipPluginMute();
					callMuted = true;
				}
			}
		});
		muteCallButton.setVisible(false);
		actionToolBar.add(muteCallButton);
		
		acceptCallButton.setStyleName("big-btn");
		acceptCallButton.setHTML("<span>Accept Call</span>");
		actionToolBar.add(acceptCallButton);
		acceptCallButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				acceptCall();
			}
		});
		
		actionPanel.add(actionToolBar);
	}
	
	private void acceptCall() {
		if (rejectTimer != null && rejectTimer.isRunning()) {
			rejectTimer.cancel();
			acceptCallButton.setEnabled(false);
			rejectCallButton.setEnabled(false);
			showMuteButton();
			rejectCallButton.removeStyleName("red");
			acceptCallButton.removeStyleName("blue");
			setUpVideoPanel();
			acceptedManualCall = true;
			acceptedConnectCall = true;
			sendResponse(null);
		}
	}
	
	private void notFirefoxBox() {
		Tlvx_interpreter that = this;
		testVideoBox = new DialogBox();
		testVideoBox.setTitle("Not Firefox");
		FlowPanel content = new FlowPanel();
		content.add(new HTML("<p>TELELANGUAGE Video requires Firefox</p>\n"+
			"<p><img src=\"images/firefox-logo.png\"/></p>"+
			"<p>Only firefox has been certified to work reliably with TeleLanguage Video.</p>"+
			"<p>If you are on Android, visit the store to install firefox, then return to this"+
			"page.  If you are on a PC or Mac, load or install firefox.  If you need to"+
			"download and install, visit https://www.mozilla.com/firefox.</p>"+
			"<p>Thank you for using TeleLanguage Video!</p>"));
		testVideoBox.add(content);
		testVideoBox.center();
	}
	
	private void newEnableVideoBox() {
		enableVideoBox = new DialogBox();
		enableVideoBox.setTitle("Call Type Mode");
		FlowPanel contents = new FlowPanel();
		enableVideoBox.add(contents);
		Button audioOnlyButton = new Button();
		audioOnlyButton.getElement().setInnerHTML("<span>Accept<br>Audio Only</span>");
		audioOnlyButton.setStyleName("mid-btn");
		audioOnlyButton.addStyleName("blue");
		contents.add(audioOnlyButton);
		Button videoOnlyButton = new Button();
		videoOnlyButton.getElement().setInnerHTML("<span>Accept<br>Video Only</span>");
		videoOnlyButton.setStyleName("mid-btn");
		videoOnlyButton.addStyleName("blue");
		contents.add(videoOnlyButton);
		Button audioAndVideoButton = new Button();
		audioAndVideoButton.getElement().setInnerHTML("<span>Accept<br>Audio and Video</span>");
		audioAndVideoButton.setStyleName("mid-btn");
		audioAndVideoButton.addStyleName("blue");
		contents.add(audioAndVideoButton);
		enableVideoBox.center();
		
		audioOnlyButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				acceptVideo("<span>Accepting<br>Audio Only</span>", "Only accepting audio calls.", false, false);
			}
		});
		videoOnlyButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				acceptVideo("<span>Accepting<br>Video Only</span>", "Only accepting video calls.", true, true);
			}
		});
		audioAndVideoButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				acceptVideo("<span>Accepting<br>Video or Audio</span>", "Accepting audio and video calls.", true, false);
			}
		});
	}
	
	private void acceptVideo(final String buttonText, final String successMessage, final boolean video, boolean videoOnly) {
		interpreterService.acceptVideo(sessionId, video, videoOnly, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				info(caught.getMessage());
			}
			@Override
			public void onSuccess(Void result) {
				if (forceVideoOnly) {
					enableVideoButton.setHTML("<span>Video Only</span>");
				} else {
					enableVideoButton.setHTML(buttonText);
					enableVideoButton.setEnabled(true);
					enableVideoButton.addStyleName("red");
				}
				interpreter.setVideoCall(video);
				info(successMessage);
				videoEnabled = video;
				if (enableVideoBox != null) enableVideoBox.hide();
				interpretingMethodSelected = true;
			}
		});
	}
	
	private void newTestVideoBox() {
		Tlvx_interpreter that = this;
		
		testVideoBox = new DialogBox();
		testVideoBox.setTitle("Test Video");
		testVideoBox.hide();
		
		FlowPanel contents = new FlowPanel();
		contents.getElement().getStyle().setWidth(800, Unit.PX);
		contents.getElement().getStyle().setHeight(320, Unit.PX);
		testVideoContainer.add(testLocalVideo);
		testVideoContainer.add(testRemoteVideo);
		contents.add(testVideoContainer);
		FlowPanel buttonBoxContainer = new FlowPanel();
		final Button okButton = new Button("OK");
		final Button cancelButton = new Button("Cancel");
		buttonBoxContainer.add(okButton);
		buttonBoxContainer.add(cancelButton);
		contents.add(buttonBoxContainer);
		testVideoBox.add(contents);
		testVideoBox.center();
		
		testVideo = new Janus(new JanusListener() {
			@Override
			public void initSuccess() {
				log("echotest initSuccess()");
				testVideo.echoPluginAttach(new JanusEchoTestListener() {
					@Override
					public void echoPluginSuccess() {
						testVideo.echoStartTest();
					}
					@Override
					public void echoPluginError(String error) {
						log("Video test error: "+error);
					}
					@Override
					public void echoPluginConsentDialog(Boolean on) {
						
					}
					@Override
					public void echoPluginDone() {
						info("Video done.");
					}
				});
			}
			@Override
			public void error(String reason) {
				info("Test Video Error: "+reason);
			}
			@Override
			public void destroyed() {
				info("Video test done.");
			}
		}, webrtcUrl, turnServer, turnUsername, turnPassword);
		
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				testVideoBox.hide();
				testVideo.echoPluginHangup();
				testVideo.destroy();
				okButton.setEnabled(false);
				cancelButton.setEnabled(false);
				testVideoButton.setHTML("<span>Test<br>Video</span>");
				testVideoButton.setEnabled(true);
				testVideoButton.addStyleName("orange");
			}
		});
		
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				testVideoBox.hide();
				testVideo.echoPluginHangup();
				testVideo.destroy();
			}
		});

	}
	
	private void newPhoneNumberBox() {
		phoneNumberBox = new DialogBox();
		phoneNumberBox.setTitle("Dial Third Party Number");
		phoneNumberBox.hide();
		phoneNumberBox.center();
		phoneNumber = new TextBox();
		Label phoneNumberLabel = new Label("Phone Number:");
		FlowPanel phoneNumberBoxContainer = new FlowPanel();
		phoneNumberBoxContainer.add(phoneNumberLabel);
		phoneNumberBoxContainer.add(phoneNumber);
		Button okButton = new Button("OK");
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				interpreterService.dialThirdParty(sessionId, phoneNumber.getValue(), new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						info("Unable to dial 3rd party "+caught.getMessage());
					}
					@Override
					public void onSuccess(Void result) {
						info("Dial third party request sent.");
					}
				});
//					interpreterService.updateNumber(sessionId, false, phoneNumber.getValue(), new AsyncCallback<Void>() {
//						@Override
//						public void onFailure(Throwable caught) {
//							info("Unable to update to this number, please try a different number, it may already be in use.");
//						}
//						@Override
//						public void onSuccess(Void result) {
//							updatedNumber = true;
//						}
//					});
				phoneNumberBox.hide();
			}
		});
		phoneNumberBoxContainer.add(okButton);
		Button agentButton = new Button("Agent Assist");
		agentButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!agentRequested) {
					agentRequested=true;
					dialingContainer.add(connecting);
					dialingContainer.add(agento);
					interpreterService.requestAgent(sessionId, new AsyncCallback<Void>() {
						@Override
						public void onFailure(Throwable caught) {
							error(caught);
						}
						@Override
						public void onSuccess(Void result) {
						}
					});
				}
				phoneNumberBox.hide();
			}
		});
		phoneNumberBoxContainer.add(agentButton);
		Button cancelButton = new Button("Cancel");
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				phoneNumberBox.hide();
			}
		});
		phoneNumberBoxContainer.add(cancelButton);
		phoneNumberBox.add(phoneNumberBoxContainer);
	}
	
	private static boolean languageValid() {
		//System.out.println(languageCombo.getValue());
		return languages.contains(languageCombo.getValue());
	}
	
	static private int priorClickedRow = 0;
	
	static void info(String message) {
		info("Info", message);
	}
	
	private static void info(String title, String message) {
		showMessage(title, message);
	}
	
	private void showAgentPanel() {
		RootPanel.get("wrapper").add(header);
		RootPanel.get("wrapper").add(center);
	}

	public static void connected() {
		//blink.setUrl("telelanguage/images/online.png");
	}

	public static void disconnected() {
		//blink.setUrl("telelanguage/images/exclamation.png");
	}
	
	private static DateTimeFormat dateFormatter = DateTimeFormat.getFormat(" MMMM dd, yyyy ");
	private static DateTimeFormat timeFormatter = DateTimeFormat.getFormat(" h:mm zzzz ");
	
	public static void updateStatus(String status) {
		if (status != null) {
			String callid = getDataValue(status, "callid");
			String name = getDataValue(status, "name");
			String number = getDataValue(status, "number");
			
			interpreter.setTopLabel(name);
			
			statusBar.setHTML(0, 0, name);
			statusBar.setHTML(0, 1, number);
			statusBar.setHTML(0, 2, getDataValue(status, "active"));
		}
		Date now = new Date();
		statusBar.setHTML(0, 3, dateFormatter.format(now));
		statusBar.setHTML(0, 4, timeFormatter.format(now));
	}
	
	public static void updateVideosVisible() {
		videoContainer.setVisible(!agentOnCall && !onHold);
	}

	public static void processMessages(List<String> messages) {
		for (Object message: messages) {
			String msg = (String) message;
			if (msg.indexOf(":")==-1) {
				log("onMessage (NOT IMPLEMENTED): "+msg);
				return;
			}
			String type = msg.substring(0, msg.indexOf(":"));
			String data = msg.substring(msg.indexOf(":")+1);
			log("onMessage: "+type+" data: "+data);
			agentOnCall = "true".equals(getDataValue(data, "agentOnCall"));
			String newMissedCalls = getDataValue(data, "missedCalls");
			if (newMissedCalls != null && !newMissedCalls.equals("") && !newMissedCalls.equals("null")) {
				missedCalls = newMissedCalls;
				interpreter.setBottomLabel("Missed Calls: "+missedCalls);
			}
			if (agentOnCall) agentRequested = false;
			boolean interpreterValidated = "true".equals(getDataValue(data, "interpreterValidated"));
			if (!msg.startsWith("ping:")) {
				for (int i=0; i<4; i++) {
					Caller thirdParty = thirdPartyList.get(i);
					thirdParty.setId(getDataValue(data,"thirdPartyId"+i));
					thirdParty.setBottomLabel(getDataValue(data,"thirdPartyNumber"+i));
				}
				if (getDataValue(data, "dialingThirdParty").equals("true")) {
					dialingContainer.add(connecting);
					dialingContainer.add(thirdPartyConnecting);
				} else {
					dialingContainer.clear();
				}
			}
			String instructionsText = getDataValue(data, "instructionsText");
			if (null == instructionsText || "null".equals(instructionsText) || "".equals(instructionsText)) {
				//removeInstructionsWav();
			} else {
				insertInstructionsWav(instructionsText);
			}
			switch(InterpreterMessages.valueOf(type)) {
			case status: updateStatus(data); updateVideosVisible(); break;
			case onhold:
				onHold = true;
				String callId = getDataValue(data, "callId");
				String language = getDataValue(data, "language");
				customer.setTopLabel("Call ID: "+callId);
				customer.setBottomLabel(language);
				customer.setIncomingCall(false, false);
				dialingContainer.clear();
				dialingContainer.add(connecting);
				dialingContainer.add(customer);
				connectedContainer.remove(agento);
				if (agentOnCall) dialingContainer.add(agento);
				else dialingContainer.remove(agento);
				for (int i=0; i<4; i++) {
					Caller thirdParty = thirdPartyList.get(i);
					if ("".equals(thirdParty.getId())) thirdParty.removeFromParent();
					else dialingContainer.add(thirdParty);
				}
				connectedContainer.setStyleName("incall");
				showActionPanel();
				rejectCallButton.setEnabled(false);
				rejectCallButton.removeStyleName("red");
				showMuteButton();
				acceptCallButton.setEnabled(false);
				acceptCallButton.removeStyleName("blue");
				hangupButton.setEnabled(true);
				hangupButton.addStyleName("red");
				if (!agentOnCall && interpreterValidated) {
					requestAgentButton.setEnabled(true);
					requestAgentButton.addStyleName("blue");
				} else {
					requestAgentButton.setEnabled(false);
					requestAgentButton.removeStyleName("blue");
				}
				updateVideosVisible();
				break;
			case offhold:
				onHold = false;
				callId = getDataValue(data, "callId");
				language = getDataValue(data, "language");
				janusVideoRoomServer = getDataValue(data, "videoJanusServer");
				janusVideoRoomNumber = getDataValue(data, "videoJanusRoomNumber");
				if ("null".equals(janusVideoRoomServer) || "".equals(janusVideoRoomServer)) {
					janusVideoRoomServer = null;
					customer.setVideoCall(false);
				} else {
					customer.setVideoCall(true);
				}
				customer.setTopLabel("Call ID: "+callId);
				customer.setBottomLabel(language);
				customer.setIncomingCall(false, false);
				dialingContainer.clear();
				dialingContainer.remove(connecting);
				connectedContainer.add(customer);
				connectedContainer.setStyleName("incall");
				dialingContainer.remove(agento);
				if (agentOnCall) connectedContainer.add(agento);
				else connectedContainer.remove(agento);
				for (int i=0; i<4; i++) {
					Caller thirdParty = thirdPartyList.get(i);
					if ("".equals(thirdParty.getId())) thirdParty.removeFromParent();
					else connectedContainer.add(thirdParty);
				}
				showActionPanel();
				acceptCallButton.setEnabled(false);
				acceptCallButton.removeStyleName("blue");
				rejectCallButton.setEnabled(false);
				rejectCallButton.removeStyleName("red");
				acceptCallButton.setEnabled(false);
				acceptCallButton.removeStyleName("blue");
				hangupButton.setEnabled(true);
				hangupButton.addStyleName("red");
				if (!agentOnCall && interpreterValidated) {
					requestAgentButton.setEnabled(true);
					requestAgentButton.addStyleName("blue");
				} else {
					requestAgentButton.setEnabled(false);
					requestAgentButton.removeStyleName("blue");
				}
				if (getDataValue(data, "playConfIn").equals("true")) {
					sounds.play("conf-in.wav", new AsyncCallback<Void>() {
						@Override
						public void onFailure(Throwable caught) {
						}
						@Override
						public void onSuccess(Void result) {
							if (videoEnabled && videoRoom == null && janusVideoRoomServer != null && janusVideoRoomNumber != null) {
								startVideoRoom();
							}
						}
					});
				} else {
					if (videoEnabled && videoRoom == null && janusVideoRoomServer != null && janusVideoRoomNumber != null) {
						startVideoRoom();
					}
				}
				updateVideosVisible();
				break;
			case callIncoming:
				onHold = true;
				agentOnCall = false;
				callId = getDataValue(data, "callId");
				language = getDataValue(data, "language");
				janusVideoRoomServer = getDataValue(data, "videoJanusServer");
				janusVideoRoomNumber = getDataValue(data, "videoJanusRoomNumber");
				
				if ("null".equals(janusVideoRoomServer) || "".equals(janusVideoRoomServer) || janusVideoRoomServer==null) {
					customer.setVideoCall(false);
				} else {
					customer.setVideoCall(true);
				}
				customer.setTopLabel("Call ID: "+callId);
				customer.setBottomLabel(language);
				customer.setIncomingCall(false, false);
				//customer.setVideoCall(videoEnabled && videoRoom == null && janusVideoRoomServer != null && janusVideoRoomNumber != null);
				interpreter.setVideoCall(videoEnabled && videoRoom == null && janusVideoRoomServer != null && janusVideoRoomNumber != null);
				dialingContainer.clear();
				dialingContainer.add(connecting);
				dialingContainer.add(customer);
				connectedContainer.remove(agento);
				if (agentOnCall) dialingContainer.add(agento);
				else dialingContainer.remove(agento);
				for (int i=0; i<4; i++) {
					Caller thirdParty = thirdPartyList.get(i);
					if ("".equals(thirdParty.getId())) thirdParty.removeFromParent();
					else dialingContainer.add(thirdParty);
				}
				connectedContainer.setStyleName("outcall");
				agentRequested = false;
				//sendAceeptResponse = false;
				acceptedManualCall = false;
				acceptedConnectCall = false;
				rejectedManualCall = false;
				rejectedConnectCall = false;
				askToAccept = false;
				guiAskToAccept();
				thisTlvx.showNotification(language);
				updateVideosVisible();
				break;
			case askToAcceptCall:
				//sipCall.answer();
				//sendAceeptResponse = true;
				askToAccept = true;
				sendResponse(null);

				for (int i=0; i<4; i++) {
					Caller thirdParty = thirdPartyList.get(i);
					if ("".equals(thirdParty.getId())) thirdParty.removeFromParent();
					else dialingContainer.add(thirdParty);
				}
//				callId = getDataValue(data, "callId");
//				language = getDataValue(data, "language");
//				customer.setTopLabel("Call ID: "+callId);
//				customer.setBottomLabel(language);
//				dialingContainer.clear();
//				dialingContainer.add(connecting);
//				dialingContainer.add(customer);
//				connectedContainer.remove(agento);
//				if (agentOnCall) dialingContainer.add(agento);
//				else dialingContainer.remove(agento);
//				customer.setIncomingCall(true, false);
//				connectedContainer.setStyleName("outcall");
//				guiAskToAccept();
				break;
			case disconnect:
				hideVideoContainer();
				dialingContainer.clear();
				connectedContainer.remove(customer);
				connectedContainer.remove(agento);
				for (int i=0; i<4; i++) {
					Caller thirdParty = thirdPartyList.get(i);
					thirdParty.removeFromParent();
				}
				connectedContainer.setStyleName("outcall");
				janusVideoRoomServer = null;
				janusVideoRoomNumber = null;
				if (rejectTimer != null) rejectTimer.cancel();
				removeActionPanel();
				stopVideoRoom();
				boolean rejectable = rejectCallButton.isEnabled();
				rejectCallButton.setEnabled(false);
				sipHangup();
				rejectCallButton.setEnabled(rejectable);
				onHold = true;
				agentOnCall = false;
				updateVideosVisible();
				removeInstructionsWav();
				break;
			case interpreterNotAcceptingCalls:
				takeCallsButton.setHTML("<span>Accept<br>Calls</span>");
				if (!callActive) interpreter.setVisible(false);
				notTakingCallsLabel.setVisible(true);
				break;
			case agentDisconnected:
				agentOnCall = false;
				updateVideosVisible();
				agento.removeFromParent();
				for (int i=0; i<4; i++) {
					Caller thirdParty = thirdPartyList.get(i);
					if ("".equals(thirdParty.getId())) thirdParty.removeFromParent();
					else connectedContainer.add(thirdParty);
				}
				if (!agentOnCall && interpreterValidated) {
					requestAgentButton.setEnabled(true);
					requestAgentButton.addStyleName("blue");
				} else {
					requestAgentButton.setEnabled(false);
					requestAgentButton.removeStyleName("blue");
				}
				agentRequested = false;
				break;
			case callStatusUpdate:
				for (int i=0; i<4; i++) {
					Caller thirdParty = thirdPartyList.get(i);
					if ("".equals(thirdParty.getId())) thirdParty.removeFromParent();
					else if (onHold) {
						dialingContainer.add(thirdParty);
					} else {
						connectedContainer.add(thirdParty);
					}
				}
				if (!agentOnCall && interpreterValidated) {
					requestAgentButton.setEnabled(true);
					requestAgentButton.addStyleName("blue");
				} else {
					requestAgentButton.setEnabled(false);
					requestAgentButton.removeStyleName("blue");
				}
				break;
			case Logout:
				logout(data);
				agentRequested = false;
				webPhoneEnabled = false;
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
				websocket.send(InterpreterMessages.SessionIdResponse.toString()+":"+sessionId);
				break;
			default: System.out.println("NOT IMPLEMENTED IN CLIENT");
			}
		}
	}
	
	static boolean lastReasonPopulated = false;
	
	private static void insertInstructionsWav(String instructionsText) {
		if (!lastReasonPopulated) {
			lastReasonPopulated = true;
			Label label = new Label();
			label.getElement().setInnerHTML("<centered><b>Special Instructions: </b>"+instructionsText+"</centered> ");
			lastReasonLabel.add(label);
//			Button button = new Button();
//			button.setHTML("replay");
//			final AudioElement audio = Document.get().createAudioElement();
//			audio.setSrc("instructions/"+instructionsWav);
//			audio.setAutoplay(true);
//			button.addClickHandler(new ClickHandler() {
//				@Override
//				public void onClick(ClickEvent event) {
//					audio.play();
//				}
//			});
//			lastReasonLabel.add(button);
//			lastReasonLabel.getElement().appendChild(audio);
		}
	}

	private static void removeInstructionsWav() {
		if (lastReasonPopulated) {
			lastReasonPopulated = false;
			lastReasonLabel.clear();
		}
	}

	private static void sendResponse(String reason) {
		log("sendResponse: "+sipIncoming+" "+acceptedManualCall+" "+acceptedConnectCall+" "+rejectedManualCall+" "+rejectedConnectCall+" "+askToAccept);
		
		if (sipIncoming && rejectedManualCall) {
			sipIncoming = false;
			rejectedManualCall = false;
			log("sendResponse: rejected sipIncoming rejectedManualCall");
			sipCall.hangup();
			interpreterService.rejectCall(sessionId, reason, new AsyncCallback<Void>() {
				@Override
				public void onFailure(Throwable caught) {
					error(caught);
				}
				@Override
				public void onSuccess(Void result) {
				}
			});
		}
		
		if (askToAccept && rejectedConnectCall) {
			askToAccept = false;
			rejectedConnectCall = false;
			log("sendResponse: rejected Connect Call askToAccept");
			sipCall.hangup();
			interpreterService.rejectCall(sessionId, reason, new AsyncCallback<Void>() {
				@Override
				public void onFailure(Throwable caught) {
					error(caught);
				}
				@Override
				public void onSuccess(Void result) {
				}
			});
		}
		
		if (sipIncoming && acceptedManualCall) {
			sipIncoming = false;
			acceptedManualCall = false;
			log("sendResponse: accepted Manual SIP call");
			sipCall.answer();
			log("sendResponse4: "+sipIncoming+" ");
		}
		
		if (askToAccept && acceptedConnectCall) {
			askToAccept = false;
			acceptedConnectCall = false;
			log("sendResponse: accepted askToAccept Connet");
			sipCall.answer();
			interpreterService.acceptCall(sessionId, new AsyncCallback<Void>() {
				@Override
				public void onFailure(Throwable caught) {
					error(caught);
				}
				@Override
				public void onSuccess(Void result) {
				}
			});
		}
	}
	
	private static void guiAskToAccept() {
		if (!rejectTimer.isRunning()) {
			timedReject = false;
			rejectTimer.schedule(12000);
			showActionPanel();
			acceptCallButton.setEnabled(true);
			acceptCallButton.addStyleName("blue");
			rejectCallButton.setEnabled(true);
			rejectCallButton.addStyleName("red");
			showRejectButton();
			hangupButton.setEnabled(true);
			hangupButton.addStyleName("red");
			requestAgentButton.setEnabled(false);
			requestAgentButton.removeStyleName("blue");
			noAgentsSound.play();
		}
	}

	public static native void log(String msg) /*-{
		$wnd.console.log(msg);
	}-*/;
	
	static private void startVideoRoom() {
		if (videoEnabled && videoRoom == null && janusVideoRoomServer != null && janusVideoRoomNumber != null
				&& !janusVideoRoomServer.equals("null")) {
			info("Start Video Call");
		} else return;
//		videoAgent = Document.get().getElementById("videoagent").cast();
//		videoCustomer = Document.get().getElementById("videocustomer").cast();
//		videoInterpreter = Document.get().getElementById("videointerpreter").cast();
		customerVideo.setVisible(false);
		agentVideo.setVisible(false);
		videoRoomID = UUID.uuid();
		
		if (videoRoom != null) {
			stopVideoRoom();
		}
		
		videoRoom = new Janus(new JanusListener() {
			@Override
			public void initSuccess() {
				log("videoRoom initSuccess()");
				showVideoContainer();
				videoRoom.videoRoomPluginAttach(new JanusVideoRoomListener() {
					@Override
					public void videoRoomPluginSuccess() {
						log("videoRoomPluginSuccess");
						videoRoom.videoRoomRegisterUsername("interpreter", "videointerpreter", Integer.parseInt(janusVideoRoomNumber));
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
					}

					@Override
					public void videoRoomNewParticipant(String id, String display) {
						log("videoRoomNewParticipant: "+id+", "+display);
						if ("customer".equals(display)) {
							customerVideo.connecting();
							customerVideo.setVisible(true);
							videoRoom.addNewParticipant(id, display, "videocustomer", Integer.parseInt(janusVideoRoomNumber));
							interpreterService.videoSessionStarted(sessionId, new AsyncCallback<Void>() {
								@Override
								public void onFailure(Throwable caught) {
								}
								@Override
								public void onSuccess(Void result) {
								}
							});
						}
						if ("agent".equals(display)) {
							agentVideo.setVisible(true);
							videoRoom.addNewParticipant(id, display, "videoagent", Integer.parseInt(janusVideoRoomNumber));
						}
					}

					@Override
					public void videoRoomSlowlink(String uplink, String nacks) {
						log("videoRoomSlowlink: "+uplink+", "+nacks);
					}

					@Override
					public void videoRoomLocalStream(JavaScriptObject stream) {
						log("videoRoomLocalStream: "+stream);
					}

					@Override
					public void videoRoomOnCleanup() {
						customerVideo.notConnecting();
						log("videoRoomOnCleanup");
					}
				});
			}
			@Override
			public void error(String reason) {
				log("videoRoom error: "+reason);
			}
			@Override
			public void destroyed() {
				log("videoRoom destroyed");
			}
		}, janusVideoRoomServer, turnServer, turnUsername, turnPassword); //"https://janus.icoa.com:8089/janus");
		videoRoom.id = videoRoomID;
	}
	
	static public void sipHangup() {
		log("sipHangup");
		if (rejectCallButton.isEnabled()) {
			rejectCallAction("clicked customer hangup (x)");
			logout("Logged out as part of reject action.");
		}
		acceptedManualCall = false;
		acceptedConnectCall = false;
		rejectedManualCall = false;
		rejectedConnectCall = false;
		sipIncoming = false;
		askToAccept = false;
		//sendAceeptResponse = false;
		if (sipCall != null) {
			sipCall.hangup();
		}
	}
	
	static private void stopVideoRoom() {
		hideVideoContainer();
		if (videoRoom != null) {
			videoRoom.videoRoomHangup();
			videoRoom.destroy();
			videoRoom = null;
		}
//		VideoElement videoAgent = Document.get().getElementById("videoagent").cast();
//		VideoElement videoCustomer = Document.get().getElementById("videocustomer").cast();
//		VideoElement videoInterpreter = Document.get().getElementById("videointerpreter").cast();
//		if (videoAgent != null) {
//			videoAgent.setSrc("");
//		}
//		if (videoCustomer != null) {
//			videoCustomer.setSrc("");
//		}
//		if (videoInterpreter != null) {
//			videoInterpreter.setSrc("");
//		}
	}
	
	static void error(Throwable caught) {
		info(caught.getMessage());
		if (caught instanceof TLVXError) {
			TLVXError error = (TLVXError) caught;
			if (error.forceLogout) {
				//messageTimer.cancel();
				//blink.setVisible(false);
				clearContent();
				showLoginForm();
				info("We have detected that you were previously logged out out.  Possibly you had 2 windows open and one logged out or the server timed you out.");
			}
		}
	}

	private static void stopTakingCalls() {
		interpreterService.stopTakingCalls(sessionId, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				error(caught);
			}
			@Override
			public void onSuccess(Void x) {
				takeCallsButton.setHTML("<span>Accept<br>Calls</span>");
				if (!callActive) interpreter.setVisible(false);
				notTakingCallsLabel.setVisible(true);
			}
		});
	}

	private static void removeActionPanel() {
		center.remove(interpretersPanel);
		center.remove(actionPanel);
		center.remove(actionToolBar);
	}

	private static void showActionPanel() {
		//historyStore.clear();
		//availableStore.clear();
		center.add(interpretersPanel);
		center.add(actionPanel);
		center.add(actionToolBar);
		//RootPanel.get("wrapper").add(interpretersPanel);
		//RootPanel.get("wrapper").add(actionPanel);
		accessCode.setFocus(true);
	}
	
	private static void setUpVideoPanel() {
		videoContainer.clear();
		interpreterVideo = new Video("interpreter");
		interpreterVideo.addPauseClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (videoRoom != null) {
					videoRoom.videoRoomUnpublishOwnFeed();
				}
			}
		});
		interpreterVideo.addPlayClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (videoRoom != null) {
					videoRoom.videoRoomPublishOwnFeed(true);
				}
			}
		});
		customerVideo = new Video("customer");
		customerVideo.addPauseClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				interpreterService.playCustomerVideo(sessionId, false, new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
					}
					@Override
					public void onSuccess(Void result) {
					}
				});
			}
		});
		customerVideo.addPlayClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				interpreterService.playCustomerVideo(sessionId, true, new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
					}
					@Override
					public void onSuccess(Void result) {
					}
				});
			}
		});
		agentVideo = new Video("agent");
		videoContainer.add(interpreterVideo);
		videoContainer.add(customerVideo);
		//videoContainer.add(agentVideo);
	}
	
	private static void showVideoContainer() {
		center.add(videoContainer);
	}
	
	private static void hideVideoContainer() {
		center.remove(videoContainer);
//		videoContainer.clear();
//		interpreterVideo = new Video("interpreter");
//		interpreterVideo.setHeight("200px");
//		customerVideo = new Video("customer");
//		customerVideo.setHeight("200px");
//		agentVideo = new Video("agent");
//		agentVideo.setHeight("200px");
//		videoContainer.add(interpreterVideo);
//		videoContainer.add(customerVideo);
//		videoContainer.add(agentVideo);
//		videoAgent = Document.get().getElementById("videoagent").cast();
//		videoCustomer = Document.get().getElementById("videocustomer").cast();
//		videoInterpreter = Document.get().getElementById("videointerpreter").cast();
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

//	@Override
//	public void scriptLoaded() {
//		//info("Script Loaded");
//		sipCall.init();
//	}

//	@Override
//	public void scriptLoadErrror(Exception reason) {
//		info("Script Load Error "+reason);
//	}

	@Override
	public void sipPluginSuccess() {
		//info("sip plugin success");
		String password = Tlvx_interpreter.email.substring(0, Tlvx_interpreter.email.indexOf("@"));
		sipCall.registerSipEndpoint("sip:"+Tlvx_interpreter.phonenumber+"@"+Tlvx_interpreter.sipRegistrationServer, password, "sip:"+Tlvx_interpreter.sipRegistrationServer);
	}

	@Override
	public void sipPluginError(String error) {
		GWT.log("sip plugin error "+error);
	}
	
	@Override
	public void sipRegistrationFailed(String reason) {
		showMessage("SIP Error", reason);
//		logout("Logging Out");
//		enableVideoButton.setHTML("<span>Enable<br>Web Phone</span>");
//		enableBrowserPhoneButton.setStyleName("mid-btn");
//		enableBrowserPhoneButton.addStyleName("red");
//		enableBrowserPhoneButton.setEnabled(true);
	}

	@Override
	public void sipPluginConsentDialog(Boolean on) {
		//info("sip plugin consent Dialog "+on);
	}

	@Override
	public void sipIncomingCall(String username, Boolean audio, Boolean video) {
		GWT.log("sip incoming call "+username+" "+audio+" "+video);
		sipIncoming = true;
		sendResponse(null);
	}
	
	@Override
	public void sipAccepted(String username, String reason) {
		info("Call Connected");
		callActive = true;
		sendResponse(null);
		if (videoEnabled && videoRoom == null && janusVideoRoomServer != null && janusVideoRoomNumber != null) {
			startVideoRoom();
		}
	}

	@Override
	public void sipRegistered(String username) {
		interpreterService.setWebPhoneEnabled(sessionId, true, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				error(caught);
			}
			@Override
			public void onSuccess(Void result) {
				info("Web Phone Enabled");
				webPhoneEnabled = true;
				interpreterService.updateStatus(sessionId, new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						error(caught);
					}
					@Override
					public void onSuccess(Boolean v) {
						if (v!=null && v && !videoEnabled) {
							testVideoButton.setHTML("<span>Test<br>Video</span>");
							testVideoButton.setEnabled(true);
							testVideoButton.addStyleName("orange");
							enableVideoButton.setHTML("<span>Enable<br>Video</span>");
							enableVideoButton.setEnabled(true);
							enableVideoButton.addStyleName("red");
							enableVideoButton.setEnabled(true);
							enableVideoButton.addStyleName("red");
						}
						checkNotificationSettings();
					}
				});
			}
		});

	}

	@Override
	public void sipDetached() {
		GWT.log("sipDetached");
//		logout("Sip Detached");
//		interpreterService.setWebPhoneEnabled(sessionId, false, new AsyncCallback<Void>() {
//			@Override
//			public void onFailure(Throwable caught) {
//				error(caught);
//			}
//			@Override
//			public void onSuccess(Void result) {
//				info("Web Phone Disabled");
//				changeNumberButton.setEnabled(true);
//				changeNumberButton.addStyleName("orange");
////				enableBrowserPhoneButton.setEnabled(true);
////				enableBrowserPhoneButton.addStyleName("red");
//				webPhoneEnabled = false;
//			}
//		});
	}

	@Override
	public void sipHangup(String username, String reason) {
		GWT.log("sipHangup: "+username+" "+reason);
		info("Call disconnected.");
		callId = "";
		callActive = false;
		acceptedManualCall = false;
		acceptedConnectCall = false;
		rejectedManualCall = false;
		rejectedConnectCall = false;
		sipIncoming = false;
		askToAccept = false;
		//sendAceeptResponse = true;
		if (takeCallsButton.getHTML().equalsIgnoreCase("<span>Accept<br>Calls</span>")) interpreter.setVisible(false);
		//sipCall.destroy();
		removeActionPanel();
		AudioElement siplocalaudio = Document.get().getElementById("siplocalaudio").cast();
		AudioElement sipremoteaudio = Document.get().getElementById("sipremoteaudio").cast();
		//siplocalaudio.setSrc("");
		//sipremoteaudio.setSrc("");
		stopVideoRoom();
	}

    private static Timer reconnectTimer = new Timer() {
		@Override
		public void run() {
			info("Server connection lost, attempting reconnect.");
			websocket = new Websocket(wsUrl+"?sid="+sessionId);
			websocket.addListener(thisTlvx);
			websocket.open();
		}
    };
	
	int closeCount = 0;
	@Override
	public void onClose(com.telelanguage.interpreter.client.websockets.CloseEvent event) {
		//messageTimer.schedule(1000);
		try {
			closeCount++;
			if (callActive && closeCount < 10) {
				reconnectTimer.schedule(2000);
			} else {
				logout("Connection Lost");
			}
		} catch (Exception e) {
			GWT.log("Error onClose", e);
		}
		acceptedManualCall = false;
		acceptedConnectCall = false;
		rejectedManualCall = false;
		rejectedConnectCall = false;
		sipIncoming = false;
		askToAccept = false;
	}

	@Override
	public void onMessage(String msg) {
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
		GWT.log("Connection opened.");
	}
}
