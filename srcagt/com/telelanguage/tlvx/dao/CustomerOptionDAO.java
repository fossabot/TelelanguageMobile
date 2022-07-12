package com.telelanguage.tlvx.dao;

import java.util.List;

import com.telelanguage.tlvx.model.CustomerOption;
import com.telelanguage.tlvx.service.TLVXManager;

/**
 * CustomerOptionDAO
 */
public class CustomerOptionDAO 
{
    @SuppressWarnings("unchecked")
	public List<CustomerOption> findByCustomerId(String customerId)
    {
        return TLVXManager.getSession()
        		.createQuery("select o from CustomerOption o,CustomerOptionContent c WHERE o.optionContentId=c.id AND o.customerId = :customerId ORDER BY o.optionNumber")
        		.setParameter("customerId", customerId)
        		.list();
    }
}
