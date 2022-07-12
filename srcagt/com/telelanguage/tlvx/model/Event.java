package com.telelanguage.tlvx.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "events")
public class Event {
	
	public final static int LOGOFF = 91;

	private Long event_id;
	private int code;
	private Date created_at;
	private Date updated_at;
	private String payload;
	private String payload2;
	private long agent_id;
	private long call_id;

	@Id
	@GeneratedValue
	@Column(name = "event_id")
	public Long getId() {
		return event_id;
	}
	
	public void setId(Long event_id) {
		this.event_id = event_id;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	public Date getCreated_at() {
		return created_at;
	}

	public void setCreated_at(Date created_at) {
		this.created_at = created_at;
	}

	public Date getUpdated_at() {
		return updated_at;
	}

	public void setUpdated_at(Date updated_at) {
		this.updated_at = updated_at;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getPayload2() {
		return payload2;
	}

	public void setPayload2(String payload2) {
		this.payload2 = payload2;
	}

	public long getAgent_id() {
		return agent_id;
	}

	public void setAgent_id(long agent_id) {
		this.agent_id = agent_id;
	}

	public long getCall_id() {
		return call_id;
	}

	public void setCall_id(long call_id) {
		this.call_id = call_id;
	}
}
