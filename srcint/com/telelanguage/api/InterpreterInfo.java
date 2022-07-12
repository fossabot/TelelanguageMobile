package com.telelanguage.api;

public class InterpreterInfo
{
    public String interpreterId;
    public String accessCode;
    public String firstName;
    public String lastName;
    public String areaCode;
    public String phoneNumber;
    public String email;
    public boolean activeSession;
    public boolean webPhone;
    public Long callId;
    public String onWebPage;
    public boolean resetNumMissedCalls = false;
	public String webPhoneSipAddress;
	public boolean videoEnabled;
	public boolean logon = false;
	public boolean allowVideo;
	public boolean videoOnly;
	public boolean forceVideoOnly;
	public String rejectReason;
	public String allowedIPAddresses;
	public String thirdPartyId;
    
    @Override
    public String toString() {
    	return ""+interpreterId+" "+accessCode+" "+firstName+" "+lastName+" "+areaCode+phoneNumber+" "+email+
    			" "+activeSession+" "+webPhone+" "+callId+" "+onWebPage+" "+resetNumMissedCalls+" "+videoEnabled+" "+videoOnly+" "+forceVideoOnly+" l:"+logon+" rr:"+rejectReason+" allowedIps: "+allowedIPAddresses;
    }
}
