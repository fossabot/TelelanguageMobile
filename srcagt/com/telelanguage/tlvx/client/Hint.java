package com.telelanguage.tlvx.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlowPanel;

public class Hint extends FlowPanel {
	FlowPanel text = new FlowPanel();
	
	public Hint() {
		setStyleName("metro hint top");
		text.setStyleName("metro hint-text");
		add(text);
		getElement().getStyle().setBackgroundColor("rgb(255,252,192)");
		getElement().getStyle().setDisplay(Display.NONE);
	}
	
	public void setToolTop(Element element, String question) {
		text.getElement().setInnerText(question);
		getElement().getStyle().setLeft(element.getOffsetLeft(), Unit.PX);
		getElement().getStyle().setTop(element.getOffsetTop(), Unit.PX);
		getElement().getStyle().setDisplay(Display.BLOCK);
		element.getParentElement().appendChild(this.getElement());
	}
}
