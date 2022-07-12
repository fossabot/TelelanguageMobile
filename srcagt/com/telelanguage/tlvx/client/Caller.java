package com.telelanguage.tlvx.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class Caller extends AbsolutePanel {
	static public enum TYPE { Agent, Client, Intreperter, ThridParty };
	
	private Label topLabel = new Label();
	private Label bottomLabel = new Label();
	//private Label onHoldLabel = new Label("On-Hold");
	private Image muteIcon = new Image("images/btn_hold_on.png");
	private Image hangupIcon = new Image("images/btn_del.gif");
	private Image videoIcon = new Image("images/video-24.png");
	boolean onHold = false;
	TYPE type;
	String connectionId;
	
	public Caller(final TYPE type, final String connectionId) {
		this.type = type;
		this.connectionId = connectionId;
		muteIcon.setStyleName("muteIcon");
		hangupIcon.setStyleName("hangupIcon");
		this.setWidth("118px");
		this.setHeight("116px");
		this.setStyleName("caller");
		this.add(topLabel, 0, 90);
		topLabel.setWidth("120px");
		topLabel.setStyleName("callerTopLabel");
		this.add(bottomLabel, 0, 100);
		bottomLabel.setWidth("120px");
		bottomLabel.setStyleName("callerBottomLabel");
		//this.add(onHoldLabel, 0, 85);
		//onHoldLabel.setWidth("80px");
		//onHoldLabel.setStyleName("callerOnHoldLabel");
		//onHoldLabel.setVisible(false);
		this.add(videoIcon, 10, 50);
		videoIcon.setVisible(false);
		this.add(muteIcon, 82, 43);
		muteIcon.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				muteIcon.setVisible(false);
				if(event.getSource() == muteIcon) {
					if (!onHold) {
						switch(type) {
						case Client: Tlvx.agentService.callerOnHold(Tlvx.sessionId, Tlvx.callId, false, new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
								Tlvx.error(caught);
								muteIcon.setVisible(true);
							}
							@Override
							public void onSuccess(Void v) {
								Tlvx.info("Caller on hold.");
								muteIcon.setVisible(true);
							}
						}); break;
						case Intreperter: Tlvx.agentService.interpreterOnHold(Tlvx.sessionId, Tlvx.callId, new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
								Tlvx.error(caught);
								muteIcon.setVisible(true);
							}
							@Override
							public void onSuccess(Void v) {
								Tlvx.info("Interpreter on hold.");
								muteIcon.setVisible(true);
							}
						}); break;
						case ThridParty: Tlvx.agentService.thirdPartyOnHold(Tlvx.sessionId, Tlvx.callId, connectionId, new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
								Tlvx.error(caught);
								muteIcon.setVisible(true);
							}
							@Override
							public void onSuccess(Void v) {
								Tlvx.info("Third Party on hold.");
								muteIcon.setVisible(true);
							}
						}); break;
						}
					} else {
						switch(type) {
						case Client: Tlvx.agentService.callerOffHold(Tlvx.sessionId, Tlvx.callId, new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
								Tlvx.error(caught);
								muteIcon.setVisible(true);
							}
							@Override
							public void onSuccess(Void v) {
								Tlvx.info("Caller off hold.");
								muteIcon.setVisible(true);
							}
						}); break;
						case Intreperter: Tlvx.agentService.interpreterOffHold(Tlvx.sessionId, Tlvx.callId, new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
								Tlvx.error(caught);
								muteIcon.setVisible(true);
							}
							@Override
							public void onSuccess(Void v) {
								Tlvx.info("Interpreter off hold.");
								muteIcon.setVisible(true);
							}
						}); break;
						case ThridParty: Tlvx.agentService.thirdPartyOffHold(Tlvx.sessionId, Tlvx.callId, connectionId, new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
								Tlvx.error(caught);
								muteIcon.setVisible(true);
							}
							@Override
							public void onSuccess(Void v) {
								Tlvx.info("Third party off hold.");
								muteIcon.setVisible(true);
							}
						}); break;
						}						
					}
				}
			}
		});
		this.add(hangupIcon, 82, 15);
		hangupIcon.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				switch(type) {
					case Client:
						Tlvx.completeCall(true);
					break;
					case Intreperter:
						Tlvx.agentService.hangupInterpreter(Tlvx.sessionId, Tlvx.callId, new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
								Tlvx.error(caught);
							}
							@Override
							public void onSuccess(Void v) {
								Tlvx.info("Interpreter hung up.");
							}
						});
					break;
					case ThridParty:
						Tlvx.agentService.hangupThirdParty(Tlvx.sessionId, Tlvx.callId, connectionId, new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
								Tlvx.error(caught);
							}
							@Override
							public void onSuccess(Void v) {
								Tlvx.info("Third Party hung up.");
							}
						});
					break;
					default: break;
				}
			}
		});
		switch (type) {
			case Agent:
				addStyleName("callerAgent");
				muteIcon.setVisible(false);
				hangupIcon.setVisible(false);
				break;
			case Client:
				topLabel.setText("Customer");
				addStyleName("callerCustomer");
				break;
			case Intreperter:
				addStyleName("callerInterpreter");
				break;
			case ThridParty:
				addStyleName("callerThirdParty");
				break;
		}
	}

	public void setTopLabel(String dataValue) {
		topLabel.setText(dataValue);
	}

	public void setBottomLabel(String dataValue) {
		bottomLabel.setText(dataValue);
	}

	public void setMuted(boolean muted) {
		String mutedStyle = "callerCustomerOnHold";
		switch (type) {
		case Agent:
			break;
		case Client:
			break;
		case Intreperter:
			mutedStyle = "callerInterpreterOnHold";
			break;
		case ThridParty:
			mutedStyle = "callerThirdPartyOnHold";
			break;
		}
		if (muted) {
			addStyleName(mutedStyle);
			muteIcon.setUrl("images/btn_hold_off.png");
			onHold = true;
		} else {
			removeStyleName(mutedStyle);
			muteIcon.setUrl("images/btn_hold_on.png");
			onHold = false;
		}
	}

	public void setMuteVisibility(boolean b) {
		muteIcon.setVisible(b);
	}

	public void setHangupVisibility(boolean b) {
		hangupIcon.setVisible(b);
	}
	
	public void setIncomingCall(boolean b, boolean priority) {
		if (b) {
			if (priority) {
				addStyleName("callerPriorityCustomerIncoming");
			} else {
				addStyleName("callerCustomerIncoming");
			}
		}
		else {
			removeStyleName("callerCustomerIncoming");
			removeStyleName("callerPriorityCustomerIncoming");
		}
	}

	public void setVideoCall(boolean videoCall) {
		if (videoCall) {
			videoIcon.setVisible(true);
		} else {
			videoIcon.setVisible(false);
		}
	}
}
