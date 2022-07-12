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

/**
 * CallEvent
 */
@Entity
@Table(name = "call_event_t")
public class CallEvent
{
    public static final int CUSTOMER_CONNECT = 1;
    public static final int CUSTOMER_DISCONNECT = 2;
    public static final int AGENT_CONNECT = 3;
    public static final int AGENT_REJECT = 4;
    public static final int AGENT_DISCONNECT = 5;
    public static final int INTERPRETER_CONNECT = 6;
    public static final int INTERPRETER_DISCONNECT = 7;
    public static final int INTERPRETER_REJECTED = 8;
    public static final int THIRDPARTY_CONNECT = 9;
    public static final int THIRDPARTY_DISCONNECT = 10;
    public static final int THIRDPARTY_REJECTED = 11;
    public static final int AGENT_REQUEUE_CALL = 12;
    public static final int INTERPRETER_START = 13;
    public static final int CUSTOMER_QUEUED = 14;
    public static final int AGENT_COMPLETE_CALL = 15;
    public static final int AGENT_SAVE_CALL = 16;
    public static final int INTERPRETER_ON_CALL = 17;
    public static final int INTERPRETER_CONNECTING = 18;
    public static final int TRANSFER_CONNECT = 19;
    public static final int TRANSFER_DISCONNECT = 20;
    public static final int TRANSFER_REJECTED = 21;
    public static final int CUSTOMER_INTERPRETER_CONNECT = 22;
    public static final int IVR_CONNECT = 23;
    public static final int ACCESS_CODE_CONFIRMED = 24;
    public static final int LANGUAGE_CODE_CONFIRMED = 25;
    public static final int DEPT_CODE_CONFIRMED = 26;
    public static final int IVR_DISCONNECT = 27;
    public static final int INTERPRETER_MANUAL_CONNECTING = 28;
    public static final int CCXML_SERVER_ERROR = -1;
    public static final int INTERPRETER_IVR_EXIT_REASON = 29;
    public static final int CUSTOMER_CLICKED_HANGUP = 30;
    public static final int CUSTOMER_VIDEO_SESSION_STARTED = 31;
    public static final int INTERPRETER_VIDEO_SESSION_STARTED = 32;
    
    private Long id;
    private Call call;
    private Date date;
    private int eventType;
    private String payload;
    private String payload2;

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

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, name="date")
    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    @Column(nullable = true, length = 255, unique = false, name="payload")
    public String getPayload()
    {
        return payload;
    }

    public void setPayload(String payload)
    {
        this.payload = payload;
    }

    @Column(nullable = true, length = 255, unique = false, name = "payload2")
    public String getPayload2()
    {
        return payload2;
    }

    public void setPayload2(String payload2)
    {
        this.payload2 = payload2;
    }

    @Column(nullable = false, name = "eventType")
    public int getEventType()
    {
        return eventType;
    }

    public void setEventType(int eventType)
    {
        this.eventType = eventType;
    }
}
