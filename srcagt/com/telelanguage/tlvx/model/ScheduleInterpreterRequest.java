package com.telelanguage.tlvx.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="schedule_interpreter_request")
public class ScheduleInterpreterRequest {
	@Id
	@GeneratedValue(generator="system-uuid")
	@GenericGenerator(name="system-uuid", strategy = "uuid")
	private String id;
	
	@Column(name="access_code")
	private String accessCode;
	
	@Column(name="vri_login_email")
	private String vriLoginEmail;
	
	@Column
	private String email;
	
	@Column
	private String name;
	
	@Column
	private String phone;
	
	@Column
	private String org;
	
	@Column
	private String language;
	
	@Column(name="video_or_phone")
	private String videoOrPhone;
	
	@Column(name="schedule_time")
	private String scheduleTime;
	
	@Column(name="created_at")
	private Date createdAt;
	
	@Column(name="updated_at")
	private Date updatedAt;
	
	@Column(name="timezone_offset_minutes")
	private Integer timezoneOffsetMinutes;
	
	@Column(name="email_sent")
	private Boolean emailSent;
	
	@Column(name="emailed_on")
	private Date emailedOn;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAccessCode() {
		return accessCode;
	}
	public void setAccessCode(String accessCode) {
		this.accessCode = accessCode;
	}
	public String getVriLoginEmail() {
		return vriLoginEmail;
	}
	public void setVriLoginEmail(String vriLoginEmail) {
		this.vriLoginEmail = vriLoginEmail;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getOrg() {
		return org;
	}
	public void setOrg(String org) {
		this.org = org;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getVideoOrPhone() {
		return videoOrPhone;
	}
	public void setVideoOrPhone(String videoOrPhone) {
		this.videoOrPhone = videoOrPhone;
	}
	public String getScheduleTime() {
		return scheduleTime;
	}
	public void setScheduleTime(String scheduleTime) {
		this.scheduleTime = scheduleTime;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	public Date getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}
	public Integer getTimezoneOffsetMinutes() {
		return timezoneOffsetMinutes;
	}
	public void setTimezoneOffsetMinutes(Integer timezoneOffsetMinutes) {
		this.timezoneOffsetMinutes = timezoneOffsetMinutes;
	}
	public Boolean getEmailSent() {
		return emailSent;
	}
	public void setEmailSent(Boolean emailSent) {
		this.emailSent = emailSent;
	}
	public Date getEmailedOn() {
		return emailedOn;
	}
	public void setEmailedOn(Date emailedOn) {
		this.emailedOn = emailedOn;
	}
}
