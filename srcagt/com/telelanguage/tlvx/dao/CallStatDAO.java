package com.telelanguage.tlvx.dao;

import com.telelanguage.tlvx.model.CallStat;
import com.telelanguage.tlvx.service.TLVXManager;

/**
 * CallStatsDAO
 */
public class CallStatDAO
{
	public void save(CallStat stat) {
		TLVXManager.getSession().saveOrUpdate(stat);
		TLVXManager.commit();
	}
}
