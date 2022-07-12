package com.telelanguage.video.client;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class UserInfo implements IsSerializable {
	public String email;
	public String accessCode;
	public String deptCode; // if populated pre-populate
	public String deptLabel;
	public String deptQuestion;
	public List<String> questionId;
	public List<String> questionLabel;
	public List<String> questionPlaceholder;
	public String marketingInfo;
	public String forgotPasswordLink;
	public String alreadyCustomerLink;
	public String openAnAccountLink;
	public String preScheduleInterpreterLink;
}
