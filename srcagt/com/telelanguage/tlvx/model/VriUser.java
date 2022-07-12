package com.telelanguage.tlvx.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tlvri_users")
public class VriUser
{
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;
    
	@ManyToOne
    private Customer customer;
	
	@ManyToOne
    private CustomerDepartment department;
	
	@Column 
	private String email;
	
	@Column
    private String password;
	
	@Column
    private String sval;
	
	@Column
    private Boolean active;
	
	@Column
    private Boolean deleted;
	
	@Column
	private String login_token;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public CustomerDepartment getDepartment() {
		return department;
	}

	public void setDepartment(CustomerDepartment department) {
		this.department = department;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSval() {
		return sval;
	}

	public void setSval(String sval) {
		this.sval = sval;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public String getLogin_token() {
		return login_token;
	}

	public void setLogin_token(String login_token) {
		this.login_token = login_token;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
