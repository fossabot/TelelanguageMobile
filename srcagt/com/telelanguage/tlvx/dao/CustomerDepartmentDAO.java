package com.telelanguage.tlvx.dao;

import java.util.List;

import com.telelanguage.tlvx.model.Customer;
import com.telelanguage.tlvx.model.CustomerDepartment;
import com.telelanguage.tlvx.service.TLVXManager;

/**
 * CustomerDepartmentDAO
 */
public class CustomerDepartmentDAO
{

    @SuppressWarnings("unchecked")
	public List<CustomerDepartment> findCustomerDepartments(String customerId)
    {
        return TLVXManager.getSession()
        		.createQuery("from CustomerDepartment WHERE customerId = :customerId ORDER BY name")
				.setParameter("customerId", customerId)
        		.list();
    }

    @SuppressWarnings("unchecked")
	public CustomerDepartment findByDepartmentId(String id)
    {
        List<CustomerDepartment> customers = TLVXManager.getSession()
        		.createQuery("from CustomerDepartment WHERE ID = :id ORDER BY name")
        		.setParameter("id", id)
        		.list();
        if (customers != null && customers.size() > 0)
        {
            return customers.get(0);
        }
        return null;
    }

	public boolean isDeptCodeValidForCustomer(Customer customer,
			String departmentCode) {
        return TLVXManager.getSession()
        		.createQuery("from CustomerDepartment WHERE customerId = :customerId and code = :departmentCode ORDER BY name")
				.setParameter("customerId", customer.getCustomerId())
				.setParameter("departmentCode", departmentCode)
        		.list().size()>0;
	}
}


