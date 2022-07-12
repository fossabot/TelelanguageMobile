package com.telelanguage.tlvx.model;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CallInformation implements IsSerializable{
	public CallInformation() { }
	public String callId;
	public String customerId;
	public String company;
	public String language;
	public boolean hasDepartments = false;
	public String askDepartmentText;
	public String departmentCode;
	public List<QuestionItem> questions;
	public List<Department> departments;
	public String accessCode;
	public String lastReason;
	public String interpreterGender;
	public Boolean interpreterVideo;
	public String deptVar;
	@Override
	public String toString() {
		String q = "";
		if (questions!=null) for (QuestionItem question:questions) q+=" "+question.toString();
		String s = "CallInfo: "+callId+" cu:"+customerId+" a:"+accessCode+" co:"+company+" l:"+language+" a:"+hasDepartments+" dC:"+departmentCode+" q:"+q+" v:"+interpreterVideo+" d:"+deptVar;
		return s;
	}
}
