package com.telelanguage.tlvx.client;

import java.awt.Event;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.media.client.Audio;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestBox.DefaultSuggestionDisplay;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextBox;
import com.telelanguage.tlvx.client.Caller.TYPE;
import com.telelanguage.tlvx.model.CallInformation;
import com.telelanguage.tlvx.model.InterpreterLine;
import com.telelanguage.tlvx.model.Language;
import com.telelanguage.tlvx.model.QuestionItem;

public class Tlvx implements EntryPoint {

	public static final AgentServiceAsync agentService = GWT.create(AgentService.class);
	
	private static FlowPanel header = new FlowPanel();
	private static FlowPanel center = new FlowPanel();
	private static HorizontalPanel agentContainer = new HorizontalPanel();
	private static HorizontalPanel connectedContainer = new HorizontalPanel();
	private static HorizontalPanel dialingContainer = new HorizontalPanel();
	private static HorizontalPanel fillContainer = new HorizontalPanel();
	private static FormPanel loginForm = new FormPanel();
	private static FlowPanel interpretersPanel = new FlowPanel();
	private static FlowPanel actionPanel = new FlowPanel();
	private static Grid statusBar = new Grid(1,5);
	private static Caller agent = new Caller(Caller.TYPE.Agent, null);
	private static Caller customer = new Caller(Caller.TYPE.Client, null);
	private static Caller interpreter = new Caller(Caller.TYPE.Intreperter, null);
	private static boolean manual = false;
	private static Map<String, Caller> thirdParties = new HashMap<String, Caller>();
	private static Image connecting = new Image("images/arrows.png");
	private static Button requeueCallButton = new Button();
	private static Button completeCallButton = new Button();
	private static Button takeCallsButton = new Button();
	private static Button changeNumberButton = new Button();
	private static Button dialThirdPartyButton = new Button();
	private static Button connectButton = new Button("Connect");
	private static Button manualDialButton = new Button("Dial");
	private static TextBox accessCode = new TextBox();
	private static TextBox searchInterpreter = new TextBox();
	private static Button validateAccessCode = new Button();
	private static Button clearSearchInterprter = new Button();
	private static MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
	private static SuggestBox languageCombo = new SuggestBox(oracle);
	private static ListBox interpreterGender = new ListBox();
	private static CheckBox interpreterVideo = new CheckBox();
	private static FlowPanel additionalQuestionsLeft = new FlowPanel();
	private static FlowPanel additionalQuestionsRight = new FlowPanel();
    private static AdditionalQuestion departmentQuestion = null;
    private static CallInformation callInformation = null;
    private static Label customerNameLabel = new Label();
    private static Label lastReasonLabel = new Label();
    private static boolean accessCodeValidated = false;
    private static FlowPanel actionToolBar = new FlowPanel();
    private static FlexTable availableGrid = new FlexTable();
    private static List<InterpreterLine> availableInterpreters = null;
    private static FlexTable historyGrid = new FlexTable();
    private static List<String> languages;
    DialogBox phoneNumberBox;
    TextBox phoneNumber;
    private static boolean updatedNumber = false;
    private static boolean priorityCall = false;
    private static boolean videoCall = false;
    private static Label notTakingCallsLabel = new Label("Not Available to Take Calls");
    private static String priorThridPartyNumber = "";
    public static String sessionId = UUID.uuid();
    
    private static Timer messageTimer = new Timer() {
		@Override
		public void run() {
			try {
				GWT.log("run()");
				agentService.getMessages(sessionId, new AsyncCallback<List<String>>() {
					@Override
					public void onFailure(Throwable caught) {
						messageTimer.schedule(5000);
						disconnected();
						showMessage("Error Connecting", "Server is unreachable.");
						GWT.log("Error connecting to server.");
					}
					@Override
					public void onSuccess(List<String> result) {
						messageTimer.schedule(1);
						if (result != null) {
							processMessages(result);
							connected();
						}
					}
				});
			} catch (Exception e) {
				messageTimer.schedule(5000);
				GWT.log("Exception in message timer", e);
			}
			GWT.log("done.");
		}
    };
	
	public static String callId = "";
	public static String name = "";
	public static String email = "";
	public static boolean callActive = false;
	public static Tlvx thisTlvx;

	static String numberMode = "";
	
	static Audio noAgentsSound; 
	static Audio priorityCallSound;

	private static Image blink = new Image("telelanguage/images/exclamation.png");
	
	public void onModuleLoad() {
		thisTlvx = this;
		setUpBanner();
		setUpLoginForm();
		setUpAgentPanel();
		setUpInterpretersPanel();
		setUpActionPanel();
		
		noAgentsSound = Audio.createIfSupported();
		if (noAgentsSound != null) noAgentsSound.setSrc("telelanguage/sounds/noagents.mp3");
		priorityCallSound = Audio.createIfSupported();
		priorityCallSound.setSrc("telelanguage/sounds/prioritycall.mp3");
		
		showLoginForm();
		
		agentService.getRelease(new AsyncCallback<String>() {
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
	}
	
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
		blink.setVisible(false);
	}
	
	private static void clearContent() {
		RootPanel.get("wrapper").clear();
	}
	
	private native static void reloadTrue() /*-{
		$wnd.location.reload(true);
	}-*/;

	private void setUpLoginForm() {
		FlowPanel loginBox = new FlowPanel();
		loginBox.setStyleName("login-box");
		loginForm.add(loginBox);

		FlowPanel agentTop = new FlowPanel();
		agentTop.setStyleName("agent-top");
		FlowPanel group = new FlowPanel();
		group.addStyleName("logogroup");
		agentTop.add(group);
//		FlowPanel fl = new FlowPanel();
//		fl.setStyleName("logo");
//		group.add(fl);
//		Label fr = new Label("Telephonic Interpretation Services");
//		fr.setStyleName("name");
//		group.add(fr);
		FlowPanel agentLog = new FlowPanel();
		agentLog.setStyleName("agent-log");
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
		Label passwordLabel = new Label("Password");
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
		
		Button loginButton = new Button("Login");
		loginButton.setStyleName("login-btn");
		loginButton.addStyleName("orange");
		loginButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (email.getValue().length()>0 && password.getValue().length()>0) {
					Tlvx.email = email.getValue();
					agentService.logon(sessionId, email.getValue(), password.getValue(), new AsyncCallback<Integer>() {
						@Override
						public void onFailure(Throwable caught) {
							error(caught);
						}
						@Override
						public void onSuccess(Integer result) {
							if (result == 0) {
								info("Your email or password is invalid,\n contact the administrator.");
							} else if (result != ClientVersion.version) {
								reloadTrue();
							} else {
								login();
							}
						}
					});
				} else {
					showMessage("Invalid", "You must enter an email and a password.");
				}
			}
		});
		actionP.add(loginButton);
	}
	
	private static void logout(final String message) {
		messageTimer.cancel();
		agentService.logout(sessionId, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				error(caught);
			}
			@Override
			public void onSuccess(Void result) {
				blink.setVisible(false);
				clearContent();
				showLoginForm();
				if (message.length() == 0) info("You are now logged out.");
				else info(message);
			}
		});
	}
	
	private void login() {		
		blink.setVisible(true);
		stopTakingCalls();
		//dialThirdPartyButton.set();
		clearContent();
		showAgentPanel();
		info("You are now logged in.");
		messageTimer.schedule(1);
		agentService.updateStatus(sessionId, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				error(caught);
			}
			@Override
			public void onSuccess(Void v) {

			}
		});
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
		group.addStyleName("logogroup");
		agentTop.add(group);
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
				if (takeCallsButton.getHTML().equalsIgnoreCase("<span>Accept<br>Calls</span>")) {
					if (!updatedNumber) {
						showMessage("Warning","You must first update your number before taking calls.");
					} else {
						agentService.startTakingCalls(sessionId, new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
								error(caught);
							}
							@Override
							public void onSuccess(Void x) {
								takeCallsButton.setHTML("<span>Stop Accepting<br>Calls</span>");
								agent.setVisible(true);
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

		changeNumberButton.setHTML("<span>Change<br>Number</span>");
		changeNumberButton.setStyleName("mid-btn");
		changeNumberButton.addStyleName("orange");
		changeNumberButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				newPhoneNumberBox();
				numberMode = "updateNumber";
				phoneNumber.setValue("");
				phoneNumberBox.show();
			}
		});
		btnWrap.add(changeNumberButton);
		dialThirdPartyButton.setHTML("<span>Dial<br>Third Party</span>");
		dialThirdPartyButton.setStyleName("mid-btn");
		//dialThirdPartyButton.addStyleName("red");
		dialThirdPartyButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				newPhoneNumberBox();
				numberMode = "thirdParty";
				phoneNumber.setValue(priorThridPartyNumber);
				phoneNumberBox.show();
			}
		});
		btnWrap.add(dialThirdPartyButton);
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
		connectedContainer.add(agent);
		center.add(agentContainer);
		
		customerNameLabel.setStyleName("customerNameLabel");
		center.add(customerNameLabel);
		lastReasonLabel.setStyleName("lastReasonLabel");
		center.add(lastReasonLabel);
		notTakingCallsLabel.setStyleName("notTakingCallsLabel");
		center.add(notTakingCallsLabel);
		notTakingCallsLabel.setVisible(true);
	}
	
	private void newPhoneNumberBox() {
		phoneNumberBox = new DialogBox();
		phoneNumberBox.setTitle("Phone Number");
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
				if ("updateNumber".equalsIgnoreCase(numberMode))
					agentService.updateNumber(sessionId, phoneNumber.getValue(), new AsyncCallback<Void>() {
						@Override
						public void onFailure(Throwable caught) {
							info("Unable to update to this number, please try a different number, it may already be in use.");
						}
						@Override
						public void onSuccess(Void result) {
							updatedNumber = true;
						}
					});
				if ("thirdParty".equalsIgnoreCase(numberMode))
					if (phoneNumber.getValue().length() > 2)
						agentService.dialThirdParty(sessionId, callId, phoneNumber.getValue(), new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
								info("Unable to place call to 3rd party, please try later. "+caught.getMessage());
							}
							@Override
							public void onSuccess(Void result) {
								Caller thirdParty = thirdParties.get("dialing");
								if (thirdParty == null) {
									thirdParty = new Caller(TYPE.ThridParty, "dialing");
								}
								priorThridPartyNumber = phoneNumber.getValue();
								connectedContainer.remove(thirdParty);
								dialingContainer.add(connecting);
								dialingContainer.setCellWidth(connecting, "250px");
								dialingContainer.add(thirdParty);
								thirdParty.setBottomLabel(phoneNumber.getValue());
								thirdParty.setMuteVisibility(false);
								thirdParty.setHangupVisibility(false);
								dialingContainer.setCellWidth(thirdParty, "250px");
							}
						});
				numberMode = "";
				phoneNumberBox.hide();
			}
		});
		phoneNumberBoxContainer.add(okButton);
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
	
	private void validateAccessCode() {
		validateAccessCode.setEnabled(false);
		accessCode.setEnabled(false);
		additionalQuestionsLeft.clear();
		additionalQuestionsRight.clear();
		connectButton.setEnabled(false);
		connectButton.removeStyleName("red");
		manualDialButton.setEnabled(false);
		manualDialButton.removeStyleName("blue");
		completeCallButton.setEnabled(false);
		completeCallButton.removeStyleName("blue");
		accessCodeValidated = false;
		agentService.getCallInformationByCallIdAccessCode(sessionId, callId, accessCode.getText(),new AsyncCallback<CallInformation>() {
			@Override
			public void onFailure(Throwable caught) {
				validateAccessCode.setEnabled(true);
				accessCode.setEnabled(true);
				error(caught);
			}
			@Override
			public void onSuccess(CallInformation result) {
				validateAccessCode.setEnabled(true);
				accessCode.setEnabled(true);
				if (result != null) {
					accessCodeValidated = true;
					callInformation = result;
					languageCombo.setFocus(true);
					showCustomerSpecificQuestions();
				} else {
					info("Not a valid access code.");
				}
			}
		});
	}
	
	private void setUpActionPanel() {
		FlowPanel questionGroup = new FlowPanel();
		questionGroup.setStyleName("group");
		questionGroup.getElement().getStyle().setMarginBottom(29, Unit.PX);
		actionPanel.add(questionGroup);
		FlowPanel leftQuestions = new FlowPanel();
		leftQuestions.setStyleName("form-wrap");
		leftQuestions.addStyleName("fl");
		questionGroup.add(leftQuestions);
		FlowPanel rightQuestions = new FlowPanel();
		rightQuestions.setStyleName("form-wrap");
		rightQuestions.addStyleName("fr");
		questionGroup.add(rightQuestions);
		
		FlowPanel accessCodeH = new FlowPanel();
		accessCodeH.setStyleName("form-wrap-p");
		accessCode.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				connectButton.setEnabled(false);
				connectButton.removeStyleName("red");
				manualDialButton.setEnabled(false);
				manualDialButton.removeStyleName("blue");
				completeCallButton.setEnabled(false);
				completeCallButton.removeStyleName("blue");
				accessCodeValidated = false;
				additionalQuestionsLeft.clear();
				additionalQuestionsRight.clear();
				if (event.getNativeEvent().getKeyCode() == 13 || event.getNativeEvent().getKeyCode() == Event.TAB) {
					validateAccessCode();
					event.stopPropagation();
					event.preventDefault();
				}
			}
		});
		accessCodeH.add(new InlineLabel("Access Code:"));
		accessCode.setStyleName("s1");
		accessCode.addStyleName("form-wrap-input");
		accessCodeH.add(accessCode);
		validateAccessCode.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				validateAccessCode();
			}
		});
		validateAccessCode.setHTML("Validate");
		validateAccessCode.setStyleName("validateAccessCodeButton");
	    accessCodeH.add(validateAccessCode);
	    leftQuestions.add(accessCodeH);
		agentService.getLanguages(new AsyncCallback<List<Language>>() {
			@Override
			public void onFailure(Throwable caught) { }
			@Override
			public void onSuccess(List<Language> result) {
				languages = new ArrayList<String>();
				oracle.clear();
				for(Language language : result) {
					oracle.add(language.languageName);
					languages.add(language.languageName);
				}
			}
		});
		languageCombo.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				languageTextFieldChange();
			}
		});
		languageCombo.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeEvent().getKeyCode() == 13 || event.getNativeEvent().getKeyCode() == Event.TAB) {
					languageTextFieldChange();
					event.stopPropagation();
					event.preventDefault();
				}
			}
		});
		languageCombo.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeEvent().getKeyCode() == 13 || event.getNativeEvent().getKeyCode() == Event.TAB) {
					event.stopPropagation();
					event.preventDefault();
				} else {
					languageTextFieldChange();
				}
			}
		});
		languageCombo.addSelectionHandler(new SelectionHandler<Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				languageTextFieldChange();
			}
		});
		FlowPanel languageH = new FlowPanel();
		languageH.setStyleName("form-wrap-p");
		languageH.add(new InlineLabel("Language:"));
		languageH.add(languageCombo);
		languageCombo.setStyleName("s1l");
		languageCombo.addStyleName("form-wrap-input");
		interpreterGender.addItem("");
		interpreterGender.addItem("F");
		interpreterGender.addItem("M");
		interpreterGender.setStyleName("s1g");
		interpreterGender.addStyleName("form-wrap-input");
		languageH.add(interpreterGender);
		interpreterVideo.setHTML("<img src=\"images/video-16.png\"/>");
		interpreterVideo.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				manualDialButton.click();
			}
		});
		languageH.add(interpreterVideo);
	    leftQuestions.add(languageH);
	    connectButton.setStyleName("connectButton");
	    manualDialButton.setStyleName("manualDialButton");
	    connectButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				synchronized(this) {
					if (connectedContainer.getWidgetIndex(interpreter)>-1 || dialingContainer.getWidgetIndex(interpreter)>-1) {
						info("Hang up the current interpreter before dialing another.");
						return;
					}
					manual = false;
					callInformation.language = languageCombo.getValue();
					callInformation.interpreterGender = interpreterGender.getValue(interpreterGender.getSelectedIndex());
					callInformation.interpreterVideo = interpreterVideo.getValue();
					connectedContainer.remove(interpreter);
					dialingContainer.add(connecting);
					interpreter.setMuteVisibility(false);
					interpreter.setHangupVisibility(false);
					dialingContainer.add(interpreter);
					agentService.callLanguage(sessionId, callId, callInformation.language, callInformation.interpreterGender, callInformation.interpreterVideo, new AsyncCallback<Boolean>() {
						@Override
						public void onSuccess(Boolean result) {
							if ((result != null) && result) {
								dialingContainer.add(connecting);
								interpreter.setMuteVisibility(false);
								interpreter.setHangupVisibility(true);
								dialingContainer.add(interpreter);
							} else {
								dialingContainer.remove(connecting);
								dialingContainer.remove(interpreter);
								updateInterpreterLists();
							}
						} 
						@Override
						public void onFailure(Throwable caught) {
							error(caught);
							dialingContainer.remove(connecting);
							dialingContainer.remove(interpreter);
							updateInterpreterLists();
						}
					});
				}
			}
		});
	    languageH.add(connectButton);
	    manualDialButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				callInformation.language = languageCombo.getValue();
				callInformation.interpreterGender = interpreterGender.getValue(interpreterGender.getSelectedIndex());
				callInformation.interpreterVideo = interpreterVideo.getValue();
				updateInterpreterLists();
			}
		});
	    languageH.add(manualDialButton);
	    leftQuestions.add(additionalQuestionsLeft);
	    rightQuestions.add(additionalQuestionsRight);
		
		actionToolBar.setStyleName("btn-wrap");
		
		
		requeueCallButton.setStyleName("mid-btn");
		requeueCallButton.addStyleName("blue");
		requeueCallButton.setHTML("<span>Requeue<br>Call</span>");
		requeueCallButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				requeueCallButton.setEnabled(false);
				completeCallButton.setEnabled(false);
				loadCallInformation();
				agentService.requeueRequest(sessionId, callInformation, new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						error(caught);
						checkCompleteCallStatus();
					}
					@Override
					public void onSuccess(Void result) {
						info("Call requeued.");
					}
				});
			}
		});
		actionToolBar.add(requeueCallButton);
		
		Button personalHoldButton = new Button();
		personalHoldButton.setStyleName("mid-btn");
		personalHoldButton.addStyleName("grey");
		personalHoldButton.setHTML("<span>Personal<br>Hold</span>");
		personalHoldButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				loadCallInformation();
				agentService.personalHoldRequest(sessionId, callInformation, new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						error(caught);
					}
					@Override
					public void onSuccess(Void result) {
						info("Call put on personal hold.");
					}
				});
			}
		});
		actionToolBar.add(personalHoldButton);
		
		final DialogBox transferBox = new DialogBox();
		FlowPanel transferBoxContent = new FlowPanel();
		transferBox.setTitle("Transfer Call");
		transferBox.center();
		transferBox.hide();
		final TextBox dest = new TextBox();
		transferBoxContent.add(new Label("Phone Number"));
		transferBoxContent.add(dest);
		Button okButton = new Button("OK");
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				transferBox.hide();
				if (dest.getValue().length()>2) {
					agentService.transfer(sessionId, callId, dest.getValue(), new AsyncCallback<Void>() {
						@Override
						public void onFailure(Throwable caught) {
							error(caught);
						}
						@Override
						public void onSuccess(Void result) {
							info("Call transferred.");
						}
					});
				}
			}
		});
		transferBoxContent.add(okButton);
		Button cancel = new Button("Cancel");
		cancel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				transferBox.hide();
			}
		});
		transferBoxContent.add(cancel);
		transferBox.add(transferBoxContent);
		Button transferButton = new Button();
		transferButton.setStyleName("mid-btn");
		transferButton.addStyleName("grey");
		transferButton.setHTML("<span>Transfer<br>Call</span>");
		transferButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				//dest.clear();
				transferBox.show();
			}
		});
		actionToolBar.add(transferButton);
		
		completeCallButton.setStyleName("big-btn");
		//completeCallButton.addStyleName("blue");
		completeCallButton.setHTML("<span>Complete Call</span>");
		actionToolBar.add(completeCallButton);
		completeCallButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				checkCompleteCallStatus();
				if (completeCallButton.isEnabled()) {
					completeCallButton.setEnabled(false);
					requeueCallButton.setEnabled(false);
					completeCall(false);
				}
			}
		});
	}
	
	public static void focusNextInvalidQuestion() {
		if (callInformation.hasDepartments && !departmentQuestion.isValid()) {
			departmentQuestion.focus();
			return;
		}
		for (int i=0;i<(additionalQuestionsLeft.getWidgetCount()+additionalQuestionsRight.getWidgetCount());i++) {
			AdditionalQuestion aq = getQuestion(i);
			if (!aq.isValid()) {	
				aq.focus();
				return;
			}
		}
		//if (completeCallButton.isEnabled()) completeCall(false);
	}
	
	private static void languageTextFieldChange() {
		if (callInformation.customerId != null && callInformation.customerId.length()>5 && languageValid()) {
			((DefaultSuggestionDisplay)languageCombo.getSuggestionDisplay()).hideSuggestions();
			connectButton.setEnabled(true);
			connectButton.addStyleName("red");
			manualDialButton.setEnabled(true);
			manualDialButton.addStyleName("blue");
			focusNextInvalidQuestion();
		} else {
			connectButton.setEnabled(false);
			connectButton.removeStyleName("red");
			manualDialButton.setEnabled(false);
			manualDialButton.removeStyleName("blue");
		}
		checkCompleteCallStatus();
	}
	
	private static boolean languageValid() {
		//System.out.println(languageCombo.getValue());
		return languages.contains(languageCombo.getValue());
	}
	
	public static void completeCall(boolean hangupCaller) {
		loadCallInformation();
		agentService.completeCall(sessionId, callInformation, hangupCaller, new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(Throwable caught) {
				error(caught);
				checkCompleteCallStatus();
			}
			@Override
			public void onSuccess(Boolean result) {
				if ((result != null) && result) {
					info("Call completed.");
				} else {
					updateInterpreterLists();
				}
			}
		});
	}
	
	private static AdditionalQuestion getQuestion(int index) {
		switch(index) {
		case 0: if (additionalQuestionsRight.getWidgetCount()>0) {
			return (AdditionalQuestion) additionalQuestionsRight.getWidget(0); 
		}
		break;
		case 1: if (additionalQuestionsRight.getWidgetCount()>1) {
			return (AdditionalQuestion) additionalQuestionsRight.getWidget(1);  
		}
		break;
		case 2: if (additionalQuestionsLeft.getWidgetCount()>0) {
			return (AdditionalQuestion) additionalQuestionsLeft.getWidget(0); 
		}
		break;
		case 3: if (additionalQuestionsRight.getWidgetCount()>2) {
			return (AdditionalQuestion) additionalQuestionsRight.getWidget(2); 
		}
		break;
		case 4: if (additionalQuestionsLeft.getWidgetCount()>1) {
			return (AdditionalQuestion) additionalQuestionsLeft.getWidget(1); 
		}
		break;
		case 5: if (additionalQuestionsRight.getWidgetCount()>3) {
			return (AdditionalQuestion) additionalQuestionsRight.getWidget(3); 
		}
		break;
		case 6: if (additionalQuestionsLeft.getWidgetCount()>2) {
			return (AdditionalQuestion) additionalQuestionsLeft.getWidget(2); 
		}
		break;
		case 7: if (additionalQuestionsRight.getWidgetCount()>4) {
			return (AdditionalQuestion) additionalQuestionsRight.getWidget(4); 
		}
		break;
		case 8: if (additionalQuestionsLeft.getWidgetCount()>3) {
			return (AdditionalQuestion) additionalQuestionsLeft.getWidget(3); 
		}
		break;
		case 9: if (additionalQuestionsRight.getWidgetCount()>5) {
			return (AdditionalQuestion) additionalQuestionsRight.getWidget(5); 
		}
		break;
		case 10: if (additionalQuestionsLeft.getWidgetCount()>4) {
			return (AdditionalQuestion) additionalQuestionsLeft.getWidget(4); 
		}
		break;
		case 11: if (additionalQuestionsRight.getWidgetCount()>6) {
			return (AdditionalQuestion) additionalQuestionsRight.getWidget(6); 
		}
		break;
		}
		return null;
	}
	
	private static void addAdditionalQuestion(int index, AdditionalQuestion question) {
		switch(index) {
		case 0: additionalQuestionsRight.insert(question, 0); break;
		case 1: additionalQuestionsRight.insert(question, 1); break;
		case 2: additionalQuestionsLeft.insert(question, 0); break;
		case 3: additionalQuestionsRight.insert(question, 2); break;
		case 4: additionalQuestionsLeft.insert(question, 1); break;
		case 5: additionalQuestionsRight.insert(question, 3); break;
		case 6: additionalQuestionsLeft.insert(question, 2); break;
		case 7: additionalQuestionsRight.insert(question, 4); break;
		case 8: additionalQuestionsLeft.insert(question, 3); break;
		case 9: additionalQuestionsRight.insert(question, 5); break;
		case 10: additionalQuestionsLeft.insert(question, 4); break;
		case 11: additionalQuestionsRight.insert(question, 6); break;
		}
	}
	
	private static void loadCallInformation() {
		int offset = 0;
		callInformation.callId = callId;
		if (callInformation.hasDepartments) offset = 1;
		try {
			for(int i=0;i<callInformation.questions.size();i++) {
				callInformation.questions.get(i).setValue((getQuestion(i+offset)).getValue());
			}
		} catch(Exception e) { info("Unable to get values"); }
		if (callInformation.hasDepartments) {
			callInformation.departmentCode = departmentQuestion.getValue();
		}
		if (languageValid()) {
			callInformation.language = languageCombo.getValue();
			callInformation.interpreterGender = interpreterGender.getValue(interpreterGender.getSelectedIndex());
			callInformation.interpreterVideo = interpreterVideo.getValue();
		}
	}
	
	static private int priorClickedRow = 0;
	
	private void setUpInterpretersPanel() {
		HorizontalPanel group = new HorizontalPanel();
		group.setStyleName("group");

		availableGrid.getElement().setId("int-av");
		availableGrid.setStyleName("int-table");
		availableGrid.addStyleName("fl");
		availableGrid.setText(0, 0, " Interpreters ");
		availableGrid.getRowFormatter().setStyleName(0, "int-table-title");
		availableGrid.getRowFormatter().addStyleName(0, "blue");
		availableGrid.getFlexCellFormatter().setColSpan(0, 0, 2);
		availableGrid.setText(1, 0, " Name ");
		availableGrid.setText(1, 1, " Phone ");
		availableGrid.getRowFormatter().setStyleName(1, "int-table-top");
		availableGrid.getCellFormatter().setStyleName(1, 0, "tbname");
		availableGrid.getCellFormatter().setStyleName(1, 1, "tbphone");
		availableGrid.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				synchronized(this) {
					if (connectedContainer.getWidgetIndex(interpreter)>-1 || dialingContainer.getWidgetIndex(interpreter)>-1) {
						info("Hang up the current interpreter before dialing another.");
						return;
					}
					int row = availableGrid.getCellForEvent(event).getRowIndex();
					if (row < 2) return;
					String id = availableInterpreters.get(row-2).getId();
					if (priorClickedRow != 0) {
						availableGrid.getRowFormatter().removeStyleName(priorClickedRow, "blue");
					}
					priorClickedRow = row;
					availableGrid.getRowFormatter().addStyleName(row, "blue");
					dialingContainer.clear();
					dialingContainer.add(connecting);
					dialingContainer.add(interpreter);
					callInformation.language = languageCombo.getValue();
					interpreter.setHangupVisibility(false);
					interpreter.setMuteVisibility(false);
					manual = true;
					checkCompleteCallStatus();
					agentService.callInterpreter(sessionId, callId, callInformation.language, id, new AsyncCallback<Void>() {
						@Override
						public void onFailure(Throwable caught) {
							dialingContainer.clear();
							error(caught);
						}
						@Override
						public void onSuccess(Void result) {
							interpreter.setHangupVisibility(true);
						}
					});
				}
			}
		});

		historyGrid.getElement().setId("int-dh");
		historyGrid.setStyleName("int-table");
		historyGrid.setText(0, 0, " Interpreter Dial History ");
		historyGrid.getRowFormatter().setStyleName(0, "int-table-title");
		historyGrid.getRowFormatter().addStyleName(0, "blue");
		historyGrid.getFlexCellFormatter().setColSpan(0, 0, 2);
		historyGrid.setText(1, 0, " Name ");
		historyGrid.setText(1, 1, " Phone ");
		historyGrid.getRowFormatter().setStyleName(1, "int-table-top");
		historyGrid.getCellFormatter().setStyleName(1, 0, "tbname");
		historyGrid.getCellFormatter().setStyleName(1, 1, "tbphone");
		ScrollPanel availableScrollPanel = new ScrollPanel();
		availableScrollPanel.setHeight("100px");
		ScrollPanel historyScrollPanel = new ScrollPanel();
		historyScrollPanel.setHeight("100px");
		availableScrollPanel.add(availableGrid);
		availableScrollPanel.setStyleName("interpreterScrollPanel");
		historyScrollPanel.add(historyGrid);
		historyScrollPanel.setStyleName("interpreterScrollPanel");
		historyScrollPanel.addStyleName("fr");
		group.add(availableScrollPanel);
		group.add(historyScrollPanel);
		interpretersPanel.add(group);
		
		HorizontalPanel searchDiv = new HorizontalPanel();
		Label searchInterpreterLabel = new Label("Search Interpreters:");
		searchInterpreterLabel.setStyleName("searchInterpreterLabel");
		searchDiv.setStyleName("searchInterpreterHoizontalTable");
		searchInterpreter.setStyleName("searchInterpreterTextBox");
		searchInterpreter.getElement().setAttribute("placeholder", "Name or Interpreter Code");
		searchInterpreter.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				searchInterpreterUpdate();
			}
		});
		clearSearchInterprter.setHTML("Clear Search");
		clearSearchInterprter.setStyleName("searchInterpreterClearButton");
		clearSearchInterprter.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				searchInterpreterClear();
			}
		});
		searchDiv.add(searchInterpreterLabel);
		searchDiv.add(searchInterpreter);
		searchDiv.setCellWidth(searchInterpreter, "100%");
		searchDiv.add(clearSearchInterprter);
		interpretersPanel.add(searchDiv);
	}

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
		blink.setUrl("telelanguage/images/online.png");
	}

	public static void disconnected() {
		blink.setUrl("telelanguage/images/exclamation.png");
	}
	
	private static DateTimeFormat dateFormatter = DateTimeFormat.getFormat(" MMMM dd, yyyy ");
	private static DateTimeFormat timeFormatter = DateTimeFormat.getFormat(" h:mm zzzz ");
	
	public static void updateStatus(String status) {
		if (status != null) {
			int firstComma = status.indexOf(",");
			int lastComma = status.lastIndexOf(",");
			if (firstComma > -1 && lastComma > -1) {
				statusBar.setHTML(0, 0, status.substring(0, firstComma));
				statusBar.setHTML(0, 1, status.substring(firstComma+1, lastComma));
				statusBar.setHTML(0, 2, status.substring(lastComma+1));
			}
		}
		Date now = new Date();
		statusBar.setHTML(0, 3, dateFormatter.format(now));
		statusBar.setHTML(0, 4, timeFormatter.format(now));
	}

	public static void processMessages(List<String> messages) {
		for (Object message: messages) {
			String msg = (String) message;
			if (msg.indexOf(":")==-1) {
				GWT.log("onMessage (NOT IMPLEMENTED): "+msg);
				return;
			}
			String type = msg.substring(0, msg.indexOf(":"));
			String data = msg.substring(msg.indexOf(":")+1);
			GWT.log("onMessage: "+type+" data: "+data);
			switch(AgentMessages.valueOf(type)) {
			case SaveCallInformation:
				loadCallInformation();
				Tlvx.agentService.saveCallInformation(sessionId, callInformation, new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						error(caught);
					}
					@Override
					public void onSuccess(Void result) {
					}
				});
				break;
			case TlAgentStatusResponse: updateStatus(data); break;
			case TlCallsessionAgentSet: 
				break;
			case TlCallInit: 
				agent.setTopLabel("Call ID: "+getDataValue(data, "callid"));
				customer.setBottomLabel(getDataValue(data, "ani"));
				customer.setMuteVisibility(false);
				customer.setHangupVisibility(false);
				priorityCall = Boolean.parseBoolean(getDataValue(data, "priority"));
				videoCall = Boolean.parseBoolean(getDataValue(data, "video"));
				customer.setIncomingCall(true, priorityCall);
				customer.setVideoCall(videoCall);
				interpretersPanel.setVisible(false);
				callId = getDataValue(data, "callid");
				connectedContainer.setStyleName("outcall");
				completeCallButton.setEnabled(false);
				completeCallButton.removeStyleName("blue");
				connectButton.setEnabled(false);
				connectButton.removeStyleName("red");
				manualDialButton.setEnabled(false);
				manualDialButton.removeStyleName("blue");
				interpreterGender.setSelectedIndex(0);
				interpreterVideo.setVisible(videoCall);
				interpreterVideo.setValue(videoCall);
				callActive = true;
				accessCodeValidated = false;
				manual = false;
				accessCode.setValue("");
				String lastReason = getDataValue(data, "lastReason");
				if (lastReason != null && lastReason.length()>0) lastReasonLabel.setText(lastReason);
				if (priorityCall) {
					priorityCallSound.play();
				}
				break;
			case TlCallStart:
				String callId = getDataValue(data, "callid");
				agent.setTopLabel("Call ID: "+callId);
				customer.setMuteVisibility(true);
				customer.setHangupVisibility(true);
				customer.setIncomingCall(false, false);
				if (priorityCall) {
					connectedContainer.setStyleName("inprioritycall");
				} else {
					connectedContainer.setStyleName("incall");
				}
				getCallInformation(callId);
				showActionPanel();
				dialThirdPartyButton.setEnabled(true);
				dialThirdPartyButton.addStyleName("red");
				callActive = true;
				searchInterpreterClear();
				break;
			case TlCallStop: 
				agent.setTopLabel(name);
				customer.setTopLabel("");
				connectedContainer.setStyleName("outcall");
				dialingContainer.clear();
				thirdParties.clear();
				callId = "";
				//actionPanel.setHeadingText("Company");
				customerNameLabel.setText("");
				lastReasonLabel.setText("");
				languageCombo.setValue("");
				removeActionPanel();
				dialThirdPartyButton.setEnabled(false);
				dialThirdPartyButton.removeStyleName("red");
				additionalQuestionsLeft.clear();
				additionalQuestionsRight.clear();
				callActive = false;
				if (takeCallsButton.getHTML().equalsIgnoreCase("<span>Accept<br>Calls</span>")) agent.setVisible(false);;
				break;
			case TlCallInterpreterHistory:
				updateInterpreterLists();
				break;
			case TlCustomerStatusChange:
				//dialingContainer.clear();
				if (getDataValue(data, "ani").length()>0) customer.setBottomLabel(getDataValue(data, "ani"));
				customer.setMuted(Boolean.parseBoolean(getDataValue(data, "muted")));
				if ("connected".equalsIgnoreCase(getDataValue(data, "status")) && callActive) {
					if (connectedContainer.getWidgetIndex(customer) == -1) connectedContainer.add(customer);
				} else connectedContainer.remove(customer);
				break;
			case TlInterpreterStatusChange:
				dialingContainer.clear();
				interpreter.setMuted(Boolean.parseBoolean(getDataValue(data, "muted")));
				if ("unavailable".equalsIgnoreCase(getDataValue(data, "status"))) {
					info("No interpreters available/logged in for "+callInformation.language+" "+callInformation.interpreterGender);
				} else if ("connected".equalsIgnoreCase(getDataValue(data, "status")) && callActive) {
					interpreter.setMuteVisibility(true);
					interpreter.setHangupVisibility(true);
					if (connectedContainer.getWidgetIndex(interpreter) == -1) connectedContainer.add(interpreter);
				} else {
					connectedContainer.remove(interpreter);
				}
				if (!getDataValue(data, "oncall").equals("false")) {
					updateInterpreterLists();
				}
				checkCompleteCallStatus();
				interpreter.setTopLabel(getDataValue(data, "name"));
				break;
			case TlThirdpartyStatusChange:
				String connectionId = getDataValue(data, "id");
				String name2 = getDataValue(data, "name");
				Caller thirdParty = thirdParties.get(connectionId);
				String status = getDataValue(data, "status");
				if (thirdParty == null && ("dialing".equals(status)) || "connected".equals(status)) {
					for(int i=0; i<dialingContainer.getWidgetCount(); i++) {
						if (dialingContainer.getWidget(i) instanceof Caller) {
							Caller caller = (Caller) dialingContainer.getWidget(i);
							if ("dialing".equals(caller.connectionId)) {
								caller.removeFromParent();
								thirdParty = new Caller(TYPE.ThridParty, connectionId);
								thirdParty.setBottomLabel(name2);
								thirdParties.put(connectionId, thirdParty);
								dialingContainer.add(thirdParty);
							}
						}
					}
				}
				if (thirdParty == null && "connected".equals(status)) {
					dialingContainer.clear();
					thirdParty = new Caller(TYPE.ThridParty, connectionId);
					thirdParties.put(connectionId, thirdParty);
				}
				if (thirdParty != null) {
					thirdParty.setMuted(Boolean.parseBoolean(getDataValue(data, "muted")));
					if("dialing".equalsIgnoreCase(getDataValue(data, "status"))) {
						thirdParty.setHangupVisibility(true);
						thirdParty.setMuteVisibility(false);
					} else if("connected".equalsIgnoreCase(getDataValue(data, "status")) && callActive) {
						dialingContainer.clear();
						thirdParty.setMuteVisibility(true);
						if (connectedContainer.getWidgetIndex(thirdParty) == -1) connectedContainer.add(thirdParty);
					} else {
						dialingContainer.clear();
						connectedContainer.remove(thirdParty);
						thirdParties.remove(connectionId);
					}
					String name = getDataValue(data, "name");
					if (name != null && name.length()>2) thirdParty.setBottomLabel(name);
				}
				break;
			case TlCallIncomingNoagents:
				if (noAgentsSound != null) noAgentsSound.play();
				break;
			case TlCallPriorityIncoming:
				if (priorityCallSound != null) priorityCallSound.play();
				break;
			case TlAgentStatus:
				name = getDataValue(data, "name");
				agent.setTopLabel(name);
				agent.setBottomLabel(getDataValue(data, "number"));
				agent.setVisible(callActive || "online".equalsIgnoreCase(getDataValue(data, "status")));
				agentContainer.setVisible(callActive || "online".equalsIgnoreCase(getDataValue(data, "status")));
				break;
			case Logout:
				logout(data);
				break;
			case ErrorMessage:
				info(data);
				break;
			case ping:
				break;
			default: System.out.println("NOT IMPLEMENTED IN CLIENT");
			}
		}
	}
	
	static void error(Throwable caught) {
		info(caught.getMessage());
		if (caught instanceof TLVXError) {
			TLVXError error = (TLVXError) caught;
			if (error.forceLogout) {
				messageTimer.cancel();
				blink.setVisible(false);
				clearContent();
				showLoginForm();
				info("We have detected that you were previously logged out out.  Possibly you had 2 windows open and one logged out or the server timed you out.");
			}
		}
	}

	private static void getCallInformation(String callId) {
		//actionPanel.mask();
		agentService.getCallInformationByCallId(sessionId, callId, new AsyncCallback<CallInformation>() {
			@Override
			public void onFailure(Throwable caught) {
				//actionPanel.unmask();
				error(caught);
			}
			@Override
			public void onSuccess(CallInformation result) {
				//actionPanel.unmask();
				callInformation = result;
				showCustomerSpecificQuestions();
				updateInterpreterLists();
				if (callInformation.accessCode != null && callInformation.accessCode.length() > 2) accessCodeValidated = true;
				checkCompleteCallStatus();
			}
		});
	}
	
	protected static void searchInterpreterClear() {
		searchInterpreter.setValue("");
		if (availableInterpreters != null) {
			for (int index = 0; index < availableInterpreters.size(); index++) {
				availableGrid.getRowFormatter().setVisible(index+2, true);
			}
		}
	}

	protected static void searchInterpreterUpdate() {
		String search = searchInterpreter.getValue();
		if (search == null || search.length() == 0) {
			searchInterpreterClear();
		} else 
		for (int index = 0; index < availableInterpreters.size(); index++) {
			InterpreterLine i = availableInterpreters.get(index);
			if (i.getName().toLowerCase().contains(search.toLowerCase()) ||
				i.getCode().contains(search)) {
				availableGrid.getRowFormatter().setVisible(index+2, true);
			} else {
				availableGrid.getRowFormatter().setVisible(index+2, false);
			}
		}
	}

	private static void updateInterpreterLists() {
		if (callInformation == null || callInformation.language == null || callInformation.language.length() == 0) return;
		interpretersPanel.setVisible(true);
		//interpretersPanel.mask();
		agentService.getInterpreters(sessionId, callId, callInformation.language, callInformation.interpreterGender, callInformation.interpreterVideo, new AsyncCallback<List<InterpreterLine>>() {
			@Override
			public void onFailure(Throwable caught) {
				error(caught);
			}
			@Override
			public void onSuccess(List<InterpreterLine> result) {
				availableInterpreters = result;
				while (availableGrid.getRowCount()>2) availableGrid.removeRow(2);
				priorClickedRow = 0;
				for (int index = 0; index < availableInterpreters.size(); index++) {
					InterpreterLine line = result.get(index);
					String pStart = "<p>";
					if (!line.getAvailable()) {
						pStart="<p style=\"color: red\">";
					}
					availableGrid.setHTML(index+2, 0, pStart+line.getName()+"</p>");
					availableGrid.setHTML(index+2, 1, line.getPhoneNumber());
				}
				searchInterpreterUpdate();
			}
		});
		agentService.getInterpreterHistory(sessionId, callId, callInformation.language, new AsyncCallback<List<InterpreterLine>>() {
			@Override
			public void onFailure(Throwable caught) {
				error(caught);
			}
			@Override
			public void onSuccess(List<InterpreterLine> result) {
				while (historyGrid.getRowCount()>2) historyGrid.removeRow(2);
				for (InterpreterLine line : result) {
					int row = historyGrid.getRowCount();
					String pStart = "<p>";
					if (!line.getAvailable()) {
						pStart="<p style=\"color: red\">";
					}
					historyGrid.setHTML(row, 0, pStart+line.getName()+"</p>");
					historyGrid.setHTML(row, 1, line.getPhoneNumber());
				}
			}
		});
	}
	
	private static void showCustomerSpecificQuestions() {
		//actionPanel.setHeadingText("Company: "+callInformation.company);
		int index = 0;
		additionalQuestionsLeft.clear();
		additionalQuestionsRight.clear();
		if (callInformation.company != null && callInformation.company.length()>0)
			customerNameLabel.setText("Company Name: "+callInformation.company);
		if (callInformation.lastReason != null && callInformation.lastReason.length()>0)
			lastReasonLabel.setText(callInformation.lastReason);
		else lastReasonLabel.setText("");
		customer.setTopLabel("customer");
		if (callInformation.language != null) 
			languageCombo.setValue(callInformation.language);
		if (callInformation.interpreterGender != null) {
			if (callInformation.interpreterGender.equalsIgnoreCase("F")) interpreterGender.setSelectedIndex(1);
			if (callInformation.interpreterGender.equalsIgnoreCase("M")) interpreterGender.setSelectedIndex(2);
		}
		if (callInformation.interpreterVideo != null) {
			interpreterVideo.setValue(callInformation.interpreterVideo);
		}
		if (callInformation.accessCode != null) accessCode.setValue(callInformation.accessCode);
		if (callInformation.customerId != null && callInformation.customerId.length()>5) {
			if (callInformation.hasDepartments) {
				if (callInformation.departments.size()>0) {
					String departmentLabel = "Department";
					if (callInformation.deptVar != null && callInformation.deptVar.length()>5) departmentLabel=callInformation.deptVar;
					departmentQuestion = new AdditionalQuestion("Department", departmentLabel, callInformation.askDepartmentText, callInformation.departments, callInformation.departmentCode);
					addAdditionalQuestion(index++, departmentQuestion);
				} else { 
					callInformation.hasDepartments = false;
				}
				for (QuestionItem item: callInformation.questions) {
					addAdditionalQuestion(index++, new AdditionalQuestion(item.getLabel(), item.getLabel(), item.getQuestion(), null, item.getValue()));
				}
			} else {
				for (QuestionItem item: callInformation.questions) {
					addAdditionalQuestion(index++, new AdditionalQuestion(item.getLabel(), item.getLabel(), item.getQuestion(), null, item.getValue()));
				}
			}
			if (languageValid()) {
				connectButton.setEnabled(true);
				connectButton.addStyleName("red");
				manualDialButton.setEnabled(true);
				manualDialButton.addStyleName("blue");
			}
		} else {
			connectButton.setEnabled(false);
			connectButton.removeStyleName("red");
			manualDialButton.setEnabled(false);
			manualDialButton.removeStyleName("blue");
		}
	}

	private void stopTakingCalls() {
		agentService.stopTakingCalls(sessionId, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				error(caught);
			}
			@Override
			public void onSuccess(Void x) {
				takeCallsButton.setHTML("<span>Accept<br>Calls</span>");
				if (!callActive) agent.setVisible(false);
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

	public static void checkCompleteCallStatus() {
		boolean completeCallEnabled = true;
		if (!accessCodeValidated) completeCallEnabled = false; 
		if (callInformation != null && callInformation.customerId == null) completeCallEnabled = false; 
		for(int i=0;i<(additionalQuestionsLeft.getWidgetCount()+additionalQuestionsRight.getWidgetCount());i++) {
			if(!getQuestion(i).isValid()) {
				completeCallEnabled = false;
			} 
		}
		if (manual && dialingContainer.getWidgetIndex(interpreter)>-1) {
			completeCallEnabled = false;
			requeueCallButton.setEnabled(false);
			requeueCallButton.removeStyleName("blue");
		} else {
			requeueCallButton.setEnabled(true);
			requeueCallButton.addStyleName("blue");
		}
		if (!languageValid()) completeCallEnabled = false; 
		completeCallButton.setEnabled(completeCallEnabled);
		if (completeCallEnabled) completeCallButton.addStyleName("blue");
		else completeCallButton.removeStyleName("blue");
	}
}
