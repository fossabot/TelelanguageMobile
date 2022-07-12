package com.telelanguage.tlvx.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * InterpreterStatus
 */
@Entity
@Table(name = "interpreter_status_t")
public class InterpreterStatus
{
    public static final int INTERPRETER_STATUS_IDLE = 0;
    public static final int INTERPRETER_STATUS_BUSY = 1;    
    
    private Long interpreterStatusId;
    private String interpreterId;
    private int status;
    
    @Id
    @GeneratedValue
    @Column(name = "interpreterStatusId")
    public Long getId()
    {
        return interpreterStatusId;
    }
    
    public void setId(Long interpreterStatusId) {
    	this.interpreterStatusId = interpreterStatusId;
    }
    
    @Column(nullable = false, length = 255, unique = true)
    public String getInterpreterId()
    {
        return interpreterId;
    }
    
    public void setInterpreterId(String interpreterId)
    {
        this.interpreterId = interpreterId;
    }
    
    @Column(nullable = false)
    public int getStatus()
    {
        return status;
    }
    
    public void setStatus(int status)
    {
        this.status = status;
    }
}
