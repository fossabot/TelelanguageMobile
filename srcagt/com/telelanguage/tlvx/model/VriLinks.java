package com.telelanguage.tlvx.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tlvri_links")
public class VriLinks
{
	@Id
	@Column 
	private String marketingText;
	
	@Column
    private String forgotPasswordLink;
	
	@Column
    private String alreadyCustomerLink;
	
	@Column
    private String openAnAccountLink;
	
	@Column
    private String preScheduleInterpreterLink;

	public String getMarketingText() {
		return marketingText;
	}

	public void setMarketingText(String marketingText) {
		this.marketingText = marketingText;
	}

	public String getForgotPasswordLink() {
		return forgotPasswordLink;
	}

	public void setForgotPasswordLink(String forgotPasswordLink) {
		this.forgotPasswordLink = forgotPasswordLink;
	}

	public String getAlreadyCustomerLink() {
		return alreadyCustomerLink;
	}

	public void setAlreadyCustomerLink(String alreadyCustomerLink) {
		this.alreadyCustomerLink = alreadyCustomerLink;
	}

	public String getOpenAnAccountLink() {
		return openAnAccountLink;
	}

	public void setOpenAnAccountLink(String openAnAccountLink) {
		this.openAnAccountLink = openAnAccountLink;
	}

	public String getPreScheduleInterpreterLink() {
		return preScheduleInterpreterLink;
	}

	public void setPreScheduleInterpreterLink(String preScheduleInterpreterLink) {
		this.preScheduleInterpreterLink = preScheduleInterpreterLink;
	}

	@Override
	public String toString() {
		return preScheduleInterpreterLink;
	}
}
