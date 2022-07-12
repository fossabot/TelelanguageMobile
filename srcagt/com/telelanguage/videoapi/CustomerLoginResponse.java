package com.telelanguage.videoapi;

import java.util.List;

public class CustomerLoginResponse {
	public String email;
	public String sipUrl;
	public String token;
	public String accessCode;
	public String deptCode; // if populated pre-populate
	public String deptLabel;
	public String deptQuestion;
	public List<String> questionId;
	public List<String> questionLabel;
	public List<String> questionPlaceholder;
	public String marketingText;
	public String forgotPasswordLink;
	public String alreadyCustomerLink;
	public String openAnAccountLink;
	public String preScheduleInterpreterLink;
}
