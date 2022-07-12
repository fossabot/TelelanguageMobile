package com.telelanguage.tlvx.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * CustomerOptionData
 */
@Entity 
@Table(name="customer_option_data_t")
public class CustomerOptionData
{
	private Long customerOptionDataId;
    private String customerOptionId;
    private String value;
    private Call call;
    
    @Id
    @GeneratedValue
    @Column(name = "customerOptionDataId")
    public Long getId ()
    {
        return customerOptionDataId;
    }
    
    public void setId(Long customerOptionDataId) {
    	this.customerOptionDataId = customerOptionDataId;
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
    @Column(unique=false,length=255,nullable=false)
    public String getCustomerOptionId()
    {
        return customerOptionId;
    }
    public void setCustomerOptionId(String option)
    {
        this.customerOptionId = option;
    }
    @Column(unique=false,length=255,nullable=false)
    public String getValue()
    {
        return value;
    }
    public void setValue(String value)
    {
        this.value = value;
    }
    @Override
    public String toString() {
    	return "CustOptData: "+call.getId()+" "+customerOptionId+":"+value;
    }
}
