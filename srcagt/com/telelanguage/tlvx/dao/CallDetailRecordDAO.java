package com.telelanguage.tlvx.dao;

import com.telelanguage.tlvx.model.Call;
import com.telelanguage.tlvx.model.CallDetailRecord;
import com.telelanguage.tlvx.service.TLVXManager;

/**
 * CallDetailRecordDAO
 */
public class CallDetailRecordDAO
{
	public CallDetailRecord save(CallDetailRecord cdr) {
		TLVXManager.getSession().saveOrUpdate(cdr);
		TLVXManager.commit();
		return cdr;
	}

	public CallDetailRecord getCDRByCallId(Call call) {
		return (CallDetailRecord) TLVXManager.getSession()
				.createQuery("from CallDetailRecord " +
				             "where call = :call ")
				.setParameter("call", call)
				.uniqueResult();
	}
}
