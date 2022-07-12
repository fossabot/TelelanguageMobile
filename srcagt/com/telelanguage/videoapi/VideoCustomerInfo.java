package com.telelanguage.videoapi;

import java.util.List;

public class VideoCustomerInfo
{
    public String sessionId;
	public String webPhoneSipAddress;
	public Integer maxKbitSec;
	public Long callId;
	public String videoServer;
	public String janusServer;
	public String roomNumber;
	public String accessCode;
	public String language;
	public List<String> questionInputs;
	public String departmentCode;
	public String interpreterGender;
	public Boolean requireVideo;
	public String email;
    
    @Override
    public String toString() {
    	return ""+sessionId+" "+webPhoneSipAddress+" "+maxKbitSec+" callid: "+callId+" videoServer: "+videoServer+" janusServer: "+janusServer+" roomNumber: "+roomNumber+" accessCode: "+accessCode+" language "+language+" questions "+questionInputs+" requireVideo "+requireVideo+" email "+email;
    }
}
