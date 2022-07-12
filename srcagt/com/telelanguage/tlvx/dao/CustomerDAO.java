package com.telelanguage.tlvx.dao;

import com.telelanguage.tlvx.model.CredentialsRequest;
import com.telelanguage.tlvx.model.Customer;
import com.telelanguage.tlvx.model.ScheduleInterpreterRequest;
import com.telelanguage.tlvx.service.TLVXManager;

/**
 * CustomerDAO
 */
public class CustomerDAO {
	
	public Customer executeCustomerGet(String customerId) {
		return (Customer) TLVXManager.getSession()
        		.createQuery("from Customer where ID = :customerId")
        		.setParameter("customerId", customerId)
        		.uniqueResult();
	}

	public Customer findById(String id) {
		return (Customer) TLVXManager.getSession().get(Customer.class, id);
	}

	public Customer findByCode(String code) {
		return (Customer) TLVXManager.getSession()
        		.createQuery("from Customer where Code = :code")
        		.setParameter("code", code)
        		.uniqueResult();
	}

	public void save(ScheduleInterpreterRequest sir) {
		try {
			TLVXManager.getSession().save(sir);
			System.out.println("ScheduleInterpreterRequest id: "+sir.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void save(CredentialsRequest cr) {
		try {
			TLVXManager.getSession().save(cr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
