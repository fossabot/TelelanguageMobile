package com.telelanguage.tlvx.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * CallDetailRecord
 */
@Entity
@Table(name = "cdr_t")
public class CallDetailRecord
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Call call;
    private String ani;
    private String dnis;
    private String accessCode;
    private String language;
    private String deptCode;
    private String interpreterId;
    private Date startTime;
    private Date endTime;
    private boolean ivr;
    private int agent_connects;
    private int requeues;
    private int interpreter_attempts;
    private int interpreter_rejects;
    private float call_queue_time;
    private float ivr_time;
    private float interpreter_connect_time;
    private float interpreter_dial_time;
    private float interpreter_talk_time;
    private float thirdparty_talk_time;
    private Date interpreter_start_time;
    private Date interpreter_end_time;
    private String option_1;
    private String option_2;
    private String option_3;
    private String option_4;
    private String option_5;
    private String option_6;
    private String subscriptionCode;
    private String sipCallId;
    private Boolean video;

	/**
     * get the identifier for this object
     *
     * @return the id
     */
    @Id
    @GeneratedValue
    @Column(name = "id")
    public Long getId()
    {
        return id;
    }
    
    public void setId(Long id) {
    	this.id = id;
    }

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, name = "callId")
    public Call getCall()
    {
        return call;
    }

    public void setCall(Call call)
    {
        this.call = call;
    }

    public String getAni()
    {
        return ani;
    }

    public void setAni(String ani)
    {
        this.ani = ani;
    }

    public String getDnis()
    {
        return dnis;
    }

    public void setDnis(String dnis)
    {
        this.dnis = dnis;
    }

    public String getAccessCode()
    {
        return accessCode;
    }

    public void setAccessCode(String accessCode)
    {
        this.accessCode = accessCode;
    }

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    public String getDeptCode()
    {
        return deptCode;
    }

    public void setDeptCode(String deptCode)
    {
        this.deptCode = deptCode;
    }

    public String getInterpreterId()
    {
        return interpreterId;
    }

    public void setInterpreterId(String interpreterId)
    {
        this.interpreterId = interpreterId;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    public Date getEndTime()
    {
        return endTime;
    }

    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }

    public boolean isIvr()
    {
        return ivr;
    }

    public void setIvr(boolean ivr)
    {
        this.ivr = ivr;
    }

    public int getRequeues()
    {
        return requeues;
    }

    public void setRequeues(int requeues)
    {
        this.requeues = requeues;
    }

    public int getInterpreter_attempts()
    {
        return interpreter_attempts;
    }

    public void setInterpreter_attempts(int interpreter_attempts)
    {
        this.interpreter_attempts = interpreter_attempts;
    }

    public int getInterpreter_rejects()
    {
        return interpreter_rejects;
    }

    public void setInterpreter_rejects(int interpreter_rejects)
    {
        this.interpreter_rejects = interpreter_rejects;
    }

    public float getCall_queue_time()
    {
        return call_queue_time;
    }

    public void setCall_queue_time(float call_queue_time)
    {
        this.call_queue_time = call_queue_time;
    }

    public float getIvr_time()
    {
        return ivr_time;
    }

    public void setIvr_time(float ivr_time)
    {
        this.ivr_time = ivr_time;
    }

    public float getInterpreter_talk_time()
    {
        return interpreter_talk_time;
    }

    public void setInterpreter_talk_time(float interpreter_talk_time)
    {
        this.interpreter_talk_time = interpreter_talk_time;
    }

    public float getThirdparty_talk_time()
    {
        return thirdparty_talk_time;
    }

    public void setThirdparty_talk_time(float thirdparty_talk_time)
    {
        this.thirdparty_talk_time = thirdparty_talk_time;
    }

    public int getAgent_connects()
    {
        return agent_connects;
    }

    public void setAgent_connects(int agent_connects)
    {
        this.agent_connects = agent_connects;
    }

    public String getOption_1()
    {
        return option_1;
    }

    public void setOption_1(String option_1)
    {
        this.option_1 = option_1;
    }

    public String getOption_2()
    {
        return option_2;
    }

    public void setOption_2(String option_2)
    {
        this.option_2 = option_2;
    }

    public String getOption_3()
    {
        return option_3;
    }

    public void setOption_3(String option_3)
    {
        this.option_3 = option_3;
    }

    public String getOption_4()
    {
        return option_4;
    }

    public void setOption_4(String option_4)
    {
        this.option_4 = option_4;
    }

    public String getOption_5()
    {
        return option_5;
    }

    public void setOption_5(String option_5)
    {
        this.option_5 = option_5;
    }

    public String getOption_6()
    {
        return option_6;
    }

    public void setOption_6(String option_6)
    {
        this.option_6 = option_6;
    }

	public Date getInterpreter_start_time() 
	{
		return interpreter_start_time;
	}

	public void setInterpreter_start_time(Date interpreter_start_time) 
	{
		this.interpreter_start_time = interpreter_start_time;
	}

	public float getInterpreter_dial_time() 
	{
		return interpreter_dial_time;
	}

	public void setInterpreter_dial_time(float interpreter_dial_time) 
	{
		this.interpreter_dial_time = interpreter_dial_time;
	}

	public float getInterpreter_connect_time() 
	{
		return interpreter_connect_time;
	}

	public void setInterpreter_connect_time(float interpreter_connect_time) 
	{
		this.interpreter_connect_time = interpreter_connect_time;
	}

	public Date getInterpreter_end_time() 
	{
		return interpreter_end_time;
	}

	public void setInterpreter_end_time(Date interpreter_end_time) 
	{
		this.interpreter_end_time = interpreter_end_time;
	}
	
    public String getSubscriptionCode()
    {
        return subscriptionCode;
    }

    public void setSubscriptionCode(String subscriptionCode)
    {
        this.subscriptionCode = subscriptionCode;
    }
    
    public String getSipCallId() {
		return sipCallId;
	}

	public void setSipCallId(String sipCallId) {
		this.sipCallId = sipCallId;
	}

	public Boolean getVideo() {
		return video;
	}

	public void setVideo(Boolean video) {
		this.video = video;
	}
	
}
