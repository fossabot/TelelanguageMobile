package com.telelanguage.tlvx.dao;

import org.apache.log4j.Logger;

import com.telelanguage.tlvx.model.CustomerDNIS;
import com.telelanguage.tlvx.service.TLVXManager;

/**
 * CustomerDNISDAO
 */
public class CustomerDNISDAO
{
	private static final Logger LOG = Logger.getLogger(CustomerDNISDAO.class);

    public CustomerDNIS executeCustomerDNISSCCustomerIdGetAST(String dnis, String ani)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("searching for dnis = " + dnis);
        return (CustomerDNIS) TLVXManager.getSession()
        		.createQuery("FROM CustomerDNIS AS c WHERE c.dnisNumber = :dnis")
        		.setParameter("dnis", dnis)
        		.setMaxResults(1)
        		.uniqueResult();
    }
}

