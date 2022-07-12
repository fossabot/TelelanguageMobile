package com.telelanguage.tlvx.dao;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;

import com.telelanguage.tlvx.model.Call;
import com.telelanguage.tlvx.model.Interpreter;
import com.telelanguage.tlvx.model.InterpreterMissedCall;
import com.telelanguage.tlvx.service.InterpreterManager;
import com.telelanguage.tlvx.service.TLVXManager;

/**
 * InterpreterDAO
 */
public class InterpreterDAO
{
	private static final Logger LOG = Logger.getLogger(InterpreterDAO.class);
	
    @SuppressWarnings("unchecked")
    public List<Object[]> findActiveByLanguageSearch(String subscriptionCode, String language, String interpreterGender, Boolean interpreterVideo, boolean ignorePriority, int maxResults)
    {
    	LOG.info("findActiveByLanguageSearch:  subscriptionCode = "+subscriptionCode+" language = "+language+" ignorePriority = "+ignorePriority);
    	String hql = "from Interpreter i, Language l, InterpreterLanguageList il where l.deleted = false AND i.deleted = false AND i.active = true AND i.activeSession = true AND i.onCall = false AND i.loginLocked = false AND i.subscriptionCode = :subscriptionCode AND l.pkId = il.languageId AND i.interpreterId = il.interpreterId AND l.languageName = :language ";
    	if (interpreterGender != null && interpreterGender.length() == 1) {
    		hql+=" and i.gender = :interpreterGender ";
    	}
    	if (interpreterVideo != null && interpreterVideo) {
    		hql+=" and i.video = true and i.onWebSite is not null ";
    	} else {
    		hql+=" and (i.videoOnly = false or i.video = false or i.onWebSite is null ) ";
    	}
    	if (ignorePriority) {
    		hql += "order by i.activeSession asc, i.lastCall asc";
    	} else {
    		hql += "order by i.activeSession asc, i.priorityCode desc, i.lastCall asc";
    	}
        Query query = TLVXManager.getSession()
        		.createQuery(hql)
        		.setParameter("subscriptionCode", subscriptionCode)
        		.setParameter("language", language);
    	if (interpreterGender != null && interpreterGender.length() == 1) {
    		query.setParameter("interpreterGender", interpreterGender);
    	}
    	query.setMaxResults(maxResults);
        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> findAllByLanguageSearch(String subscriptionCode, String language, String interpreterGender, Boolean interpreterVideo)
    {
    	LOG.info("findAllByLanguageSearch:  subscriptionCode = "+subscriptionCode+" language = "+language+" interpreterGender = "+interpreterGender+", interpreterVideo = "+interpreterVideo);
    	String hql = "from Interpreter i, Language l, InterpreterLanguageList il where l.deleted = false AND i.deleted = false AND i.subscriptionCode = :subscriptionCode AND l.pkId = il.languageId AND i.interpreterId = il.interpreterId AND l.languageName = :language AND i.active = true and i.onCall = false";
    	if (interpreterGender != null && interpreterGender.length() == 1) {
    		hql+=" and i.gender = :interpreterGender ";
    	}
    	if (interpreterVideo != null && interpreterVideo) {
    		hql+=" and i.video = true ";
    	} else {
    		hql+=" and (i.videoOnly = false or i.video = false) ";
    	}
    	hql+= " order by i.activeSession desc, i.lastCall desc, i.priorityCode desc, i.firstName asc";
    	//hql+= " order by i.lastName, i.firstName";
    	LOG.info("hql = "+hql);
        Query query = TLVXManager.getSession()
        		.createQuery(hql)
        		.setParameter("subscriptionCode", subscriptionCode)
        		.setParameter("language", language);
    	if (interpreterGender != null && interpreterGender.length() == 1) {
    		query.setParameter("interpreterGender", interpreterGender);
    	}
        return query.list();
    }
    
    public Interpreter findInterpreterByID(String id)
    {
    	return (Interpreter)TLVXManager.getSession().get(Interpreter.class, id);
    }
    
    public boolean markInterpreterOnCall(final Interpreter interpreter, Call call)
    {
    	try {
	        int updates = TLVXManager.getSession()
	        	.createSQLQuery("UPDATE interpreters SET On_Call = 1, lastCall = :date, callId = :call WHERE ID = :interpreterId and On_Call = 0 ")
	        	.setParameter("date", new Date())
	        	.setParameter("call", call)
	            .setParameter("interpreterId", interpreter.getInterpreterId())
	            .executeUpdate();
	        TLVXManager.getSession().refresh(interpreter);
	        LOG.debug("markInterpreterOnCall ("+interpreter.getInterpreterId()+") : "+updates);
	        TLVXManager.commit();
	        return updates > 0;
		} catch (Exception e) {
			LOG.error("Unable to mark interpreter on call", e);
			return false;
		}
    }
    
    public void markInterpreterOffCall(final Interpreter interpreter)
    {
    	try {
	    	TLVXManager.getSession()
	    		.createSQLQuery("UPDATE interpreters SET On_Call = 0, callId = null WHERE ID = :interpreterId")
	            .setParameter("interpreterId", interpreter.getInterpreterId())
	            .executeUpdate();
	    	TLVXManager.commit();
	    	TLVXManager.getSession().refresh(interpreter);
    	} catch (Exception e) {
    		LOG.error("Unable to mark interpreter off call", e);
    	}
    }    
    
    public void markInterpreterOffCall(final Interpreter interpreter, long callId)
    {
    	try {
	    	TLVXManager.getSession()
	    		.createSQLQuery("UPDATE interpreters SET On_Call = 0, callId = null WHERE ID = :interpreterId and callId = :callId")
	            .setParameter("interpreterId", interpreter.getInterpreterId())
	            .setParameter("callId", callId)
	            .executeUpdate();
	    	TLVXManager.commit();
	    	TLVXManager.getSession().refresh(interpreter);
    	} catch (Exception e) {
    		LOG.error("Unable to mark interpreter off call", e);
    	}
    }
    
    public boolean interpreterFailed(Interpreter interpreter, boolean manualDial, String reason)
    {
    	boolean loggedOut = false;
    	
    	interpreter = findInterpreterByID(interpreter.getInterpreterId());
    	
    	int numMissedCalls = interpreter.getNumMissedCalls();
    	if (numMissedCalls < 100 && !"clean up session".equals(reason)) numMissedCalls++;
    	interpreter.setNumMissedCalls(numMissedCalls);
    	interpreter.setTotalMissedCalls(interpreter.getTotalMissedCalls()+1);

//    	TLVXManager.getSession()
//			.createSQLQuery("UPDATE interpreters SET Num_Missed_Calls = Num_Missed_Calls + 1, Total_Missed_Calls = Total_Missed_Calls + 1 WHERE ID = :interpreterId")
//            .setParameter("interpreterId", interpreter.getInterpreterId())
//            .executeUpdate();
//        
//        Byte numMissed = (Byte) TLVXManager.getSession()
//    			.createSQLQuery("SELECT Num_Missed_Calls FROM interpreters WHERE ID = :interpreterId")
//                .setParameter("interpreterId", interpreter.getInterpreterId())
//                .uniqueResult();
//        
//        interpreter.setNumMissedCalls(numMissed);
        
//        if (numMissed > 100) {
//        	TLVXManager.getSession()
//			.createSQLQuery("UPDATE interpreters SET Num_Missed_Calls = 10 WHERE ID = :interpreterId")
//            .setParameter("interpreterId", interpreter.getInterpreterId())
//            .executeUpdate();
//        }
        
        if (numMissedCalls>0 && (numMissedCalls % InterpreterManager.maxInterpreterMissedCalls == 0 || numMissedCalls > 9))
        {
        	if (LOG.isDebugEnabled())
    			LOG.debug("interpreterFailed, logging out " + interpreter.getAccessCode()+" "+interpreter.getInterpreterId()
    					+ ", reason = " + reason + ", for " + this);
        	loggedOut = true;
//        	TLVXManager.getSession()
//				.createSQLQuery("UPDATE interpreters SET webPhone = 0, onWebSite = null, webPhoneSipAddress = null, Active_Session = 0, noAnswerLogouts = noAnswerLogouts + 1 WHERE ID = :interpreterId")
//	            .setParameter("interpreterId", interpreter.getInterpreterId())
//	            .executeUpdate();

        	
//        	interpreter.setActiveSession(false);
//        	interpreter.setNoAnswerLogouts(interpreter.getNoAnswerLogouts()+1);
        }
        TLVXManager.commit();
        return loggedOut;
    }

	public void logOutInterpreter(String interpreterId) {
		TLVXManager.getSession()
			.createSQLQuery("UPDATE interpreters SET Active_Session = false WHERE ID = :interpreterId")
	        .setParameter("interpreterId", interpreterId)
	        .executeUpdate();
		TLVXManager.commit();
		
	}
	
	public Interpreter findInterpreterByEmail(String email) {
		return (Interpreter) TLVXManager.getSession()
			.createQuery("from Interpreter where email = :email and active = true")
			.setParameter("email", email)
			.uniqueResult();
	}

	public void setVideo(String email, boolean video, boolean videoOnly) {
		TLVXManager.getSession()
			.createSQLQuery("UPDATE interpreters SET video = :video, videoOnly = :videoOnly WHERE email = :email")
	        .setParameter("video", video)
	        .setParameter("videoOnly", videoOnly)
	        .setParameter("email", email)
	        .executeUpdate();
		TLVXManager.commit();
	}

	public void save(InterpreterMissedCall imc) {
		TLVXManager.getSession().save(imc);
	}
	
	public void save(Interpreter i) {
		TLVXManager.getSession().save(i);
	}

	public Interpreter refreshInterpreter(Interpreter interpreter) {
		TLVXManager.getSession().refresh(interpreter);
		return interpreter;
	}
}
