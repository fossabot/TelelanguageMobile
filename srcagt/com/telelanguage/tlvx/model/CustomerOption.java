package com.telelanguage.tlvx.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * CustomerOption
 */
@Entity
@Table(name = "customer_options")
public class CustomerOption
{
	private String id;
    private String customerId;
    private String optionContentId;
    private String optionNumber;
    private String optionConditions;
    private String subscriptionCode;
    private String customerOptionId;
	private String label;
    private String description;
    private String scOriginal;
    
    @Override
    public boolean equals (Object obj)
    {
    	if (obj instanceof CustomerOption)
    	{
    		return ((CustomerOption)obj).id.equals(id);
    	}
    	return false;
    }

    @Id
    @Column(name="ID")
    public String getId()
    {
        return id;
    }
    
    public void setId (String id)
    {
    	this.id = id;
    }

    @Column(name="Customer_ID")
	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	@Column(name="Option_Content_ID")
	public String getOptionContentId() {
		return optionContentId;
	}

	public void setOptionContentId(String optionContentId) {
		this.optionContentId = optionContentId;
	}

	@Column(name="Option_Number")
	public String getOptionNumber() {
		return optionNumber;
	}

	public void setOptionNumber(String optionNumber) {
		this.optionNumber = optionNumber;
	}

	@Column(name="Option_Conditions")
	public String getOptionConditions() {
		return optionConditions;
	}

	public void setOptionConditions(String optionConditions) {
		this.optionConditions = optionConditions;
	}

	@Column(name="Subscription_Code")
	public String getSubscriptionCode() {
		return subscriptionCode;
	}

	public void setSubscriptionCode(String subscriptionCode) {
		this.subscriptionCode = subscriptionCode;
	}
	
	@Column(name="customerOptionId")
    public String getCustomerOptionId() {
		return customerOptionId;
	}

	public void setCustomerOptionId(String customerOptionId) {
		this.customerOptionId = customerOptionId;
	}

	@Column(name="label")
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Column(name="description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="SC_Original")
	public String getScOriginal() {
		return scOriginal;
	}

	public void setScOriginal(String scOriginal) {
		this.scOriginal = scOriginal;
	}
    
}
