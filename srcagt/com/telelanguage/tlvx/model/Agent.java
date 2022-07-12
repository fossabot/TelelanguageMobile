package com.telelanguage.tlvx.model;

import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "agent_t")
public class Agent
{  
    //FIXME: we need to use subscriptonCode here or in User so that we can have agents
    //for different customers

	private Long agentId;
    private User user;
    private String sipUri;
    private String defaultUri;
    private boolean online;
    private boolean available;
    private boolean busy;
    private Date lastCallDate;
    private Date lastAttemptDate;
    private Date lastLogin;
    private boolean personalHold;

    /**
     * get the identifier for this object
     *
     * @return the id
     */
    @Id
    @GeneratedValue
    @Column(name = "agentId")
    public Long getId()
    {
        return agentId;
    }
    
    public void setId(Long agentId)
    {
        this.agentId = agentId;
    }

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, name = "userId")
    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    @Column(nullable = false, length = 255, unique = true)
    public String getSipUri()
    {
        return sipUri;
    }

    public void setSipUri(String sipUri)
    {
        this.sipUri = sipUri;
    }

    @Column(nullable = false)
    public boolean isOnline()
    {
        return online;
    }

    public void setOnline(boolean online)
    {
        this.online = online;
    }

    @Column(nullable = false)
    public boolean isAvailable()
    {
        return available;
    }

    public void setAvailable(boolean available)
    {
        this.available = available;
    }

    @Column(nullable = false)
    public boolean isBusy()
    {
        return busy;
    }

    public void setBusy(boolean busy)
    {
        this.busy = busy;
    }    
    
    @Column(nullable = false)
    public boolean isPersonalHold()
    {
        return personalHold;
    }

    public void setPersonalHold(boolean personalHold)
    {
        this.personalHold = personalHold;
    }    
    
    @Temporal(TemporalType.TIMESTAMP)
    public Date getLastCallDate()
    {
        return lastCallDate;
    }
    
    public void setLastCallDate(Date lastCallDate)
    {
        this.lastCallDate = lastCallDate;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getLastAttemptDate()
    {
        return lastAttemptDate;
    }
    
    public void setLastAttemptDate(Date lastAttemptDate)
    {
        this.lastAttemptDate = lastAttemptDate;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    public Date getLastLogin()
    {
    	return lastLogin;
    }
    
    public void setLastLogin(Date lastLogin)
    {
    	this.lastLogin = lastLogin;
    }
    
    public String getDefaultUri()
    {
        return defaultUri;
    }

    public void setDefaultUri(String defaultUri)
    {
        this.defaultUri = defaultUri;
    }
    
    @Override
    public String toString() {
    	return "Agent "+agentId+" online: "+online+" available: "+available+" busy: "+busy+" sipUri: "+sipUri;
    }
}
