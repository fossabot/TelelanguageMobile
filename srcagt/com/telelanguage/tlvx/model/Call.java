package com.telelanguage.tlvx.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "call_t")
public class Call
{
    public static final String TL_CALLSESSION_AGENT_SET = "tl.callsession.agent.set";
    public static final String TL_CALLSESSION_AGENT_REJECT = "tl.callsession.agent.reject";
    public static final String TL_CALLSESSION_AGENT_REQUEUE = "tl.callsession.agent.requeue";
    public static final String TL_CALLSESSION_CUSTOMER_SET = "tl.callsession.customer.set";
    public static final String TL_CALLSESSION_ACCESSCODE_SET = "tl.callsession.accesscode.set";
    public static final String TL_CALLSESSION_COMPLETE_CALL = "tl.callsession.complete.call";
    public static final String TL_CALLSESSION_TRANSFER_CALL = "tl.callsession.transfer.call";
    public static final String TL_CALLSESSION_CUSTOMER_HOLD_ON = "tl.callsession.customer.hold.on";
    public static final String TL_CALLSESSION_CUSTOMER_HOLD_OFF = "tl.callsession.customer.hold.off";
    public static final String TL_CALLSESSION_CUSTOMER_CONNECT = "tl.callsession.customer.connect";
    public static final String TL_CALLSESSION_THIRDPARTY_HOLD_ON = "tl.callsession.thirdparty.hold.on";
    public static final String TL_CALLSESSION_THIRDPARTY_HOLD_OFF = "tl.callsession.thirdparty.hold.off";
    public static final String TL_CALLSESSION_THIRDPARTY_CONNECT = "tl.callsession.thirdparty.connect";
    public static final String TL_CALLSESSION_THIRDPARTY_DISCONNECT = "tl.callsession.thirdparty.disconnect";
    public static final String TL_CALLSESSION_INTERPRETER_CONNECT = "tl.callsession.interpreter.connect";
    public static final String TL_CALLSESSION_INTERPRETER_DISCONNECT = "tl.callsession.interpreter.disconnect";
    public static final String TL_CALLSESSION_INTERPRETER_HOLD_ON = "tl.callsession.interpreter.hold.on";
    public static final String TL_CALLSESSION_INTERPRETER_HOLD_OFF = "tl.callsession.interpreter.hold.off";
    public static final String TL_CALLSESSION_MARK_INTERPRETER = "tl.callsession.mark.interpreter";
    public static final String TL_CALLSESSION_INTERPRETER_LIST = "tl.callsession.interpreter.list";
	
	public static final int CALL_STATUS_OUTBOUND_READY = 0;
    public static final int CALL_STATUS_QUEUED = 1;
    public static final int CALL_STATUS_FORWARDED_TO_AGENT = 2;
    public static final int CALL_STATUS_AGENT_ANSWERED = 3;
    public static final int CALL_STATUS_FINISHED = 4;
    public static final int CALL_STATUS_IVR = 5;
	
	private Long callId;
    private String callSessionId;
    private Agent agent;
    private Date startDate;
    private Date endDate;
    private Date lastAnsweredDate;
    private boolean priorityCall;
    private String customer;
    private String subscriptionCode;
    private int status;
    private Agent reservedAgent;
    private String departmentCode;
    private String language;
    private String ccxmlServer;
    private String accessCode;
    private String appServer;
    private String lastReason;
    private Boolean video = Boolean.FALSE;

    @Column(nullable = true, length = 128, unique = false)
    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    @Column(nullable = true, length = 128, unique = false)
    public String getDepartmentCode()
    {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode)
    {
        this.departmentCode = departmentCode;
    }

    public String getCustomer()
    {
        return customer;
    }

    public void setCustomer(String customer)
    {
        this.customer = customer;
    }

    @ManyToOne(optional = true)
    @JoinColumn(nullable = true, name = "agentId")
    public Agent getAgent()
    {
		return agent;
	}

	public void setAgent(Agent agent) 
	{
		this.agent = agent;
	}
    
    @ManyToOne(optional = true)
    @JoinColumn(nullable = true, name = "reservedAgentId")
    public Agent getReservedAgent()
    {
        return reservedAgent;
    }

    public void setReservedAgent(Agent agent) 
    {
        this.reservedAgent = agent;
    }

	@Column(nullable = false, length = 255, unique = true)
	public String getCallSessionId() 
    {
		return callSessionId;
	}

	public void setCallSessionId(String callSessionId) 
	{
		this.callSessionId = callSessionId;
	}

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = true)
	public Date getEndDate() 
    {
		return endDate;
	}

	public void setEndDate(Date endDate) 
	{
		this.endDate = endDate;
	}

    @Column(nullable = true)
	public boolean isPriorityCall() 
	{
		return priorityCall;
	}

	public void setPriorityCall(boolean priorityCall) 
	{
		this.priorityCall = priorityCall;
	}

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    public Date getStartDate() 
	{
		return startDate;
	}

	public void setStartDate(Date startDate) 
	{
		this.startDate = startDate;
	}

    @Column(nullable = true, length = 255, unique = false)
	public String getSubscriptionCode() 
	{
		return subscriptionCode;
	}

	public void setSubscriptionCode(String subscriptionCode) 
	{
		this.subscriptionCode = subscriptionCode;
	}

    @Id
    @GeneratedValue
    @Column(name = "callId")
    public Long getId()
    {
        return callId;
    }
    
    public void setId(Long callId)
    {
        this.callId = callId;
    }

    @Column
    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }
    
    @Column
    public Date getLastAnsweredDate()
    {
        return lastAnsweredDate;
    }

    public void setLastAnsweredDate(Date lastAnsweredDate)
    {
        this.lastAnsweredDate = lastAnsweredDate;
    }

    @Column
	public String getCcxmlServer() 
	{
		return ccxmlServer;
	}

	public void setCcxmlServer(String ccxmlServer) 
	{
		this.ccxmlServer = ccxmlServer;
	}

	@Column
	public String getAccessCode() 
	{
		return accessCode;
	}

	public void setAccessCode(String accessCode) 
	{
		this.accessCode = accessCode;
	}
	
	@Override
	public String toString() {
		String reservedAgentInfo = "no reserved agent";
		if (reservedAgent != null) reservedAgentInfo = reservedAgent.getUser().getEmail();
		return "Call: "+callSessionId+" "+reservedAgentInfo+" "+ccxmlServer+" "+customer;
	}

	@Column
	public String getAppServer() {
		return appServer;
	}

	public void setAppServer(String appServer) {
		this.appServer = appServer;
	}

	@Column
	public String getLastReason() {
		return lastReason;
	}

	public void setLastReason(String lastReason) {
		this.lastReason = lastReason;
	}
	
    @Column(nullable=false)
	public Boolean isVideo() 
	{
		return video;
	}

	public void setVideo(Boolean video) 
	{
		this.video = video;
	}
}
