package com.telelanguage.tlvx.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * CallStat
 */
@Entity
@Table(name = "call_stat_t")
public class CallStat
{
    private Long callStatId;
    private Agent agent;
    private int length;
    private Call call; 
    
    /**
     * get the identifier for this object
     *
     * @return the id
     */
    @Id
    @GeneratedValue
    @Column(name = "callStatId")
    public Long getId()
    {
        return callStatId;
    }
    
    public void setId(Long callStatId) {
    	this.callStatId = callStatId;
    }

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, name = "agentId")
    public Agent getAgent()
    {
        return agent;
    }

    public void setAgent(Agent agent) 
    {
        this.agent = agent;
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

    public int getLength()
    {
        return length;
    }

    public void setLength(int length)
    {
        this.length = length;
    }
}