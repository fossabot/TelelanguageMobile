package com.telelanguage.tlvx.dao;

import java.util.ArrayList;
import java.util.List;

import com.telelanguage.tlvx.model.CustomerOptionData;
import com.telelanguage.tlvx.service.TLVXManager;

/**
 * CustomerOptionDataDAO
 */
public class CustomerOptionDataDAO
{
	public String getValueForCustOptionCall(String callId, String optionId) {
		return (String) TLVXManager.getSession()
				.createSQLQuery("select value from customer_option_data_t " +
				             "where callId = :callId " +
				             "  and customerOptionId = :optionId ")
				.setParameter("callId", callId)
				.setParameter("optionId", optionId)
				.setMaxResults(1)
				.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public List<CustomerOptionData> findByCallId(String callId) {
		return TLVXManager.getSession()
				.createQuery("from com.telelanguage.tlvx.model.CustomerOptionData cod where cod.call.id = :callId")
				.setParameter("callId", Long.parseLong(callId))
				.list();
	}

	public CustomerOptionData findByCallIdOptionId(String callId, String optionId) {
		return (CustomerOptionData) TLVXManager.getSession()
				.createQuery("from CustomerOptionData cod where cod.call.id = :callId and cod.customerOptionId = :optionId")
				.setParameter("callId", Long.parseLong(callId))
				.setParameter("optionId", optionId)
				.uniqueResult();
	}

	public void save(CustomerOptionData d) {
		TLVXManager.getSession().saveOrUpdate(d);
		TLVXManager.commit();
	}

	public void saveAll(ArrayList<CustomerOptionData> optionData) {
		for(CustomerOptionData d : optionData) save(d);
	}   
}