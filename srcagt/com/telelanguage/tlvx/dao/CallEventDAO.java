package com.telelanguage.tlvx.dao;

import java.util.Date;
import java.util.List;

import com.telelanguage.tlvx.model.Call;
import com.telelanguage.tlvx.model.CallEvent;
import com.telelanguage.tlvx.service.TLVXManager;

/**
 * CallEventDAO
 */
public class CallEventDAO
{
    public boolean isIVR(Call call)
    {
        CallEvent event = (CallEvent) TLVXManager.getSession()
        		.createQuery("FROM CallEvent e WHERE e.call = :call AND e.eventType = :eventType")
        		.setParameter("call", call)
        		.setParameter("eventType", CallEvent.IVR_CONNECT)
        		.setMaxResults(1)
        		.uniqueResult();
        if (null != event)
        {
            return true;
        }
        return false;
    }
    
    @SuppressWarnings("unchecked")
    public int getAgentConnects(Call call)
    {
        List<CallEvent> events = TLVXManager.getSession()
        		.createQuery("FROM CallEvent e WHERE e.call = :call AND e.eventType = :eventType")
        		.setParameter("call", call)
        		.setParameter("eventType", CallEvent.AGENT_CONNECT)
        		.list();
        return events.size();
    }

    @SuppressWarnings("unchecked")
    public int getRequeues(Call call)
    {
        List<CallEvent> events = TLVXManager.getSession()
        		.createQuery("FROM CallEvent e WHERE e.call = :call AND e.eventType = :eventType")
        		.setParameter("call", call)
        		.setParameter("eventType", CallEvent.AGENT_REQUEUE_CALL)
        		.list();
        return events.size();
    }


    @SuppressWarnings("unchecked")
    public int getInterpreterAttempts(Call call)
    {
        List<CallEvent> events = TLVXManager.getSession()
        		.createQuery("FROM CallEvent e WHERE e.call = :call AND e.eventType = :eventType")
        		.setParameter("call", call)
        		.setParameter("eventType", CallEvent.INTERPRETER_CONNECTING)
        		.list();
        return events.size();
    }


    @SuppressWarnings("unchecked")
    public int getInterpreterRejects(Call call)
    {
        List<CallEvent> events = TLVXManager.getSession()
        		.createQuery("FROM CallEvent e WHERE e.call = :call AND e.eventType = :eventType")
        		.setParameter("call", call)
        		.setParameter("eventType", CallEvent.INTERPRETER_REJECTED)
        		.list();
        return events.size();
    }
    

    public float getCallQueueTime(Call call)
    {
        return findEventTime(call, CallEvent.CUSTOMER_QUEUED, CallEvent.AGENT_CONNECT, "ASC", "ASC", false);
    }


    public float getInterpreterConnectTime(Call call, boolean ivr)
    {
        if (ivr)
        {
            return findEventTime(call, CallEvent.IVR_DISCONNECT, CallEvent.INTERPRETER_ON_CALL, "DESC", "ASC", false);
        }
        else
        {
        	CallEvent completeCall = (CallEvent) TLVXManager.getSession()
            		.createQuery("FROM CallEvent e WHERE e.call = :call AND e.eventType = :eventType ORDER BY date DESC")
            		.setParameter("call", call)
            		.setParameter("eventType", CallEvent.AGENT_COMPLETE_CALL)
            		.setMaxResults(1)
            		.uniqueResult();
        	if (completeCall == null)
        	{
                return findEventTime(call, CallEvent.INTERPRETER_MANUAL_CONNECTING, CallEvent.INTERPRETER_ON_CALL, "ASC", "ASC", false);
        	}
        	else
        	{
            	CallEvent manualConnect = (CallEvent) TLVXManager.getSession()
                		.createQuery("FROM CallEvent e WHERE e.call = :call AND e.eventType = :eventType ORDER BY date DESC")
                		.setParameter("call", call)
                		.setParameter("eventType", CallEvent.INTERPRETER_MANUAL_CONNECTING)
                		.setMaxResults(1)
                		.uniqueResult();
            	if (manualConnect == null)
            	{
                    return findEventTime(call, CallEvent.AGENT_COMPLETE_CALL, CallEvent.INTERPRETER_ON_CALL, "ASC", "ASC", false);
            	}
            	else if (completeCall.getDate().compareTo(manualConnect.getDate()) > 0)
            	{
                    return findEventTime(call, CallEvent.INTERPRETER_MANUAL_CONNECTING, CallEvent.INTERPRETER_ON_CALL, "ASC", "ASC", false);
            	}
            	else
            	{
                    return findEventTime(call, CallEvent.AGENT_COMPLETE_CALL, CallEvent.INTERPRETER_ON_CALL, "ASC", "ASC", false);
            	}
        	}
        }
    }

    public float getIVRTime(Call call)
    {
        return findEventTime(call, CallEvent.IVR_CONNECT, CallEvent.INTERPRETER_CONNECTING, "ASC", "ASC", false);
    }
    
    public float getInterpreterDialTime(Call call)
    {
        return findEventTime(call, CallEvent.INTERPRETER_CONNECTING, CallEvent.INTERPRETER_ON_CALL, "ASC", "ASC", true);
    }

    public float getInterpreterTalkTime(Call call)
    {
        float time = findEventTime(call, CallEvent.INTERPRETER_ON_CALL, CallEvent.INTERPRETER_DISCONNECT, "DESC", "DESC", true);
        if (time == 0) {
        	Date start = getInterpreterStartTime(call);
        	if (start != null) {
                CallEvent end = (CallEvent) TLVXManager.getSession()
                		.createQuery("FROM CallEvent e WHERE e.call = :call AND e.eventType = :eventType ORDER BY date DESC")
                		.setParameter("call", call)
                		.setParameter("eventType", CallEvent.CUSTOMER_DISCONNECT)
                		.setMaxResults(1)
                		.uniqueResult();
                if (end != null) {
                	time = (end.getDate().getTime() - start.getTime())/1000;
                }
        	}
        }
        return (float)Math.ceil(time);
    }

    public Date getInterpreterStartTime(Call call)
    {
        CallEvent start = (CallEvent) TLVXManager.getSession()
        		.createQuery("FROM CallEvent e WHERE e.call = :call AND e.eventType = :eventType ORDER BY date DESC")
        		.setParameter("call", call)
        		.setParameter("eventType", CallEvent.INTERPRETER_ON_CALL)
        		.setMaxResults(1)
        		.uniqueResult();
        if (null != start)
        {
        	return start.getDate();
        }
        else
        {
        	return null;
        }
    }

    public Date getInterpreterEndTime(Call call)
    {
    	CallEvent interpreter = (CallEvent) TLVXManager.getSession()
        		.createQuery("FROM CallEvent e WHERE e.call = :call AND e.eventType = :eventType ORDER BY date DESC")
        		.setParameter("call", call)
        		.setParameter("eventType", CallEvent.INTERPRETER_DISCONNECT)
        		.setMaxResults(1)
        		.uniqueResult();
    	CallEvent customer = (CallEvent) TLVXManager.getSession()
        		.createQuery("FROM CallEvent e WHERE e.call = :call AND e.eventType = :eventType ORDER BY date DESC")
        		.setParameter("call", call)
        		.setParameter("eventType", CallEvent.CUSTOMER_DISCONNECT)
        		.setMaxResults(1)
        		.uniqueResult();

    	if (interpreter == null && customer == null)
    	{
    		return null;
    	}
    	else if (interpreter == null && customer != null)
    	{
    		return customer.getDate();
    	}
    	else if (interpreter != null && customer == null)
    	{
    		return interpreter.getDate();
    	}
    	else if (interpreter.getDate().compareTo(customer.getDate()) > 0)
    	{
    		return interpreter.getDate();
    	}
    	else
    	{
    		return customer.getDate();
    	}
    }

    public float getThirdPartyTalkTime(Call call)
    {
        //return findEventTime(call, CallEvent.THIRDPARTY_CONNECT, CallEvent.THIRDPARTY_DISCONNECT, "ASC", "ASC", true);
    	//select sum(unix_timestamp(end_time) - unix_timestamp(start_time)) from thirdparty_activities where call_id=105804171 and start_time is not null and end_time is not null;
    	Long thirdPartySeconds = (Long) TLVXManager.getSession()
    			.createQuery("select sum(unix_timestamp(end_time) - unix_timestamp(start_time)) from ThirdPartyActivity where callId = :callid and startTime is not null and endTime is not null")
    			.setParameter("callid", call.getId())
    			.uniqueResult();
    	if (thirdPartySeconds == null) thirdPartySeconds = 0L;
    	return (float) thirdPartySeconds;
    }

    private float findEventTime(Call call, int event1, int event2, String order1, String order2, boolean checkPayload)
    {
        CallEvent start = (CallEvent) TLVXManager.getSession()
        		.createQuery("FROM CallEvent e WHERE e.call = :call AND e.eventType = :eventType ORDER BY date " + order1)
        		.setParameter("call", call)
        		.setParameter("eventType", event1)
        		.setMaxResults(1)
        		.uniqueResult();
        CallEvent end = (CallEvent) TLVXManager.getSession()
        		.createQuery("FROM CallEvent e WHERE e.call = :call AND e.eventType = :eventType ORDER BY date " + order2)
        		.setParameter("call", call)
        		.setParameter("eventType", event2)
        		.setMaxResults(1)
        		.uniqueResult();
        if (start != null && end != null)
        {
            float val = (end.getDate().getTime() - start.getDate().getTime())/1000;

            if (checkPayload)
            {
                if ( (start.getPayload() == null && end.getPayload() == null) || 
                     (start.getPayload() != null && start.getPayload().equals(end.getPayload())) )
                {
                	if (val < 0) return 0;
                    return val;
                }
            }
            else
            {
            	if (val < 0) return 0;
                return val;
            }
        }
        return 0;
    }

	public void save(CallEvent callEvent) {
		TLVXManager.getSession().saveOrUpdate(callEvent);
		TLVXManager.commit();
	}

	public String getLastAgentId(Call call) {
        CallEvent event = (CallEvent) TLVXManager.getSession()
        		.createQuery("FROM CallEvent e WHERE e.call = :call AND e.eventType = :eventType ORDER BY date DESC")
        		.setParameter("call", call)
        		.setParameter("eventType", CallEvent.AGENT_CONNECT)
        		.setMaxResults(1)
        		.uniqueResult();
        if (event != null)
        {
        	return event.getPayload();
        }
        return null;
	}
}
