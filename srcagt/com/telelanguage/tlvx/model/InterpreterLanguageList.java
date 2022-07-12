package com.telelanguage.tlvx.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="interpreter_language_list")
public class InterpreterLanguageList {
	String id;
	String interpreterId;
	String languageId;
	Date callTicketDate;
	
	@Id
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	@Column(name="Interpreter_ID")
	public String getInterpreterId() {
		return interpreterId;
	}
	public void setInterpreterId(String interpreterId) {
		this.interpreterId = interpreterId;
	}
	
	@Column(name="Language_ID")
	public String getLanguageId() {
		return languageId;
	}
	public void setLanguageId(String languageId) {
		this.languageId = languageId;
	}
	
	@Column(name="Call_Ticket_DateTime")
	public Date getCallTicketDate() {
		return callTicketDate;
	}
	public void setCallTicketDate(Date callTicketDate) {
		this.callTicketDate = callTicketDate;
	}
}
