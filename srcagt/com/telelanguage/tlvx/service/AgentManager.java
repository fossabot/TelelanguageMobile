package com.telelanguage.tlvx.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.telelanguage.tlvx.ivr.CallSessionManager;
import com.telelanguage.tlvx.model.Agent;
import com.telelanguage.tlvx.model.AgentEventType;
import com.telelanguage.tlvx.model.Call;
import com.telelanguage.tlvx.model.CallStat;
import com.telelanguage.tlvx.model.Event;
import com.telelanguage.tlvx.model.SessionHistory;
import com.telelanguage.tlvx.model.User;
import com.telelanguage.tlvx.server.AgentServiceImpl;
import com.telelanguage.tlvx.util.SecurityUtil;
import com.telelanguage.tlvx.util.SipUtil;

/**
 * AgentManager
 */
public class AgentManager
{
    private static final Logger LOG = Logger.getLogger(AgentManager.class);

    public static final String TL_CALL_INIT = "TlCallInit";    
    public static final String TL_CALL_START = "TlCallStart";
    public static final String TL_CALL_STOP = "TlCallStop";
    public static final String TL_CALL_COMPLETE = "tl.call.complete";
    public static final String TL_CALL_REJECT = "tl.call.reject";
    public static final String TL_CALL_REQUEUE_REQUEST = "tl.call.requeue.request";
    public static final String TL_CALL_REQUEUE_RESPONSE = "tl.call.requeue.response";
    public static final String TL_CALL_PERSONALHOLD_REQUEST = "tl.call.personalhold.request";
    public static final String TL_CALL_PRIORITY_INCOMING = "TlCallPriorityIncoming";
    public static final String TL_CALL_PRIORITY_IDLE = "tl.call.priority.idle";
    public static final String TL_CALL_INCOMING_NOAGENTS = "TlCallIncomingNoagents";
    public static final String TL_CALL_INTERPRETER_HISTORY = "TlCallInterpreterHistory";
    public static final String TL_CALL_TRANSFER_REQUEST = "tl.call.transfer.request";
    public static final String TL_CALL_TRANSFER_RESPONSE = "tl.call.transfer.response";
    public static final String TL_CALL_ACCESSCODE_SET_REQUEST = "tl.call.accesscode.set.request";
    public static final String TL_CALL_ACCESSCODE_SET_RESPONSE = "tl.call.accesscode.set.response";
    public static final String TL_CONNECT_CUSTOMER = "tl.connect.customer";
    public static final String TL_CUSTOMER_STATUS_CHANGE = "TlCustomerStatusChange";
    public static final String TL_CUSTOMER_MUTE = "tl.customer.mute";
    public static final String TL_CUSTOMER_UNMUTE = "tl.customer.unmute";
    public static final String TL_CUSTOMER_DISCONNECT = "tl.customer.disconnect";
    public static final String TL_CONNECT_THIRDPARTY = "tl.connect.thirdparty";
    public static final String TL_THIRDPARTY_STATUS_CHANGE = "TlThirdpartyStatusChange";
    public static final String TL_THIRDPARTY_MUTE = "tl.thirdparty.mute";
    public static final String TL_THIRDPARTY_UNMUTE = "tl.thirdparty.unmute";
    public static final String TL_THIRDPARTY_DISCONNECT = "tl.thirdparty.disconnect";
    public static final String TL_AGENT_LOGOUT_REQUEST = "tl.agent.logout.request";
    public static final String TL_AGENT_LOGOUT_RESPONSE = "tl.agent.logout.response";
    public static final String TL_AGENT_STATUS = "TlAgentStatus";
    public static final String TL_AGENT_STATUS_UPDATE = "tl.agent.status.update";
    public static final String TL_AGENT_STATUS_RESPONSE = "TlAgentStatusResponse";
    public static final String TL_AGENT_CHANGE_PHONE = "tl.agent.change.phone";
    public static final String TL_CALL_CUSTOMER_SET_REQUEST = "tl.call.customer.set.request";
    public static final String TL_CALL_LOAD = "tl.call.load";    
    public static final String TL_CALL_SAVE = "tl.call.save";
    public static final String TL_CALL_SAVE_RESPONSE = "tl.call.save.response";    
    public static final String TL_CALL_SEARCH_CUSTOMER = "tl.search.call.customer.request";
    public static final String TL_CALL_SEARCH_CUSTOMER_RESPONSE = "tl.search.call.customer";
    public static final String TL_CALL_SEARCH_NUMBER = "tl.search.call.number.request";
    public static final String TL_CALL_SEARCH_NUMBER_RESPONSE = "tl.search.call.number";
    public static final String TL_AGENT_DISPATCH_MESSAGE = "tl.agent.dispatch.message";
    public static final String TL_CONNECT_OUTBOUND = "tl.connect.outbound";
    public static final String TL_CALL_OUTBOUND_START = "tl.call.outbound.start";
    public static final String TL_CALL_OUTBOUND_STOP = "tl.call.outbound.stop";
    public static final String TL_CALL_MARK_INTERPRETER_REQUEST = "tl.call.mark.interpreter.request";
    
    private Object callDispatcherObject = new Object();
    private CallDispatcher callDispatcher = new CallDispatcher();
    private Thread callDispatcherThread;

    private Object agentAlertsObject = new Object();
    private AgentAlerts agentAlerts = new AgentAlerts();
    private Thread agentAlertsThread;
    private boolean callAlert = false;
    private boolean priorityCall = false;
    private boolean moreThanTwoInQueue = false;
    
    private boolean sentPriorityCallMessage = false;
    
    public String getOutboundAddress() 
    {
		return TLVXManager.getProperties().getProperty("outboundAddress");
	}

    public void init()
    {
        callDispatcherThread = new Thread(callDispatcher);
        callDispatcherThread.setName("CallDispatcherThread");
        callDispatcherThread.setDaemon(true);
        callDispatcherThread.start();
        
        agentAlertsThread = new Thread(agentAlerts);
        agentAlertsThread.setName("AgentAlertsThread");
        agentAlertsThread.setDaemon(true);
        agentAlertsThread.start();
    }

    public void destroy()
    {
        callDispatcherThread = null;
        agentAlertsThread = null;
    }
    
    private class CallDispatcher implements Runnable
    {
        public void run()
        {
            while(callDispatcherThread != null)
            {
                try
                {
                	Thread.currentThread().setName("Call Dispatcher");
                    synchronized(callDispatcherObject)
                    {
                        try
                        {
                            callDispatcherObject.wait(250);
                        }
                        catch (InterruptedException e) { }
                    }
                    runCallDispatcher();
                }
                catch (Throwable t)
                {
                    LOG.warn("Uncaught exception in CallDispatcher thread, ignoring.", t);
                } 
                finally {
                	TLVXManager.cleanupSession();
                }
            }
        }
    }
    
    private class AgentAlerts implements Runnable
    {
        public void run()
        {
            while(agentAlertsThread != null)
            {
                try
                {
                    synchronized(agentAlertsObject)
                    {
                        try
                        {
                        	agentAlertsObject.wait(1000);
                        }
                        catch (InterruptedException e) { }
                    }

                    if (callAlert)
                    {
                        List<Agent> agents;
                        if (priorityCall)
	            		{
	                        agents = TLVXManager.agentDAO.findOnlineAgentsLongestIdleFirst();
	                        for (Agent agent : agents)
	                        {
	                            AgentServiceImpl.sendAgentMessage(agent, TL_CALL_PRIORITY_INCOMING+":");
	                        }
	                        sentPriorityCallMessage  = true;                
	            		}
	                    else
	                    {
	                        agents = TLVXManager.agentDAO.findOnlineAgentsLongestIdleFirst();
	                        for (Agent agent : agents)
	                        	AgentServiceImpl.sendAgentMessage(agent, TL_CALL_INCOMING_NOAGENTS+":");
	                    }
                        callAlert = false;
                        priorityCall = false;
                    }
                    sendQueueStatusUpdatesTimed();
                }
                catch (Throwable t)
                {
                    LOG.warn("Uncaught exception in AgentAlerts thread, ignoring.", t);
                } finally {
                	TLVXManager.cleanupSession();
                }
            }
        }
    }    

    public synchronized void runCallDispatcher()
    {
        doCallDispatcher();
    }
    
    public void doCallDispatcher()
    {
        List<Call> calls = TLVXManager.callDAO.findNextCallToAnswer();
        Call call = null;
        if (calls.size() > 0) call = calls.get(0);
        if (null != call)
        {
            if (false == dispatchCallToAvailableAgent(call, moreThanTwoInQueue))
            {
            	if (LOG.isDebugEnabled()) LOG.debug("reserve agent for call is not available");
            	Call noHoldcall = TLVXManager.callDAO.findNextCallNotPersonalHoldToAnswer();
            	if (null != noHoldcall) {
            		dispatchCallToAvailableAgent(noHoldcall, true);
            	} else {
            		if (LOG.isDebugEnabled()) LOG.debug("all calls arepersonal hold");
            	call = TLVXManager.callDAO.findNextCallToAnswer(call);
            		if (null != call) dispatchCallToAvailableAgent(call, false);
            	}
            }
        }
        else if (true == sentPriorityCallMessage)
        {
            List<Agent> agents = TLVXManager.agentDAO.findOnlineAgentsLongestIdleFirst();
            for (Agent activeAgent: agents)
            {
                AgentServiceImpl.sendAgentMessage(activeAgent, TL_CALL_PRIORITY_IDLE);
            }
            sentPriorityCallMessage = false;
        }        
    }
    
	public boolean logon(String email, String password, String browserSessionId, String ipAddress, String hostname, String browser, String referer, String serverId) {
		if (LOG.isDebugEnabled()) LOG.debug("agent "+email+" log-on request");
		User user = TLVXManager.userDAO.findUser(email);
		if (user == null) {
			LOG.debug("agent "+email+" unknown");
			return false;
		} 
		if (SecurityUtil.generatePassword(email, password).equalsIgnoreCase(user.getPassword())) {
			Agent agent = TLVXManager.agentDAO.findByUser(user);
        	agent.setAvailable(false);
        	agent.setOnline(true);
        	agent.setPersonalHold(false);
        	TLVXManager.agentDAO.save(agent);
//			SessionHistory sessionHistory = new SessionHistory();
//			sessionHistory.setIpaddress(ipAddress);
//			sessionHistory.setUser(user);
//			sessionHistory.setServerid(serverId);
//			sessionHistory.setHostname(hostname);
//			sessionHistory.setBrowser(browser);
//			sessionHistory.setReferer(referer);
//			sessionHistory.setUuid(browserSessionId);
//			sessionHistory.setSessionid(browserSessionId);
//			sessionHistory.setStartDate(new Date());
//			TLVXManager.sessionHistoryDAO.save(sessionHistory);
			sendQueueStatusUpdates();
			return true;
		}
		return false;
	}
	
    public synchronized void logout(String emailAddress) 
    {
    	if (LOG.isDebugEnabled()) LOG.debug("agent "+emailAddress+" log-off request");
    	
    	User user = TLVXManager.userDAO.findUser(emailAddress);
    	Agent agent = TLVXManager.agentDAO.findByUser(user);
        if (null != agent)
        {
        	Call call = getCall(agent);
        	if (null != call)
        		TLVXManager.callSessionManager.onCallSessionCompleteCall(""+call.getId(), true, null);
        	else
            	if (LOG.isDebugEnabled()) LOG.debug("not sending message, call not found for " + emailAddress);        	
        	
//        	//By: Rafael Revi Date: 05/13/2009, added a active sessions check. So as to find the current active session and end it with datetime stamp
        	Collection<SessionHistory> history = TLVXManager.sessionHistoryDAO.findActiveSessionsForUser(agent.getUser());
        	if (history!=null && false == history.isEmpty())
            {
                for (SessionHistory session : history)
                {
                	session.setEndDate(new Date());
                    session = TLVXManager.sessionHistoryDAO.save(session);
                }
            }
        	agent.setAvailable(false);
        	agent.setOnline(false);
            agent.setSipUri(agent.getDefaultUri());
            TLVXManager.agentDAO.save(agent);
        	Event agentLogoutEvent = new Event();
        	agentLogoutEvent.setCode(91);
        	agentLogoutEvent.setCreated_at(Calendar.getInstance().getTime());        	
        	agentLogoutEvent.setPayload(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        	agentLogoutEvent.setAgent_id(agent.getUser().getUserId());
        	TLVXManager.eventDAO.save(agentLogoutEvent);
        }
        sendQueueStatusUpdates();
        sendAgentStatusUpdate(emailAddress);
    }
    
    public void sendQueueStatusUpdatesTimed()
    {
    	int numberAgentsAvailable = TLVXManager.agentDAO.findAvailableAgentsCount(null);
    	int numberAgentsOnline = TLVXManager.agentDAO.findOnlineAgentsCount();
    	int numberOngoingCalls = TLVXManager.callDAO.findTotalCallsOngoing();
    	int numberCallsInQueue = TLVXManager.callDAO.findCallsInQueue();
    	moreThanTwoInQueue = numberCallsInQueue > 2;
    	AgentServiceImpl.sendAllMessage(TL_AGENT_STATUS_RESPONSE+":"+numberAgentsAvailable+"/"+numberAgentsOnline+" Agents Available/Online, "+numberOngoingCalls+" Ongoing Calls, "+numberCallsInQueue+" Calls in Queue");
    }
    
    public void sendQueueStatusUpdates()
    {
//    	int numberAgentsAvailable = TLVXManager.agentDAO.findAvailableAgentsCount(null);
//    	int numberAgentsOnline = TLVXManager.agentDAO.findOnlineAgentsCount();
//    	int numberOngoingCalls = TLVXManager.callDAO.findTotalCallsOngoing();
//    	int numberCallsInQueue = TLVXManager.callDAO.findCallsInQueue();
//    	moreThanTwoInQueue = numberCallsInQueue > 2;
//    	AgentServiceImpl.sendAllMessage(TL_AGENT_STATUS_RESPONSE+":"+numberAgentsAvailable+"/"+numberAgentsOnline+" Agents Available/Online, "+numberOngoingCalls+" Ongoing Calls, "+numberCallsInQueue+" Calls in Queue");
    }
    
    public void sendAgentStatusUpdate(String email) {
    	int numberAgentsAvailable = TLVXManager.agentDAO.findAvailableAgentsCount(null);
    	int numberAgentsOnline = TLVXManager.agentDAO.findOnlineAgentsCount();
    	int numberOngoingCalls = TLVXManager.callDAO.findTotalCallsOngoing();
    	int numberCallsInQueue = TLVXManager.callDAO.findCallsInQueue();
    	Agent agent = getAgent(TLVXManager.userDAO.findUser(email));
    	AgentServiceImpl.sendAgentMessage(agent, TL_AGENT_STATUS_RESPONSE+":"+numberAgentsAvailable+"/"+numberAgentsOnline+" Agents Available/Online, "+numberOngoingCalls+" Ongoing Calls, "+numberCallsInQueue+" Calls in Queue");
    	dispatchAgentMessage(agent, getAgentDetails(agent), TL_AGENT_STATUS);
    }
    
    /**
     * for a given callSession, find the next available agent to dispatch the 
     * call to (and optionally exclude a given agent from getting the call)
     * @param moreCalls 
     * 
     * @param callSession
     * @param exclude
     */   
    public boolean dispatchCallToAvailableAgent(Call call, boolean moreCalls)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("dispatchCallToAvailableAgent call = " + call+" " +moreCalls);
    	List<Agent> agents = TLVXManager.agentDAO.findOnlineAndAvailableAgentsLongestIdleFirst();
    	if (LOG.isDebugEnabled()) LOG.debug("after findOnlineAndAvailableAgentsLongestIdleFirst");
    	if (!TLVXManager.callSessionManager.verifyCustomerOnCall(call)) {
    		if (LOG.isDebugEnabled()) LOG.debug("dispatchCallToAvailableAgent customer is no longer on call ");
    		call.setStatus(CallSessionManager.CALL_STATUS_FINISHED);
            call = TLVXManager.callDAO.save(call);
    		return false;
    	}
    	boolean handled = false;
    	if (false == agents.isEmpty())
    	{
    		for (Agent agent : agents)
    		{
    			try
    			{
                    if (call.getReservedAgent() != null && call.getReservedAgent().getId().equals(agent.getId()) && agent.isPersonalHold() && moreCalls)
                    {
                    	if (LOG.isDebugEnabled()) LOG.debug("agent wants to take a different call first " + agent.getId());
                        continue;
                    }
                    if (call.getReservedAgent() != null && false == call.getReservedAgent().getId().equals(agent.getId()) && moreCalls)
                    {
                    	if (LOG.isDebugEnabled()) LOG.debug("found available agent, but call is not reserved for " + agent.getId());
                        continue;
                    }
    				if (processAgentReadyForCall(agent, call))
    				{
	    				handled = true;
	    				break;
    				}
    			}
    			catch (IOException e)
    			{
    				LOG.warn("unable to route call to " + agent + ", call: " + call, e);
    			}
    		}
    	}
    	
    	if (call.getReservedAgent() != null && false == handled)
    	{
    		return false;
    	}
    	
    	if (false == handled && call.getReservedAgent() == null)
    	{
    		if (LOG.isInfoEnabled()) LOG.info("no available agent to handle customer call, queuing " + call);

            call.setStatus(Call.CALL_STATUS_QUEUED);
            call = TLVXManager.callDAO.save(call);            
            callAlert = true;
            priorityCall = call.isPriorityCall();
    	}
    	
    	if (LOG.isDebugEnabled()) LOG.debug("end of dispatchCallToAvailableAgent");
    	
        TLVXManager.agentManager.sendQueueStatusUpdates();

		return true;
    }

    public boolean processAgentReadyForCall(Agent agent, Call call) throws IOException
    {
        if (agent.isOnline()  && agent.isAvailable())
        {
            if (LOG.isInfoEnabled()) LOG.info("processing call "+call+" to " + agent);
            call.setStatus(Call.CALL_STATUS_FORWARDED_TO_AGENT);
        	call = TLVXManager.callDAO.save(call);
        	
            TLVXManager.callSessionManager.onCallSessionAgentSet(agent.getId(), call.getCallSessionId());
        	
            agent.setLastAttemptDate(new Date());
            agent.setBusy(true);
            agent.setPersonalHold(false);
            agent = TLVXManager.agentDAO.save(agent);

            if (LOG.isInfoEnabled()) LOG.info("routing new incoming call "+call+" to " + agent);
            
            TLVXManager.agentManager.sendQueueStatusUpdates();
            dispatchAgentMessage(agent, getAgentDetails(agent), TL_AGENT_STATUS);
            return true;
        }
        else if (LOG.isInfoEnabled()) LOG.info("agent completed last call and is now offline: " + agent);
        dispatchAgentMessage(agent, getAgentDetails(agent), TL_AGENT_STATUS);
        return false;
    }
    
    public synchronized void changeAgentStatus(String emailAddress, Boolean online) 
    {
    	if (LOG.isDebugEnabled()) LOG.debug("agent "+emailAddress+" changing status to "+online);
    	User user = TLVXManager.userDAO.findUser(emailAddress);
    	Agent agent = TLVXManager.agentDAO.findByUser(user);
    	if (agent != null) {
	        if (online) {
	            agent.setOnline(true);
	            agent.setAvailable(true);
	            agent.setBusy(false);
	            agent = TLVXManager.agentDAO.save(agent);
	            synchronized(callDispatcherObject)
	            {
	                callDispatcherObject.notify();
	            }
	        } else
	        {               
	        	agent.setAvailable(false);
	            agent = TLVXManager.agentDAO.save(agent);
	        }
	        dispatchAgentMessage(agent, getAgentDetails(agent), TL_AGENT_STATUS);
	        sendQueueStatusUpdates();
    	}
    }
    
    public void doAgentRequeueRequest(String emailAddress, String callId)
    {
    	User user = TLVXManager.userDAO.findUser(emailAddress);
        Agent agent = getAgent(user);
        Call thisCall = TLVXManager.callDAO.findById(Long.parseLong(callId));
        if (null != agent)
        {
            boolean priorityCall = thisCall.isPriorityCall();
            boolean performRequeue = true;
            if (true == priorityCall)
            {
                Call call = TLVXManager.callDAO.findNextCallToAnswerNotReserved();
                if (null != call && true == call.isPriorityCall())
                {
                    call.setReservedAgent(agent);
                    call = TLVXManager.callDAO.save(call);
                    List<Agent> agents = TLVXManager.agentDAO.findOnlineAgentsLongestIdleFirst();
                    for (Agent activeAgent: agents)
                    	AgentServiceImpl.sendAgentMessage(activeAgent, TL_CALL_PRIORITY_IDLE);

                }
                else performRequeue = false;
            }
            if (performRequeue) TLVXManager.callSessionManager.onCallSessionAgentRequeue(thisCall);
            else AgentServiceImpl.sendAgentMessage(agent, TL_CALL_REQUEUE_RESPONSE+"failed");
        }        
    }

    public void handleAgentPersonalHoldRequest(String emailAddress, String callId)
    {
        if (LOG.isDebugEnabled()) LOG.debug("agent personal hold call request "+callId);
    	User user = TLVXManager.userDAO.findUser(emailAddress);
        Agent agent = getAgent(user);
        if (null != agent)
        {
            Call call = TLVXManager.callDAO.findById(Long.parseLong(callId));
            if (call != null)
            {
                call.setReservedAgent(agent);
                call = TLVXManager.callDAO.save(call);
                agent.setPersonalHold(true);
                agent = TLVXManager.agentDAO.save(agent);
                TLVXManager.callSessionManager.onCallSessionAgentRequeue(call);
            }
            else if (LOG.isDebugEnabled()) LOG.debug("not sending message, call not found for " + callId);        	
        }
    }

    public synchronized void handleAgentPhoneChange(String emailAddress, String numberPassed) 
    {
    	User user = TLVXManager.userDAO.findUser(emailAddress);
    	Agent agent = getAgent(user);
    	if (agent!=null)
    	{
    		if (!SipUtil.validateNumber(numberPassed)) throw new RuntimeException("Invalid number "+numberPassed+" passed into agent phone number.");
    		String number = getOutboundPhoneAddress(numberPassed);
    		LOG.info("agent changed phone number from "+agent.getSipUri()+" to "+number);
    		agent.setSipUri(number);
    		agent = TLVXManager.agentDAO.save(agent);
    		dispatchAgentMessage(agent, getAgentDetails(agent), TL_AGENT_STATUS);
    	}
    }
    
    private Call getCall(Agent agent)
    {
    	Call call = TLVXManager.callDAO.findByAgent(agent);
    	if (call == null)
    	{
        	LOG.warn("unable to find call for agent " + agent);
    	}
    	return call;
    }
    
    /**
     * for a given user, return the agent object or null if not found
     * 
     * @param user
     * @return
     */
    private Agent getAgent(User user)
    {
        if (null == user)
        {
        	LOG.warn("user is null, not able to get agent");
            return null;
        }
        return TLVXManager.agentDAO.findByUser(user);
    }

    /**
     * called when an agent is no longer connected
     * 
     * @param agent
     */
	public void agentCallDisconnected(Agent agent, Call call) 
	{
		try {
			if (LOG.isDebugEnabled()) LOG.debug("agentCallDisconnected, agent = " + agent + ", call = " + call);
			doAgentCallDisconnected(agent, call);
		} catch (Exception e) {
			LOG.error("agentCallDisconnected", e);
		}
	}
	
    public void doAgentCallDisconnected(Agent agent, Call call)
    {
        if (null != agent)
        {
            agent.setBusy(false);
            agent = TLVXManager.agentDAO.save(agent);
//            if (null != call)
//            {
//                CallStat stat = new CallStat();
//                stat.setCall(call);
//                stat.setLength((int)(new Date().getTime() - call.getLastAnsweredDate().getTime()));
//                stat.setAgent(agent);
//                TLVXManager.callStatDAO.save(stat);
//            }
            synchronized(callDispatcherObject)
            {
                callDispatcherObject.notify();
            }
        }
    }
	
	private String getOutboundPhoneAddress (String number)
	{
		if (false == number.startsWith("sip:"))
		{
			number = number.replace("(", "").replace(")","").replace("-","").replace(" ", "").replace(".", "");
			number = "sip:" + number + "@" + getOutboundAddress();
		}
		return number;
	}
	
	private Map<String, String> getAgentDetails(Agent agent) {
		Map<String, String> data = new HashMap<String, String>();
    	data.put("name",agent.getUser().getFirstName()+" "+agent.getUser().getLastName());
    	data.put("status",agent.isOnline() && agent.isAvailable()?AgentEventType.ONLINE.toString().toLowerCase():AgentEventType.OFFLINE.toString().toLowerCase());
    	data.put("number",SipUtil.getPhoneNumber(agent.getSipUri()));
		return data;
	}

	public void dispatchAgentMessage(Agent agent, Map<String, String> data, String type) {
		AgentServiceImpl.sendAgentMessage(agent, type+":"+data.toString());
	}
}
