package com.telelanguage.tlvx.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Customer
 */
@Entity
@Table(name = "customers")
public class Customer
{
    private String customerId;
    private String code;
    private Boolean sendToAgent;
    private String name;
    private int timeZoneId;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String postalCode;
    private String contactPerson;
    private String contactPhone;
    private String contactPhoneExtension;
    private String attention;
    private String didIvr;
    private String didOperator;
    private boolean didActive;
    private int rateOption;
    private boolean callerIdActive;
    private int rateIncrement;
    private int minMinute;
    private String greetingWave;
    private String subscriptionCode;
    private String ddn;
    private String ddc;
    private String ddnWave;
    private String holdMusicWave;
    private String ow1Id;
    private String ow2Id;
    private String greetingSentence;
    private String qualificationId;
    private int qualificationLevel;
    private boolean callRecording;
    private boolean manualConnect;
    private boolean askCode;
    private boolean listenIn;
    private boolean deleted;
    private boolean mssg2IntrpActive;
    private String mssg2IntrpWave;
    private String mssg2IntrpText;
    private boolean mssg2IntrpReqIDActive;
    private String deptVar;  // text that replaces the placeholder text in the department question
    
    private transient CustomerDNIS customerDNIS;
    
    
    @Transient
    public CustomerDNIS getCustomerDNIS() 
    {
		return customerDNIS;
	}

	public void setCustomerDNIS(CustomerDNIS customerDNIS) 
	{
		this.customerDNIS = customerDNIS;
	}

    @Id
    @Column (name = "ID")
    public String getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(String customerId)
    {
        this.customerId = customerId;
    }

    @Column (name = "Code")
    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }
    
    @Column(name="Send_To_Agent")
	public Boolean getSendToAgent() {
		return sendToAgent;
	}

	public void setSendToAgent(Boolean sendToAgent) {
		this.sendToAgent = sendToAgent;
	}

    @Column (name = "Name")
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Column (name = "Time_Zone_ID")
    public int getTimeZoneId()
    {
        return timeZoneId;
    }

    public void setTimeZoneId(int timeZoneId)
    {
        this.timeZoneId = timeZoneId;
    }

    @Column (name = "Address1")
    public String getAddress1()
    {
        return address1;
    }

    public void setAddress1(String address1)
    {
        this.address1 = address1;
    }
    
    @Column (name = "Address2")
    public String getAddress2()
    {
        return address2;
    }

    public void setAddress2(String address2)
    {
        this.address2 = address2;
    }
    
    @Column (name = "City")
    public String getCity()
    {
        return city;
    }

    public void setCity(String city)
    {
        this.city = city;
    }
    
    @Column (name = "State")
    public String getState()
    {
        return state;
    }

    public void setState(String state)
    {
        this.state = state;
    }

    @Column (name = "Postal_Code")
    public String getPostalCode()
    {
        return postalCode;
    }

    public void setPostalCode(String postalCode)
    {
        this.postalCode = postalCode;
    }

    @Column (name = "Contact_Person")
    public String getContactPerson()
    {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson)
    {
        this.contactPerson = contactPerson;
    }
    
    @Column (name = "Contact_Phone")
    public String getContactPhone()
    {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone)
    {
        this.contactPhone = contactPhone;
    }

    @Column (name = "Contact_Phone_Extension")
    public String getContactPhoneExtension()
    {
        return contactPhoneExtension;
    }

    public void setContactPhoneExtension(String contactPhoneExtension)
    {
        this.contactPhoneExtension = contactPhoneExtension;
    }
    
    @Column (name = "Attention")
    public String getAttention()
    {
        return attention;
    }

    public void setAttention(String attention)
    {
        this.attention = attention;
    }

    @Column (name = "DID_IVR")
    public String getDidIvr()
    {
        return didIvr;
    }

    public void setDidIvr(String didIvr)
    {
        this.didIvr = didIvr;
    }

    @Column (name = "DID_Operator")
    public String getDidOperator()
    {
        return didOperator;
    }

    public void setDidOperator(String didOperator)
    {
        this.didOperator = didOperator;
    }

    @Column (name = "DID_Active")
    public boolean isDidActive()
    {
        return didActive;
    }

    public void setDidActive(boolean didActive)
    {
        this.didActive = didActive;
    }

    @Column (name = "Rate_Option")
    public int getRateOption()
    {
        return rateOption;
    }

    public void setRateOption(int rateOption)
    {
        this.rateOption = rateOption;
    }

    @Column (name = "Caller_ID_Active")
    public boolean isCallerIdActive()
    {
        return callerIdActive;
    }

    public void setCallerIdActive(boolean callerIdActive)
    {
        this.callerIdActive = callerIdActive;
    }

    @Column (name = "Rate_Increment")
    public int getRateIncrement()
    {
        return rateIncrement;
    }

    public void setRateIncrement(int rateIncrement)
    {
        this.rateIncrement = rateIncrement;
    }

    @Column (name = "Min_Minute")
    public int getMinMinute()
    {
        return minMinute;
    }

    public void setMinMinute(int minMinute)
    {
        this.minMinute = minMinute;
    }

    @Column (name = "Greeting_Wave")
    public String getGreetingWave()
    {
        return greetingWave;
    }

    public void setGreetingWave(String greetingWave)
    {
        this.greetingWave = greetingWave;
    }

    @Column (name = "Subscription_Code")
    public String getSubscriptionCode()
    {
        return subscriptionCode;
    }

    public void setSubscriptionCode(String subscriptionCode)
    {
        this.subscriptionCode = subscriptionCode;
    }

    @Column (name = "DDN")
    public String getDdn()
    {
        return ddn;
    }

    public void setDdn(String ddn)
    {
        this.ddn = ddn;
    }

    @Column (name = "DDC")
    public String getDdc()
    {
        return ddc;
    }

    public void setDdc(String ddc)
    {
        this.ddc = ddc;
    }

    @Column (name = "DDN_Wave")
    public String getDdnWave()
    {
        return ddnWave;
    }

    public void setDdnWave(String ddnWave)
    {
        this.ddnWave = ddnWave;
    }

    @Column (name = "HoldMusic_Wave")
    public String getHoldMusicWave()
    {
        return holdMusicWave;
    }

    public void setHoldMusicWave(String holdMusicWave)
    {
        this.holdMusicWave = holdMusicWave;
    }

    @Column (name = "OW1_ID")
    public String getOw1Id()
    {
        return ow1Id;
    }

    public void setOw1Id(String ow1Id)
    {
        this.ow1Id = ow1Id;
    }

    @Column (name = "OW2_ID")
    public String getOw2Id()
    {
        return ow2Id;
    }

    public void setOw2Id(String ow2Id)
    {
        this.ow2Id = ow2Id;
    }

    @Column (name = "Greeting_Sentence")
    public String getGreetingSentence()
    {
        return greetingSentence;
    }

    public void setGreetingSentence(String greetingSentence)
    {
        this.greetingSentence = greetingSentence;
    }

    @Column (name = "Qualification_ID")
    public String getQualificationId()
    {
        return qualificationId;
    }

    public void setQualificationId(String qualificationId)
    {
        this.qualificationId = qualificationId;
    }

    @Column (name = "Qualification_Level")
    public int getQualificationLevel()
    {
        return qualificationLevel;
    }

    public void setQualificationLevel(int qualificationLevel)
    {
        this.qualificationLevel = qualificationLevel;
    }

    @Column (name = "Call_Recording")
    public boolean isCallRecording()
    {
        return callRecording;
    }

    public void setCallRecording(boolean callRecording)
    {
        this.callRecording = callRecording;
    }

    @Column (name = "Manual_Connect")
    public boolean isManualConnect()
    {
        return manualConnect;
    }

    public void setManualConnect(boolean manualConnect)
    {
        this.manualConnect = manualConnect;
    }

    @Column (name = "Ask_Code")
    public boolean isAskCode()
    {
        return askCode;
    }

    public void setAskCode(boolean askCode)
    {
        this.askCode = askCode;
    }

    @Column (name = "Listen_In")
    public boolean isListenIn()
    {
        return listenIn;
    }

    public void setListenIn(boolean listenIn)
    {
        this.listenIn = listenIn;
    }

    @Column (name = "Deleted")
    public boolean isDeleted()
    {
        return deleted;
    }

    public void setDeleted(boolean deleted)
    {
        this.deleted = deleted;
    }

    @Column (name = "Mssg2Intrp_Active")
    public boolean isMssg2IntrpActive()
    {
        return mssg2IntrpActive;
    }

    public void setMssg2IntrpActive(boolean mssg2IntrpActive)
    {
        this.mssg2IntrpActive = mssg2IntrpActive;
    }
    
    @Column (name = "Mssg2Intrp_Wave")
    public String getMssg2IntrpWave()
    {
        return mssg2IntrpWave;
    }

    public void setMssg2IntrpWave(String mssg2IntrpWave)
    {
        this.mssg2IntrpWave = mssg2IntrpWave;
    }

    @Column (name = "Mssg2Intrp_Text")
    public String getMssg2IntrpText()
    {
        return mssg2IntrpText;
    }

    public void setMssg2IntrpText(String mssg2IntrpText)
    {
        this.mssg2IntrpText = mssg2IntrpText;
    }

    @Column (name = "Mssg2Intrp_ReqID_Active")
    public boolean isMssg2IntrpReqIDActive()
    {
        return mssg2IntrpReqIDActive;
    }

    public void setMssg2IntrpReqIDActive(boolean mssg2IntrpReqIDActive)
    {
        this.mssg2IntrpReqIDActive = mssg2IntrpReqIDActive;
    }

    @Column (name = "dept_var")
	public String getDeptVar() {
		return deptVar;
	}

	public void setDeptVar(String deptVar) {
		this.deptVar = deptVar;
	}
}
