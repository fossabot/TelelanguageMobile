package com.telelanguage.tlvx.dao;

import com.telelanguage.tlvx.model.Event;
import com.telelanguage.tlvx.service.TLVXManager;

public class EventDAO {
	public void save(Event event) {
		TLVXManager.getSession().saveOrUpdate(event);
		TLVXManager.commit();
	}
}
