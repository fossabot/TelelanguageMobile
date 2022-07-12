package com.telelanguage.tlvx.dao;

import java.util.List;

import com.telelanguage.tlvx.service.TLVXManager;

public class SubscribedClientsDIDDAO
{
    public boolean isDontRecordCall(final String did)
    {
        @SuppressWarnings("unchecked")
		List<Boolean> dontRecordCallList = TLVXManager.getSession()
        		.createSQLQuery("SELECT Dont_Record_Call FROM subscribed_clients_did s WHERE s.DID = '"+did+"'")
        		.list();
        if (dontRecordCallList.size() > 0)
        {
            Object result = dontRecordCallList.get(0);
            if (result != null)
            {
                return (Boolean) result;
            }
        }
        return false;
    }
	
    public boolean isIVREnabled(final String did)
    {
        @SuppressWarnings("unchecked")
		List<Boolean> ivrEnabledList = TLVXManager.getSession()
        		.createSQLQuery("SELECT IVR_Enabled FROM subscribed_clients_did s WHERE s.DID = '"+did+"'")
        		.list();
        if (ivrEnabledList.size() > 0)
        {
            Object result = ivrEnabledList.get(0);
            if (result != null)
            {
                return (Boolean) result;
            }
        }
        return false;
    }
    
    public String getGreetingWave(final String did)
    {
        return (String)TLVXManager.getSession()
        		.createSQLQuery("SELECT greetingWave FROM subscribed_clients_did s WHERE s.DID = '"+did+"'")
        		.uniqueResult();
    }
    
    public String getGreetingSentence(final String did)
    {
        return (String)TLVXManager.getSession()
        		.createSQLQuery("SELECT greetingSentence FROM subscribed_clients_did s WHERE s.DID = '"+did+"'")
        		.uniqueResult();
    }
        
    public String getSubscriptionCode(final String did)
    {
        return (String)TLVXManager.getSession()
        		.createSQLQuery("SELECT Subscription_Code FROM subscribed_clients_did s WHERE s.DID = '"+did+"'")
        		.uniqueResult();
    }
}
