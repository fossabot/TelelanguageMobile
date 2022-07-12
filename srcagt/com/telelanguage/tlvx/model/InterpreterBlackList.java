package com.telelanguage.tlvx.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "interpreter_blacklist_t")
public class InterpreterBlackList
{
	private Long id;
    private String accessCode;
    private String interpreterId;

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
    
    @Column(nullable = false, length = 255, unique = true)
    public String getInterpreterId()
    {
        return interpreterId;
    }
    
    public void setInterpreterId(String interpreterId)
    {
        this.interpreterId = interpreterId;
    }

	public String getAccessCode() 
	{
		return accessCode;
	}

	public void setAccessCode(String accessCode) 
	{
		this.accessCode = accessCode;
	}
}
