package com.telelanguage.tlvx.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="subscribed_clients_did")
public class SubscribedClientsDID
{
    private String subscriberId;
    private boolean IVR_Enabled;
    private String DID;
    private boolean dontRecordCall;
    private String Subscription_Code;
    private String greetingWave;
    private String greetingSentence;
    @Id
    @Column(name="ID")
    public String getSubscriberId()
    {
        return this.subscriberId;
    }
    public void setSubscriberId(String subscriberId){
    	this.subscriberId = subscriberId;
    }
    @Column(name="IVR_Enabled")
    public boolean isIVR_Enabled()
    {
        return IVR_Enabled;
    }
    public void setIVR_Enabled(boolean enabled)
    {
        IVR_Enabled = enabled;
    }
    @Column(name="DID")
    public String getDID()
    {
        return DID;
    }
    public void setDID(String did)
    {
        DID = did;
    }
    @Column(name="Dont_Record_Call")
	public boolean isDontRecordCall() {
		return dontRecordCall;
	}
	public void setDontRecordCall(boolean dontRecordCall) {
		this.dontRecordCall = dontRecordCall;
	}
    @Column(name="Subscription_Code")
    public String getSubscription_Code()
    {
        return Subscription_Code;
    }
    public void setSubscription_Code(String subscription_Code)
    {
        Subscription_Code = subscription_Code;
    }
    @Column(name="greetingWave")
    public String getGreetingWave()
    {
        return greetingWave;
    }
    public void setGreetingWave(String greetingWave)
    {
        this.greetingWave = greetingWave;
    }
    @Column(name="greetingSentence")
    public String getGreetingSentence()
    {
        return greetingSentence;
    }
    public void setGreetingSentence(String greetingSentence)
    {
        this.greetingSentence = greetingSentence;
    }
}
