package com.telelanguage.tlvx.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="interpreters")
public class Interpreter
{
    private String interpreterId;
    private String accessCode;
    private String firstName;
    private String middleName;
    private String lastName;
    private String areaCode;
    private String phoneNumber;
    private String zipcode;
    private String country;
    private Date lastCall;
    private String gender;
    private String email;
    private int priorityCode;
    private double regularHourRate;
    private double afterHourRate;
    private boolean deleted;
    private boolean dprActive;
    private String subscriptionCode;
    private boolean active;
    private int callRecording;
    private int callRecordingOption;
    private boolean activeSession;
    private boolean onCall;
    private boolean salary;
    private int syncId;
    private int numMissedCalls;
    private int totalMissedCalls;
    private boolean loginLocked;
    private Long callId;
	private boolean webPhone;
    private String webPhoneSipAddress;
    private String onWebSite;
    private boolean allowPSTN;
    private boolean allowWebphone;
    private boolean allowVideo;
    private boolean video;
    private boolean videoOnly;
    private int noAnswerLogouts;
    private boolean forceVideoOnly;
    private String note;

	@Id
    @Column(name="ID")
    public String getInterpreterId()
    {
        return this.interpreterId;
    }
    
    public boolean equals (Object obj)
    {
        if (obj instanceof Interpreter)
        {
            return this.interpreterId!=null && this.interpreterId.equals(((Interpreter)obj).getInterpreterId());
        }
        return false;
    }
    
    public int hashCode ()
    {
        return this.interpreterId==null ? super.hashCode() : this.interpreterId.hashCode();
    }
    
    public void setInterpreterId(String interpreterId)
    {
        this.interpreterId = interpreterId;
    }
    
    @Column(name="Access_Code")
    public String getAccessCode()
    {
        return this.accessCode;
    }
    public void setAccessCode(String accessCode)
    {
        this.accessCode = accessCode;
    }
    
    @Column(name="Active", columnDefinition="tinyint")
    public boolean getActive()
    {
        return this.active;
    }
    public void setActive(boolean active)
    {
        this.active = active;
    }
    
    @Column(name="Active_Session", columnDefinition="tinyint")
    public boolean getActiveSession()
    {
        return this.activeSession;
    }
    public void setActiveSession(boolean activeSession)
    {
        this.activeSession = activeSession;
    }
    
    @Column(name="AHR")
    public double getAfterHourRate()
    {
        return this.afterHourRate;
    }
    public void setAfterHourRate(double afterHourRate)
    {
        this.afterHourRate = afterHourRate;
    }
    
    @Column(name="Area_Code")
    public String getAreaCode()
    {
        return this.areaCode;
    }
    public void setAreaCode(String areaCode)
    {
        this.areaCode = areaCode;
    }
    
    @Column(name="Call_Recording")
    public int getCallRecording()
    {
        return this.callRecording;
    }
    public void setCallRecording(int callRecording)
    {
        this.callRecording = callRecording;
    }
    
    @Column(name="Call_Recording_Option")
    public int getCallRecordingOption()
    {
        return this.callRecordingOption;
    }
    public void setCallRecordingOption(int callRecordingOption)
    {
        this.callRecordingOption = callRecordingOption;
    }
    
    @Column(name="Country")
    public String getCountry()
    {
        return this.country;
    }
    public void setCountry(String country)
    {
        this.country = country;
    }
    
    @Column(name="Deleted", columnDefinition="tinyint")
    public boolean getDeleted()
    {
        return this.deleted;
    }
    public void setDeleted(boolean deleted)
    {
        this.deleted = deleted;
    }
    
    @Column(name="DPR_Active", columnDefinition="tinyint")
    public boolean isDprActive()
    {
        return this.dprActive;
    }
    public void setDprActive(boolean dprActive)
    {
        this.dprActive = dprActive;
    }
    
    @Column(name="Email")
    public String getEmail()
    {
        return this.email;
    }
    public void setEmail(String email)
    {
        this.email = email;
    }
    
    @Column(name="First_Name")
    public String getFirstName()
    {
        return this.firstName;
    }
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }
    
    @Column(name="Gender")
    public String getGender()
    {
        return this.gender;
    }
    public void setGender(String gender)
    {
        this.gender = gender;
    }
    
    @Column(name="Last_Name")
    public String getLastName()
    {
        return this.lastName;
    }
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }
    
    @Column(name="Login_Locked", columnDefinition="tinyint")
    public boolean getLoginLocked()
    {
        return this.loginLocked;
    }
    public void setLoginLocked(boolean loginLocked)
    {
        this.loginLocked = loginLocked;
    }
    
    @Column(name="Middle_Name")
    public String getMiddleName()
    {
        return this.middleName;
    }
    public void setMiddleName(String middleName)
    {
        this.middleName = middleName;
    }
    
    @Column(name="Num_Missed_Calls")
    public int getNumMissedCalls()
    {
        return this.numMissedCalls;
    }
    public void setNumMissedCalls(int numMissedCalls)
    {
        this.numMissedCalls = numMissedCalls;
    }
    
    @Column(name="On_Call", columnDefinition="tinyint", updatable=false)
    public boolean getOnCall()
    {
        return this.onCall;
    }
    public void setOnCall(boolean onCall)
    {
        this.onCall = onCall;
    }
    
    @Column(name="Phone_Number")
    public String getPhoneNumber()
    {
        return this.phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }
    
    @Column(name="Priority_Code")
    public int getPriorityCode()
    {
        return this.priorityCode;
    }
    public void setPriorityCode(int priorityCode)
    {
        this.priorityCode = priorityCode;
    }
    
    @Column(name="RHR")
    public double getRegularHourRate()
    {
        return this.regularHourRate;
    }
    public void setRegularHourRate(double regularHourRate)
    {
        this.regularHourRate = regularHourRate;
    }
    
    @Column(name="Salary", columnDefinition="tinyint")
    public boolean getSalary()
    {
        return this.salary;
    }
    public void setSalary(boolean salary)
    {
        this.salary = salary;
    }
    
    @Column(name="Subscription_Code")
    public String getSubscriptionCode()
    {
        return this.subscriptionCode;
    }
    public void setSubscriptionCode(String subscriptionCode)
    {
        this.subscriptionCode = subscriptionCode;
    }
    
    @Column(name="Sync_ID")
    public int getSyncId()
    {
        return this.syncId;
    }
    public void setSyncId(int syncId)
    {
        this.syncId = syncId;
    }
    
    @Column(name="Total_Missed_Calls")
    public int getTotalMissedCalls()
    {
        return this.totalMissedCalls;
    }
    public void setTotalMissedCalls(int totalMissedCalls)
    {
        this.totalMissedCalls = totalMissedCalls;
    }
    
    @Column(name="Zipcode")
    public String getZipcode()
    {
        return this.zipcode;
    }
    public void setZipcode(String zipcode)
    {
        this.zipcode = zipcode;
    }

    @Column(name="lastCall")
    public Date getLastCall()
    {
        return lastCall;
    }
    public void setLastCall(Date lastCall)
    {
        this.lastCall = lastCall;
    }
    
//    @ManyToOne   //test making sure getObject(id) works on this after uncommenting
//    @JoinColumn(name = "callId")
//    public Call getCall()
//    {
//        return call;
//    }
//
//    public void setCall(Call call)
//    {
//        this.call = call;
//    }
//    
    @Column(name="webPhone", columnDefinition="tinyint")
    public boolean getWebPhone()
    {
        return this.webPhone;
    }
    public void setWebPhone(boolean webPhone)
    {
        this.webPhone = webPhone;
    }
    
    @Column(name="webPhoneSipAddress")
	public String getWebPhoneSipAddress() {
		return webPhoneSipAddress;
	}
	public void setWebPhoneSipAddress(String webPhoneSipAddress) {
		this.webPhoneSipAddress = webPhoneSipAddress;
	}

	@Column
	public String getOnWebSite() {
		return onWebSite;
	}
	public void setOnWebSite(String onWebPage) {
		this.onWebSite = onWebPage;
	}

	@Column
	public boolean getAllowPSTN() {
		return allowPSTN;
	}
	
	public void setAllowPSTN(boolean allowPSTN) {
		this.allowPSTN = allowPSTN;
	}

	@Column
	public boolean isAllowWebphone() {
		return allowWebphone;
	}

	public void setAllowWebphone(boolean allowWebphone) {
		this.allowWebphone = allowWebphone;
	}

	@Column
	public boolean isAllowVideo() {
		return allowVideo;
	}

	public void setAllowVideo(boolean allowVideo) {
		this.allowVideo = allowVideo;
	}

	@Column
	public boolean isVideo() {
		return video;
	}

	public void setVideo(boolean video) {
		this.video = video;
	}
	
	@Column
	public boolean isVideoOnly() {
		return videoOnly;
	}

	public void setVideoOnly(boolean videoOnly) {
		this.videoOnly = videoOnly;
	}

	@Column
	public int getNoAnswerLogouts() {
		return noAnswerLogouts;
	}

	public void setNoAnswerLogouts(int noAnswerLogouts) {
		this.noAnswerLogouts = noAnswerLogouts;
	}

	@Column
	public boolean isForceVideoOnly() {
		return forceVideoOnly;
	}

	public void setForceVideoOnly(boolean forceVideoOnly) {
		this.forceVideoOnly = forceVideoOnly;
	}
	
	@Column
    public Long getCallId() {
		return callId;
	}
	
	public void setCallId(Long callId) {
		this.callId = callId;
	}

	@Column
  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }
	
	
}
