package com.telelanguage.tlvx.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * CustomerDNIS
 */
@Entity
@Table(name = "customer_dnis")
public class CustomerDNIS
{
	private String customerDnisId;
    private boolean priority;
    private String customerId;
    private boolean askDepartment;
    //private boolean askDtmfPin;
    private boolean askLanguage;
    private boolean ivrEnabled;
    private String languageId;
    private String departmentId;
    private String dnisNumber;    
    
    @Id
	@Column (name = "ID")
	public String getCustomerDnisId() {
		return customerDnisId;
	}
    
    public void setCustomerDnisId(String customerDnisId) {
		this.customerDnisId = customerDnisId;
	}
    
    @Column (name = "Is_Priority")
    public boolean isPriority()
    {
        return priority;
    }

    public void setPriority(boolean priority)
    {
        this.priority = priority;
    }
    
    @Column (name = "Customer_ID")
    public String getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(String customerId)
    {
        this.customerId = customerId;
    }
    
    @Column (name = "Ask_Department")
    public boolean isAskDepartment()
    {
        return askDepartment;
    }

    public void setAskDepartment(boolean askDepartment)
    {
        this.askDepartment = askDepartment;
    }
    
    //@Column (name = "Ask_DtmfPin")
    @Transient
    public boolean isAskDtmfPin()
    {
        return false;
    }
    
    @Column (name = "Ask_Language")
    public boolean isAskLanguage()
    {
        return askLanguage;
    }

    public void setAskLanguage(boolean askLanguage)
    {
        this.askLanguage = askLanguage;
    }
    
    @Column (name = "Language_ID")
    public String getLanguageId()
    {
        return languageId;
    }

    public void setLanguageId(String languageId)
    {
        this.languageId = languageId;
    }
    
    @Column (name = "IVR_Enabled")
	public boolean isIvrEnabled() 
	{
		return ivrEnabled;
	}

	public void setIvrEnabled(boolean ivrEnabled) 
	{
		this.ivrEnabled = ivrEnabled;
	}
	
	@Column (name = "Department_ID")
	public String getDepartmentId() 
	{
		return departmentId;
	}

	public void setDepartmentId(String departmentId) 
	{
		this.departmentId = departmentId;
	}
	
	@Column (name = "DNIS_Number")
	public String getDnisNumber()
	{
		return this.dnisNumber;
	}
	
	public void setDnisNumber(String dnisNumber)
	{
		this.dnisNumber = dnisNumber;
	}
}