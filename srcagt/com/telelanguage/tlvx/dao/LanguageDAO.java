package com.telelanguage.tlvx.dao;

import java.math.BigInteger;
import java.util.List;

import com.telelanguage.tlvx.model.Language;
import com.telelanguage.tlvx.service.TLVXManager;

/**
 * LanguageDAO
 */
public class LanguageDAO
{
    public Language findByName (String name)
    {
    	return (Language) TLVXManager.getSession()
        		.createQuery("from Language where languageName = :name")
        		.setParameter("name", name)
        		.setMaxResults(1)
        		.uniqueResult();
    }

    public Language findLanguageById (String id)
    {
    	if (id == null) return null;
        return (Language) TLVXManager.getSession().get(Language.class, id);
    }

	@SuppressWarnings("unchecked")
	public List<Language> findLanguages() {
		return TLVXManager.getSession()
        		.createQuery("from Language where deleted < 1 order by languageName")
        		.list();
	}
	
	public Number getLanguageCountForInterpreter(String interpreterId) {
		return (Number) TLVXManager.getSession()
        		.createQuery("select count(*) from InterpreterLanguageList where interpreterId = :interpreterId")
        		.setParameter("interpreterId", interpreterId)
        		.uniqueResult();
	}
}
