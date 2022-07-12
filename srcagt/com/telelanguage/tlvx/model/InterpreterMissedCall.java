package com.telelanguage.tlvx.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="interpreter_missed_calls")
public class InterpreterMissedCall {
	@Id @GeneratedValue(generator="system-uuid")
	@GenericGenerator(name="system-uuid", strategy = "uuid")
	private String id;
	
	@ManyToOne
	@JoinColumn(name = "interpreter_id")
	private Interpreter interpreter;
	
	@Column(name="call_id")
	private Long callId;
	
	@Column(name="missed_date")
	Date missedDate;
	
	@Column
	private String language;
	
	@Column 
	private String reason;
	
	@Column
	private Integer count;
	
	@Column
	private Boolean reject;
	
	@Column(name="call_type")
	private String callType;
	
	@Column
	private Boolean logout;
	
	@Column
	private Boolean manual;
	
	@Column(name="email_sent")
	private Boolean emailSent;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Interpreter getInterpreter() {
		return interpreter;
	}

	public void setInterpreter(Interpreter interpreter) {
		this.interpreter = interpreter;
	}

	public Long getCallId() {
		return callId;
	}

	public void setCallId(Long callId) {
		this.callId = callId;
	}

	public Date getMissedDate() {
		return missedDate;
	}

	public void setMissedDate(Date missedDate) {
		this.missedDate = missedDate;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Boolean getReject() {
		return reject;
	}

	public void setReject(Boolean reject) {
		this.reject = reject;
	}

	public Boolean getLogout() {
		return logout;
	}

	public void setLogout(Boolean logout) {
		this.logout = logout;
	}

	public Boolean getManual() {
		return manual;
	}

	public void setManual(Boolean manual) {
		this.manual = manual;
	}

	public String getCallType() {
		return callType;
	}

	public void setCallType(String callType) {
		this.callType = callType;
	}

	public Boolean getEmailSent() {
		return emailSent;
	}

	public void setEmailSent(Boolean emailSent) {
		this.emailSent = emailSent;
	}
}
