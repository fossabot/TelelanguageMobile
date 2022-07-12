package com.telelanguage.tlvx.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public class QuestionItem implements IsSerializable {
	private String label;
	private String question;
	private String optionId;
	private String value;
	
	public QuestionItem() {
		
	}
	
	public QuestionItem(String optionId, String label, String question) {
		this.optionId = optionId;
		this.label = label;
		this.question = question;
	}
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public String getOptionId() {
		return optionId;
	}
	public void setOptionId(String optionId) {
		this.optionId = optionId;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	@Override
	public String toString() {
		return label+":"+value;
	}
}
