package com.telelanguage.tlvx.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="thirdparty_activities")
public class ThirdPartyActivity {
	@Id
	@GeneratedValue
	private Long id;
	
	@Column(name="call_id")
	private Long callId;
	
	@Column(name="thirdparty_ani")
	private String thirdpartyAni;
	
	@Column(name="start_time")
	private Date startTime;
	
	@Column(name="end_time")
	private Date endTime;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getCallId() {
		return callId;
	}
	public void setCallId(Long callId) {
		this.callId = callId;
	}
	public String getThirdpartyAni() {
		return thirdpartyAni;
	}
	public void setThirdpartyAni(String thirdpartyAni) {
		this.thirdpartyAni = thirdpartyAni;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
}
