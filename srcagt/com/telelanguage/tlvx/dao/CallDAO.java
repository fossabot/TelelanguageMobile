package com.telelanguage.tlvx.dao;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.log4j.Logger;

import com.telelanguage.tlvx.model.Agent;
import com.telelanguage.tlvx.model.Call;
import com.telelanguage.tlvx.model.InterpreterActivity;
import com.telelanguage.tlvx.model.ThirdPartyActivity;
import com.telelanguage.tlvx.service.TLVXManager;

/**
 * CallSession
 */
public class CallDAO {
	private static final Logger LOG = Logger.getLogger(CallDAO.class);
	private static String ipAddress = null;
	
	static {
		try {
			ipAddress = InetAddress.getLocalHost().getHostAddress();
			System.out.println(ipAddress);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public Call save(Call arg0) {
		arg0.setAppServer(ipAddress);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Saving call " + arg0 + " Status: "+arg0.getStatus()+" language "+arg0.getLanguage());
		}
		TLVXManager.getSession().saveOrUpdate(arg0);
		TLVXManager.commit();
		return arg0;
	}

	public Call findCallByCallSessionId(String callSessionId) {
		return (Call) TLVXManager.getSession()
        		.createQuery("FROM Call c WHERE c.callSessionId = :callSessionId")
        		.setParameter("callSessionId", callSessionId)
				.uniqueResult();
	}

	public Call findByAgent(Agent agent) {
		return (Call) TLVXManager.getSession()
        		.createQuery("FROM Call c WHERE c.agent = :agent")
        		.setParameter("agent", agent)
        		.setMaxResults(1)
        		.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public List<Call> findNextCallToAnswer() {
		List<Call> calls = TLVXManager.getSession()
        		.createQuery("FROM Call c WHERE c.status = 1 and c.appServer = :appServer ORDER BY priorityCall DESC, startDate ASC")
        		.setParameter("appServer", ipAddress)
        		.setMaxResults(1)
        		.list();
		return calls;
	}
	
	@SuppressWarnings("unchecked")
	public Call findNextCallNotPersonalHoldToAnswer() {
		List<Call> calls = TLVXManager.getSession()
        		.createQuery("FROM Call c WHERE c.status = 1 and c.appServer = :appServer and c.reservedAgent is null ORDER BY priorityCall DESC, startDate ASC")
        		.setParameter("appServer", ipAddress)
        		.setMaxResults(1)
        		.list();
		if (calls.size()>0) return calls.get(0);
		return null;
	}

	@SuppressWarnings("unchecked")
	public Call findNextCallToAnswer(Call ignoreCall) {
		List<Call> calls = TLVXManager.getSession()
        		.createQuery("FROM Call c WHERE c.status = 1 AND c.id != :ignoreCallId and c.appServer = :appServer ORDER BY priorityCall DESC, startDate ASC")
        		.setParameter("appServer", ipAddress)
        		.setParameter("ignoreCallId", ignoreCall.getId())
        		.list();
		if (calls.size()>0) return calls.get(0);
		return null;
	}

	@SuppressWarnings("unchecked")
	public Call findNextCallToAnswerNotReserved() {
		List<Call> calls = TLVXManager.getSession()
        		.createQuery("FROM Call c WHERE c.status = 1 AND c.reservedAgent IS NULL and c.appServer = :appServer ORDER BY priorityCall DESC, startDate ASC")
        		.setParameter("appServer", ipAddress)
        		.list();
		if (calls.size()>0) return calls.get(0);
		return null;
	}

	public int findTotalCallsOngoing() {
		BigInteger bigInt = (BigInteger) TLVXManager.getSession()
				.createSQLQuery("SELECT count(*) FROM call_t c WHERE c.endDate IS NULL")
				.uniqueResult();
		return bigInt.intValue();
	}

	public int findCallsInQueue() {
		BigInteger bigInt = (BigInteger) TLVXManager.getSession()
				.createSQLQuery("SELECT count(*) FROM call_t c WHERE c.status = 1")
				.uniqueResult();
		return bigInt.intValue();
	}

	public Call findById(Long id) {
		Call call = (Call) TLVXManager.getSession().get(Call.class, id);
		//Thread.currentThread().setName("CallId: "+call.getId());
		return call;
	}

	public void save(InterpreterActivity interpreterActivity) {
		TLVXManager.getSession().saveOrUpdate(interpreterActivity);
		TLVXManager.commit();
	}

	public void save(ThirdPartyActivity thirdPartyActivity) {
		TLVXManager.getSession().saveOrUpdate(thirdPartyActivity);
		TLVXManager.commit();
	}
}