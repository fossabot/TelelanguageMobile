package com.telelanguage.tlvx.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * CustomerDepartment
 */
@Entity
@Table(name="customer_departments")
public class CustomerDepartment
{
	private String id;
	private String departmentId;
	private String customerId;
	private String name;
    private String code;

    @Id
    @Column (name = "ID")
    public String getId() {
		return id;
	}
    
	public void setId(String id) {
		this.id = id;
	}

	@Column (name = "departmentId")
	public String getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(String departmentId) {
		this.departmentId = departmentId;
	}
	
	@Column (name = "Customer_ID")
    public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	@Column (name = "Department_Name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column (name = "Department_Code")
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
    public boolean equals (Object obj)
    {
        if (obj instanceof CustomerDepartment)
        {
            return ((CustomerDepartment)obj).departmentId.equals(departmentId);
        }
        return false;
    }
}
