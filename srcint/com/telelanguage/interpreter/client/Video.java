package com.telelanguage.interpreter.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.VideoElement;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;

public class Video extends FlowPanel {
	private VideoElement video = Document.get().createVideoElement();
	private SpanElement label = Document.get().createSpanElement();
	private Image pausePlayButton = new Image("images/pause.png");
	private ClickHandler pauseClickHandler = null;
	private ClickHandler playClickHandler = null;
	public Video(String id) {
		getElement().getStyle().setHeight(300, Unit.PX);
		getElement().getStyle().setPosition(Position.RELATIVE);
		getElement().setId("divvideo"+id);
		video.setId("video"+id);
		video.getStyle().setPosition(Position.ABSOLUTE);
		video.setAutoplay(true);
		video.setHeight(300);
		video.setMuted(true);
		video.getStyle().setPaddingRight(10, Unit.PX);
		//<span class="label label-info" id="curbitrate1" style="position: absolute; bottom: 0px; right: 0px; margin: 15px; display: inline;">117 kbits/sec</span>
		label.getStyle().setPosition(Position.ABSOLUTE);
		label.getStyle().setTop(0, Unit.PX);
		label.getStyle().setMargin(5, Unit.PX);
		label.getStyle().setRight(0, Unit.PX);
		label.getStyle().setBackgroundColor("#393e61");
		label.getStyle().setColor("#ffffff");
		label.setInnerText(id);
		pausePlayButton.setStyleName("");
		pausePlayButton.getElement().getStyle().setDisplay(Display.NONE);
		pausePlayButton.getElement().getStyle().setPosition(Position.ABSOLUTE);
		pausePlayButton.getElement().getStyle().setBottom(5, Unit.PX);
		pausePlayButton.getElement().getStyle().setLeft(5, Unit.PX);
		pausePlayButton.getElement().getStyle().setCursor(Cursor.POINTER);
		getElement().appendChild(video);
		getElement().appendChild(label);
		getElement().appendChild(pausePlayButton.getElement());
		Event.sinkEvents(pausePlayButton.getElement(), Event.ONCLICK);
	    Event.setEventListener(pausePlayButton.getElement(), new EventListener() {
	        @Override
	        public void onBrowserEvent(Event event) {
				if (pausePlayButton.getUrl().contains("pause")) {
					pausePlayButton.setUrl("images/play.png");
					if (pauseClickHandler != null) {
						pauseClickHandler.onClick(null);
					}
				} else {
					pausePlayButton.setUrl("images/pause.png");
					if (playClickHandler != null) {
						playClickHandler.onClick(null);
					}
				}
	        }
	    });
	}
	
	public void addPauseClickHandler(ClickHandler pauseClickHandler) {
		pausePlayButton.getElement().getStyle().clearDisplay();
		this.pauseClickHandler = pauseClickHandler;
	}
	
	public void addPlayClickHandler(ClickHandler playClickHandler) {
		pausePlayButton.getElement().getStyle().clearDisplay();
		this.playClickHandler = playClickHandler;
	}
	
	public void connecting() {
		video.setPoster("images/connecting.gif");
	}
	
	public void notConnecting() {
		video.setPoster("");
	}
}
