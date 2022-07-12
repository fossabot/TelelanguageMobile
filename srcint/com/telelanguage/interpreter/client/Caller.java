package com.telelanguage.interpreter.client;

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
	
	public Caller(final TYPE type) {
		this.type = type;
		muteIcon.setStyleName("muteIcon");
		hangupIcon.setStyleName("hangupIcon");
		this.setWidth("118px");
		this.setHeight("116px");
		this.setStyleName("caller");
		this.add(topLabel, 0, 90);
		this.add(videoIcon, 10, 50);
		videoIcon.setVisible(false);
		topLabel.setWidth("120px");
		topLabel.setStyleName("callerTopLabel");
		this.add(bottomLabel, 0, 100);
		bottomLabel.setWidth("120px");
		bottomLabel.setStyleName("callerBottomLabel");
		this.add(hangupIcon, 82, 18);
		hangupIcon.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				switch(type) {
					case Client:
						Tlvx_interpreter.sipHangup();
					case ThridParty:
						Tlvx_interpreter.interpreterService.hangupThirdParty(Tlvx_interpreter.sessionId, id, new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
							}
							@Override
							public void onSuccess(Void result) {
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
				topLabel.setText("Agent");
				muteIcon.setVisible(false);
				hangupIcon.setVisible(false);
				break;
			case Client:
				topLabel.setText("Customer");
				addStyleName("callerCustomer");
				break;
			case Intreperter:
				addStyleName("callerInterpreter");
				muteIcon.setVisible(false);
				hangupIcon.setVisible(false);
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
	
	String id;

	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
}
