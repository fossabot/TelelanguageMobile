package com.telelanguage.tlvx.dao;

import java.util.List;

import com.telelanguage.tlvx.model.CustomerOptionContent;
import com.telelanguage.tlvx.service.TLVXManager;

/**
 * CustomerOptionDAO
 */
public class CustomerOptionContentDAO 
{
    @SuppressWarnings("unchecked")
	public List<CustomerOptionContent> findByCustomerId(String customerId)
    {
        return TLVXManager.getSession()
        		.createQuery("select c from CustomerOption o,CustomerOptionContent c WHERE o.optionContentId=c.id AND o.customerId = :customerId ORDER BY o.optionNumber")
        		.setParameter("customerId", customerId)
        		.list();
    }
}
