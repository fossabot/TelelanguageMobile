package com.telelanguage.tlvx.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * CustomerOptionContent
 */
@Entity
@Table(name = "customer_options_contents")
public class CustomerOptionContent
{
	private String id;
    private String optionContent;
	private String subscriptionCode;
    private String optionLabel;
    private String scOriginal;
    
    @Override
    public boolean equals (Object obj)
    {
    	if (obj instanceof CustomerOptionContent)
    	{
    		return ((CustomerOptionContent)obj).id.equals(id);
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

    @Column(name="Option_Content")
    public String getOptionContent() {
		return optionContent;
	}

	public void setOptionContent(String optionContent) {
		this.optionContent = optionContent;
	}

	@Column(name="Subscription_Code")
	public String getSubscriptionCode() {
		return subscriptionCode;
	}

	public void setSubscriptionCode(String subscriptionCode) {
		this.subscriptionCode = subscriptionCode;
	}

	@Column(name="Option_Label")
	public String getOptionLabel() {
		return optionLabel;
	}

	public void setOptionLabel(String optionLabel) {
		this.optionLabel = optionLabel;
	}

	@Column(name="SC_Original")
	public String getScOriginal() {
		return scOriginal;
	}

	public void setScOriginal(String scOriginal) {
		this.scOriginal = scOriginal;
	}
	
	@Override
	public String toString() {
		return "CustOption: "+optionLabel;
	}
    
}
