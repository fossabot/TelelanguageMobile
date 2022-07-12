package com.telelanguage.tlvx.ivr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.telelanguage.api.InterpreterInfo;
import com.telelanguage.tlvx.model.Call;
import com.telelanguage.tlvx.model.CallInformation;
import com.telelanguage.tlvx.model.Customer;
import com.telelanguage.tlvx.model.CustomerDNIS;
import com.telelanguage.tlvx.model.Interpreter;
import com.telelanguage.tlvx.model.InterpreterLine;
import com.telelanguage.tlvx.model.Language;
import com.telelanguage.tlvx.service.TLVXManager;
import com.telelanguage.videoapi.BlueStreamRequest;
import com.telelanguage.videoapi.InterpreterRequest;

/**
 * CallSessionManager
 */
public class CallSessionManager
{
    private static final Logger LOG = Logger.getLogger(CallSessionManager.class);

    public static final int CALL_STATUS_OUTBOUND_READY = 0;
    public static final int CALL_STATUS_QUEUED = 1;
    public static final int CALL_STATUS_FORWARDED_TO_AGENT = 2;
    public static final int CALL_STATUS_AGENT_ANSWERED = 3;
    public static final int CALL_STATUS_FINISHED = 4;
    public static final int CALL_STATUS_IVR = 5;
    
    public Map<String, CallSession> sessions = new ConcurrentHashMap<String, CallSession>();
    private Map<String, OutboundCallSession> outboundSessions = new ConcurrentHashMap<String, OutboundCallSession>();
    private Map<String, InterpreterRequest> outboundRequests = new ConcurrentHashMap<String, InterpreterRequest>();
    
    public CallSession createSession(Map parameters)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("createSession called " + TLVXManager.agentManager+ " with parameters = "+parameters);
    	
    	String sessionId = (String) parameters.get("sessionid");
    	String connectionId = (String) parameters.get("connectionid");
    	String destination = (String) parameters.get("connectionLocal");
    	String origination = (String) parameters.get("connectionRemote");
    	String ccxmlServer = (String) parameters.get("remoteAddress");
    	String sipCallId = (String) parameters.get("sipCallId");
    	
    	Connection caller = new Connection(connectionId, destination, origination);
    	
    	CallSession callSession = new CallSession(ccxmlServer, sessionId, this, caller, (String) parameters.get("connectionFrom"), sipCallId);
    	
    	sessions.put(callSession.getId(), callSession);
        callSession.startSession(null);
        return callSession;
    }
    
    public void onConnectionProgressing(Map request)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("onSignalingProgressing called");
    	
    	String sessionId = (String) request.get("sessionid");
    	
    	CallSession session = sessions.get(sessionId);
        
        if (null != session)
        {
            session.onConnectionProgressing(request);
            return;
        }
        
        OutboundCallSession outboundSession = outboundSessions.get(sessionId);
        
        if (null != outboundSession)
        {
            outboundSession.onConnectionProgressing(request);
            return;
        }
    }
    
    public void onConnectionConnected(Map request)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("onSignalingConnected called");
    	
    	String sessionId = (String) request.get("sessionid");
    	
    	CallSession session = sessions.get(sessionId);
        
        if (null != session)
        {
            session.onConnectionConnected(request);
            return;
        }

        OutboundCallSession outboundSession = outboundSessions.get(sessionId);
        
        if (null != outboundSession)
        {
            outboundSession.onConnectionConnected(request);
            return;
        }
    }

    public void onConnectionDisconnected(Map request)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("onSignalingDisconnected called");
    	
    	String sessionId = (String) request.get("sessionid");
    	
    	CallSession session = sessions.get(sessionId);
        
        if (null != session)
        {
        	synchronized(session) {
	            session.onConnectionDisconnected(request);
	            removeIfDestroyedAndAllCallsDisconnected(session);
        	}
            return;
        }

        OutboundCallSession outboundSession = outboundSessions.get(sessionId);
        
        if (null != outboundSession)
        {
            outboundSession.onConnectionDisconnected(request);
            removeIfDestroyedAndAllCallsDisconnected(session);
            return;
        }
    }
    
    public void onConnectionFailed(Map request)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("onConnectionFailed called");
    	
    	String sessionId = (String) request.get("sessionid");
    	
    	CallSession session = sessions.get(sessionId);
        
        if (null != session)
        {
            session.onConnectionFailed(request);
            return;
        }
        
        OutboundCallSession outboundSession = outboundSessions.get(sessionId);
        
        if (null != outboundSession)
        {
            outboundSession.onConnectionFailed(request);
            return;
        }
    }
    
    public void onConnectionWrongState(Map request)
    {
        if (LOG.isDebugEnabled()) LOG.debug("onConnectionWrongState called");
        
        String sessionId = (String) request.get("sessionid");
        
        CallSession session = sessions.get(sessionId);
        
        if (null != session)
        {
            session.onConnectionWrongState(request);
            return;
        }
        
        OutboundCallSession outboundSession = outboundSessions.get(sessionId);
        
        if (null != outboundSession)
        {
            outboundSession.onConnectionWrongState(request);
            return;
        }
    }    
    
	public void onErrorSemantic(Map<String, String> request) {
        if (LOG.isDebugEnabled()) LOG.debug("onErrorSemantic called");
        String sessionId = request.get("sessionid");
        CallSession session = sessions.get(sessionId);
        if (null != session)
        {
            session.onErrorSemantic(request);
            return;
        }
	}
    
    public void onConferenceCreated(Map request)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("onSignalingConferenceCreated called");
    	
    	String sessionId = (String) request.get("sessionid");
    	
    	CallSession session = sessions.get(sessionId);
        
        if (null != session)
        {
            session.onConferenceCreated(request);
            return;
        }
        
        OutboundCallSession outboundSession = outboundSessions.get(sessionId);
        
        if (null != outboundSession)
        {
            outboundSession.onConferenceCreated(request);
            return;
        }
    }

    public void onConferenceDestroyed(Map request)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("onSignalingConferenceDestroyed called");
    	
    	String sessionId = (String) request.get("sessionid");
    	
    	CallSession session = sessions.get(sessionId);
        
        if (null != session)
        {
            session.onConferenceDestroyed(request);
            return;
        }
        
        OutboundCallSession outboundSession = outboundSessions.get(sessionId);
        
        if (null != outboundSession)
        {
            outboundSession.onConferenceDestroyed(request);
            return;
        }
    }

    public void onConferenceUnjoined(Map request)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("onSignalingConferenceUnjoined called");
    	
    	String sessionId = (String) request.get("sessionid");
    	
    	CallSession session = sessions.get(sessionId);
        
        if (null != session)
        {
            session.onConferenceUnjoined(request);
            return;
        }
        
        OutboundCallSession outboundSession = outboundSessions.get(sessionId);
        
        if (null != outboundSession)
        {
            outboundSession.onConferenceUnjoined(request);
            return;
        }        
    }

    public void onConferenceJoined(Map request)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("onSignalingConferenceJoined called");

    	String sessionId = (String) request.get("sessionid");
    	
    	CallSession session = sessions.get(sessionId);
        
        if (null != session)
        {
            session.onConferenceJoined(request);
            return;
        }
                
        OutboundCallSession outboundSession = outboundSessions.get(sessionId);
        
        if (null != outboundSession) outboundSession.onConferenceJoined(request);
    }
    
    public void onDialogStarted(Map request)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("onSignalingDialogStarted called");
    	String sessionId = (String) request.get("sessionid");
    	CallSession session = sessions.get(sessionId);
        if (null != session)  session.onDialogStarted(request);
    }

    public void onDialogExit(Map request)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("onSignalingDialogExit called");
    	String sessionId = (String) request.get("sessionid");
    	CallSession session = sessions.get(sessionId);
        if (null != session) session.onDialogExit(request);
    }
    
	public void onErrorDialogNotStarted(Map<String, String> request) {
        if (LOG.isDebugEnabled()) LOG.debug("onErrorDialogNotStarted called");
        String sessionId = (String) request.get("sessionid");
    	CallSession session = sessions.get(sessionId);
        if (null != session) session.onErrorDialogNotStarted(request);
	}
    
    public void onSessionDestroyed(Map<String, String> request)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("onSessionDestroyed called");

    	String sessionId = (String) request.get("sessionid");
    	
    	CallSession session = sessions.get(sessionId);
        
        if (null != session)
        {
        	synchronized(session) {
	            session.onSessionDestroyed(request);
	            removeIfDestroyedAndAllCallsDisconnected(session);
        	}
        }
    }
    
    private void removeIfDestroyedAndAllCallsDisconnected(CallSession session) {
    	if (session.isDestroyedAndAllCallsDisconnected()) {
    		LOG.info("removing session "+session);
    		sessions.remove(session.getId());
    	}
    }
    
    public void onCallSessionAgentSet(Long agentId, String callSessionId)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("onCallSessionAgentSet called with agentId = "+agentId+" callSessionid = "+callSessionId);
    	
    	CallSession session = sessions.get(callSessionId);
        
        if (null != session)
        {
            session.setAgent(agentId);
            return;
        }
    }

    public void onCallSessionAgentReject(Map request)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("onCallSessionAgentReject called with request = "+request);

    	String sessionId = (String) request.get("callSessionId");
    	
    	CallSession session = sessions.get(sessionId);
        
        if (null != session)
        {
            session.agentRejected();
            return;
        }
    }
    
    public void onCallSessionAgentRequeue(Call call)
    {
        if (LOG.isDebugEnabled()) LOG.debug("onCallSessionAgentRequeue called with request = "+call);
        CallSession session = sessions.get(call.getCallSessionId());
        if (null != session) session.requeueCall(call);
    }
    
    public void onCallSessionCustomerSet(Map request)
    {
        if (LOG.isDebugEnabled()) LOG.debug("onCallSessionCustomerSet called with request = "+request);

        String sessionId = (String)request.get("callSessionId");
        String customerId = (String)request.get("customerId");
        
        CallSession session = sessions.get(sessionId);
        if (null != session) session.setCustomer(customerId);
    }    

    public CallInformation onCallSessionAccessCode(String callId, String code)
    {
        if (LOG.isDebugEnabled()) LOG.debug("onCallSessionAccessCode called with code = "+code);
    	Call call = TLVXManager.callDAO.findById(Long.parseLong(callId));
    	if (call != null) {
	    	CallSession session = sessions.get(call.getCallSessionId());
	    	if (session != null) {
	        return session.setAccessCode(code);
	    	} else {
	    		LOG.warn("onCallSessionAccessCode session is null for sessionId: "+call.getCallSessionId());
	    		return null;
	    	}
    	} else {
    		LOG.warn("onCallSessionAccessCode call is null for callid: "+callId);
    		return null;
    	}
    }

    public Boolean onCallSessionCompleteCall(String callId, Boolean hangupCaller, String language)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("onCallSessionCompleteCall called with callSessionId = "+callId+" hangupCaller = "+hangupCaller);
    	Call call = TLVXManager.callDAO.findById(Long.parseLong(callId));
    	CallSession session = sessions.get(call.getCallSessionId());
        if (null != session) return session.completeCall(hangupCaller, call, language);
        return false;
    }

    public void onCallSessionTransferCall(String callId, String dest)
    {
        if (LOG.isDebugEnabled()) LOG.debug("onCallSessionTransferCall called with request = "+dest);
    	Call call = TLVXManager.callDAO.findById(Long.parseLong(callId));
    	CallSession session = sessions.get(call.getCallSessionId());
        if (null != session) session.transferCall(dest);    
    }

    public void onCallSessionCallerHoldOn(String callId)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("onCallSessionCallerHoldOn called with request = "+callId);
    	Call call = TLVXManager.callDAO.findById(Long.parseLong(callId));
    	CallSession session = sessions.get(call.getCallSessionId());
        if (null != session) session.callerOnHold(false);
    }
    
    public void onCallSessionCallerHoldOff(String callId)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("onCallSessionCallerHoldOn called with request = "+callId);
    	Call call = TLVXManager.callDAO.findById(Long.parseLong(callId));
    	CallSession session = sessions.get(call.getCallSessionId());
        if (null != session) session.callerOffHold();
    }

    public void onCallSessionCustomerConnect(String sessionId, String dest)
    {
        if (LOG.isDebugEnabled()) LOG.debug("onCallSessionCustomerConnect sessionId = "+sessionId);
        CallSession session = sessions.get(sessionId);
        //if (null != session) session.connectCustomer(dest);
        //else 
        	LOG.warn("onCallSessionCustomerConnect sessionId = "+sessionId+ " session not found.");
    }

    public void onCallSessionInterpreterConnect(String sessionId, String interpreterId, String language, String interpreterGender, boolean manual, Boolean interpreterVideo)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("onCallSessionInterpreterConnect called with sessionId = "+sessionId);
    	CallSession session = sessions.get(sessionId);
        if (null != session) session.connectInterpreter(interpreterId, language, interpreterGender, manual, interpreterVideo);
        else {
          LOG.warn("onCallSessionInterpreterConnect sessionId = " + sessionId + " session not found.");
          Interpreter interpreter = TLVXManager.interpreterDAO.findInterpreterByID(interpreterId);
          TLVXManager.interpreterDAO.markInterpreterOffCall(interpreter);
        } 
    }

    public void onCallSessionInterpreterDisconnect(String callId)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("onCallSessionInterpreterDisconnect called with request = "+callId);	
    	Call call = TLVXManager.callDAO.findById(Long.parseLong(callId));
    	CallSession session = sessions.get(call.getCallSessionId());
        if (null != session) session.disconnectInterpreter("clicked hang up interpreter");
    }

    public void onCallSessionInterpreterHoldOn(String callId)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("onCallSessionInterpreterHoldOn called with request = "+callId);
    	Call call = TLVXManager.callDAO.findById(Long.parseLong(callId));
    	CallSession session = sessions.get(call.getCallSessionId());
        if (null != session) session.interpreterOnHold(false);
    }

    public void onCallSessionInterpreterHoldOff(String callId) 
    {
    	if (LOG.isDebugEnabled()) LOG.debug("onCallSessionInterpreterHoldOff called with request = "+callId);
    	Call call = TLVXManager.callDAO.findById(Long.parseLong(callId));
    	CallSession session = sessions.get(call.getCallSessionId());
        if (null != session) session.interpreterOffHold();
    }

    public void onCallSessionMarkInterpreter(Map request)
    {
        if (LOG.isDebugEnabled()) LOG.debug("onCallSessionMarkInterpreter called with request = "+request);

        String sessionId = (String) request.get("callSessionId");
        
        CallSession session = sessions.get(sessionId);
        
        if (null != session) session.markAsInterpreter();
    }    

//    @MessageHandler(request = TL_CALLSESSION_INTERPRETER_LIST, authenticationRequired = false)
    public void onCallSessionInterpreterList(Map request)
    {
        if (LOG.isDebugEnabled()) LOG.debug("onCallSessionInterpreterList called with request = "+request);

        String sessionId = (String) request.get("callSessionId");
        
        CallSession session = sessions.get(sessionId);
        
        if (null != session)
        {
        	String language = (String) request.get("language");
            session.setPreviousLanguage(language);
            return;
        }
    }

    public void onCallSessionThirdpartyConnect(String callId, String phonenumber)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("onCallSessionThirdpartyConnect called request = "+phonenumber);
    	Call call = TLVXManager.callDAO.findById(Long.parseLong(callId));
    	CallSession session = sessions.get(call.getCallSessionId());
        if (null != session) session.connectThirdParty(phonenumber);
    }

    public void onCallSessionThirdpartyDisconnect(String connectionId, String callId)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("onCallSessionThirdpartyDisconnect called with request = "+connectionId+", "+callId);
    	Call call = TLVXManager.callDAO.findById(Long.parseLong(callId));
    	CallSession session = sessions.get(call.getCallSessionId());
        if (null != session) session.thirdPartyDisconnect(connectionId, false);
    }

    public void onCallSessionThirdpartyHoldOn(String connectionId, String callId)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("onCallSessionThirdpartyHoldOn called with request = "+callId);
    	Call call = TLVXManager.callDAO.findById(Long.parseLong(callId));
    	CallSession session = sessions.get(call.getCallSessionId());
        if (null != session) session.thirdPartyOnHold(connectionId);
    }

    public void onCallSessionThirdpartyHoldOff(String connectionId, String callId)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("onCallSessionThirdpartyHoldOff called with request = "+callId);
    	Call call = TLVXManager.callDAO.findById(Long.parseLong(callId));
    	CallSession session = sessions.get(call.getCallSessionId());
        if (null != session) session.thirdPartyOffHold(connectionId);
    }
    
	public List<InterpreterLine> getInterpreterHistory(String callId, String language) {
    	if (LOG.isDebugEnabled()) LOG.debug("getInterpreterHistory called with request = "+callId);
    	Call call = TLVXManager.callDAO.findById(Long.parseLong(callId));
    	if (call != null) {
	    	CallSession session = sessions.get(call.getCallSessionId());
	    	if (session != null) {
		    	session.setPreviousLanguage(language);
				return session.getInterpreterHistory();
	    	} else {
	    		LOG.warn("getInterpreterHistory has null session for sessionId: "+call.getCallSessionId());
	    		return new ArrayList<InterpreterLine>();
	    	}
    	} else {
    		LOG.warn("getInterpreterHistory has null call for callId: "+callId);
    		return new ArrayList<InterpreterLine>();
    	}
	}

	public List<InterpreterLine> getInterpreters(String callId, String language, String interpreterGender, Boolean video) {
    	if (LOG.isDebugEnabled()) LOG.debug("getInterpreters called with request = "+callId);
    	Call call = TLVXManager.callDAO.findById(Long.parseLong(callId));
    	CallSession session = sessions.get(call.getCallSessionId());
    	if (session != null) {
	    	session.setPreviousLanguage(language);
	    	session.setPreviousInterpreterGender(interpreterGender);
	    	session.setPreviousVideo(video);
			return session.getInterpretersAvailable();
    	} else {
    		return new ArrayList<InterpreterLine>();
    	}
	}

    public void endCallSession(CallSession callSession, Call call)
    {
        if (LOG.isDebugEnabled()) LOG.debug("endCallSession called with call = " + call);
        
        call = TLVXManager.callDAO.findById(call.getId());
        call.setEndDate(new Date());
        call.setAgent(null);
        call.setReservedAgent(null);
        call.setStatus(CallSessionManager.CALL_STATUS_FINISHED);
        call = TLVXManager.callDAO.save(call);
        
        TLVXManager.agentManager.sendQueueStatusUpdates();
    }
    
    public void endOutboundCallSession(OutboundCallSession outboundSession)
    {
        if (LOG.isDebugEnabled()) LOG.debug("endOutboundCallSession called with outboundSession = " + outboundSession);
        
        outboundSessions.remove(outboundSession.getId());
    }    
    
    public CustomerDNIS findCustomerDNIS(Connection connection)
    {
        CustomerDNIS customerDNIS = null;
        try
        {
            customerDNIS = TLVXManager.customerDNISDAO.executeCustomerDNISSCCustomerIdGetAST(connection.getDestination(), connection.getOrigination());
            if (LOG.isDebugEnabled()) LOG.debug("returning customerdnis " + customerDNIS);
        }
        catch (Throwable t)
        {
            LOG.warn("customerDNISDAO threw exception ", t);
            // the stored procedure is returning exception when the record does not exist,
            // probably because it does not return a result set of 0
        }
        return customerDNIS;
    }

	public Customer findCustomer(CustomerDNIS customerDNIS, Connection connection)
	{
        if (null != customerDNIS)
        {
            String customerId = customerDNIS.getCustomerId();
            Customer customer = TLVXManager.customerDAO.executeCustomerGet(customerId);
            customer.setCustomerDNIS(customerDNIS);
            return customer;
        }
        else
        {
            if (LOG.isDebugEnabled()) LOG.debug("unable to locate customer dnis for " + connection);
            return null;
        }
	}
	
	public String findCustomerLanguage(CustomerDNIS customerDNIS)
	{
        if (customerDNIS != null)
        {
            Language result = TLVXManager.languageDAO.findLanguageById(customerDNIS.getLanguageId());
            
            if (result != null)
            {
                return result.getLanguageName();
            }
        }
        
        return null;
	}
	
	public String findLanguageId(String language)
	{
		Language result = TLVXManager.languageDAO.findByName(language);
		
		if (result != null)
		{
			return result.getPkId();
		}
		else
		{
			return null;
		}
	}

	public String getInterpreterDialogURI() 
	{
		return TLVXManager.getProperties().getProperty("interpreterDialogURI");
	}
	
	public String getHoldDialogURI() 
	{
		return TLVXManager.getProperties().getProperty("holdDialogURI");
	}

	public String getInitialDialogURI() 
	{
		return TLVXManager.getProperties().getProperty("initialDialogURI");
	}

	public String getCallbackAgentDialogURI()
	{
		return TLVXManager.getProperties().getProperty("callbackAgentDialogURI");
	}

    public String getCustomerCompleteCallHoldURI()
    {
        return TLVXManager.getProperties().getProperty("customerCompleteCallHoldURI");
    }

    public String getInterpreterHoldDialogURI()
    {
        return TLVXManager.getProperties().getProperty("interpreterHoldDialogURI");
    }

    public String getProxyAddress()
    {
        return (String) TLVXManager.getProperties().getProperty("proxyAddress");
    }
    
    public String getPstnOutboundAddress()
    {
        return (String) TLVXManager.getProperties().getProperty("pstnOutboundAddress");
    }

    public String getPstnOutboundPrefix()
    {
      String prefix = (String) TLVXManager.getProperties().getProperty("pstnOutboundPrefix");
      LOG.debug("prefix = "+prefix);
      if (prefix != null) return prefix;
      return "";
    }

    public String getInterpreterPromptBaseURL()
    {
        return TLVXManager.getProperties().getProperty("interpreterPromptBaseURL");
    }

	public String getTeleAutoURI() 
	{
		return TLVXManager.getProperties().getProperty("teleAutoURI");
	}

    public String getCustomerPromptBaseURL()
    {
        return TLVXManager.getProperties().getProperty("customerPromptBaseURL");
    }
    
    public String getCustomDeptPromptBaseURL(){
    	return TLVXManager.getProperties().getProperty("customDeptPromptBaseURL");
    }

    public String getIvrTransferTarget1()
    {
        return TLVXManager.getProperties().getProperty("ivrTransferTarget1");
    }

	public String getInterpreterTimeout() 
	{
		return TLVXManager.getProperties().getProperty("interpreterTimeout");
	}

	public String getAgentTimeout() 
	{
		return TLVXManager.getProperties().getProperty("agentTimeout");
	}

	public String getThirdPartyTimeout() 
	{
		return TLVXManager.getProperties().getProperty("thirdPartyTimeout");
	}

	public String getCallRecordingApiURL() 
	{
		return TLVXManager.getProperties().getProperty("callRecordingApiURL");
	}

	public CallInformation getCallInformationByCallId(String callId) {
		Call call = TLVXManager.callDAO.findById(Long.parseLong(callId));
		if (call != null) {
			CallSession session = sessions.get(call.getCallSessionId());
			if (session != null) {
				return session.loadCallInformation();
			}
		}
		LOG.error("no call information found for callid = "+callId);
		return null;
	}

	public void saveCallInformation(CallInformation callInformation) {
    	Call call = TLVXManager.callDAO.findById(Long.parseLong(callInformation.callId));
    	if (call != null) {
	    	CallSession session = sessions.get(call.getCallSessionId());
	    	if (session != null) {
	    		session.saveCallInformation(callInformation);
	    	} else {
	    		LOG.warn("saveCallInformation has null session from sessionId: "+call.getCallSessionId());
	    	}
    	} else {
    		LOG.warn("saveCallInformation has null call: "+callInformation);
    	}
	}

	public boolean verifyCustomerOnCall(Call call) {
		if (call.getStatus() == CallSessionManager.CALL_STATUS_FINISHED) return false;
		CallSession session = sessions.get(call.getCallSessionId());
		if (session == null) return false;
		return session.verifyCustomerOnCall();
	}

	public void onWebInterpreterAcceptCall(InterpreterInfo interpreter) {
		Call call = TLVXManager.callDAO.findById(interpreter.callId);
		if (call == null) return;
		CallSession session = sessions.get(call.getCallSessionId());
		if (session == null) return;
		session.acceptInterpreterFromWeb(interpreter);
	}
	
	public void onCustomerClickedHangup(String sessionId) {
		LOG.debug("onCustomerClickedHangup with callSessionId = " + sessionId);
		CallSession session = sessions.get(sessionId);
		if (session == null) return;
		session.onCustomerClickedHangup();
	}

	public void onWebInterpreterRejectCall(InterpreterInfo interpreter) {
		Call call = TLVXManager.callDAO.findById(interpreter.callId);
		if (call == null) return;
		CallSession session = sessions.get(call.getCallSessionId());
		if (session == null) return;
		session.rejectInterpreterFromWeb(interpreter);
	}

	public void onWebInterpreterRequestAgent(InterpreterInfo interpreter) {
		Call call = TLVXManager.callDAO.findById(interpreter.callId);
		if (call == null) return;
		CallSession session = sessions.get(call.getCallSessionId());
		if (session == null) return;
		session.interpreterWebAgentOutdialRequest(interpreter);
	}
	
	public void onWebInterpreterRequestPlayCustomerVideo(InterpreterInfo interpreter) {
		Call call = TLVXManager.callDAO.findById(interpreter.callId);
		if (call == null) return;
		CallSession session = sessions.get(call.getCallSessionId());
		if (session == null) return;
		session.interpreterWebPlayCustomerVideoRequest(interpreter);
	}

	public void onWebInterpreterRequestPauseCustomerVideo(InterpreterInfo interpreter) {
		Call call = TLVXManager.callDAO.findById(interpreter.callId);
		if (call == null) return;
		CallSession session = sessions.get(call.getCallSessionId());
		if (session == null) return;
		session.interpreterWebPauseCustomerVideoRequest(interpreter);
	}
	
	public void onWebInterpreterVideoSessionStarted(InterpreterInfo interpreter) {
		Call call = TLVXManager.callDAO.findById(interpreter.callId);
		if (call == null) return;
		CallSession session = sessions.get(call.getCallSessionId());
		if (session == null) return;
		session.interpreterVideoSessionStarted(interpreter);
	}
	
	public void onCustomerVideoSessionStarted(String callSessionId) {
		CallSession session = sessions.get(callSessionId);
		if (session == null) return;
		session.customerVideoSessionStarted();
	}

	public void ccxmlLoaded(Map<String, String> map) {
		LOG.info("ccxmlLoaded: "+map);
		if (map.containsKey("tempsessionid") && !"null".equals(map.get("tempsessionid"))) {
			String tempsessionid = map.get("tempsessionid");
			if (outboundRequests.containsKey(tempsessionid)) {
				InterpreterRequest request = outboundRequests.get(tempsessionid);
				outboundRequests.remove(tempsessionid);
		    	String sessionId = (String) map.get("sessionid");
		    	String ccxmlServer = (String) map.get("remoteAddress");
				CallSession callSession = new CallSession(ccxmlServer, sessionId, this, null, request.videoCustomerInfo.webPhoneSipAddress, null);
		    	sessions.put(callSession.getId(), callSession);
//		    	if (request.accessCode != null) {
//			    	Customer customer = TLVXManager.customerDAO.findByCode(request.accessCode);
//			    	if (customer != null) {
//				    	callSession.setCustomer(customer.getCustomerId());
//			    	}
//		    	}
		        callSession.startSession(null);
				String sessionid = map.get("sessionid");
				sessions.put(sessionid, callSession);
				callSession.connectCustomer(request);
			}
		}
	}

	public void addRequest(String tempSessionId, InterpreterRequest request) {
		outboundRequests.put(tempSessionId, request);
	}

	public void onBluestreamInitiateCall(Call call, BlueStreamRequest request) {
		LOG.info("onBluestreamInitiateCall: " + call + " " + request);
		if (call.getStatus() == CallSessionManager.CALL_STATUS_FINISHED) return;
		CallSession session = sessions.get(call.getCallSessionId());
		if (session == null) return;
		session.bluestreamInitiateCall(call, request);
	}

	public void onCustomerBluestreamConnected(String callSessionId, String bsCallId, String bsInterpreterId, String bsInterpreterName) {
		LOG.info("onCustomerBluestreamConnected: " + callSessionId + " " + bsCallId + " " + bsInterpreterId + " " + bsInterpreterName);
		CallSession session = sessions.get(callSessionId);
		if (session == null) return;
		session.blueStreamConnected(bsCallId, bsInterpreterId, bsInterpreterName);
	}
}
