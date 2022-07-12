package com.telelanguage.videoapi;

import java.util.Date;

public class ScheduleInterpreterRequest {
	
	public String accessCode;
	public String videoEmail;
	public String email;
	public String name;
	public String phone;
	public String org;
	public String languageString;
	public String typeString;
	public String date;
	public Date dateSubmitted;
	public Integer timezone;

	@Override
	public String toString() {
		return "ScheduleInterpreterRequest: "+accessCode+" "+videoEmail+" "+email+" "+
				phone+" "+org+" "+languageString+" "+typeString+" "+date+" "+dateSubmitted+" "+timezone;
	}
}
