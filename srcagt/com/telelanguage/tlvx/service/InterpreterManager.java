package com.telelanguage.tlvx.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import com.telelanguage.api.INTAPIClient;
import com.telelanguage.api.InterpreterCallInfo;
import com.telelanguage.api.InterpreterInfo;
import com.telelanguage.tlvx.model.Call;
import com.telelanguage.tlvx.model.CallEvent;
import com.telelanguage.tlvx.model.Customer;
import com.telelanguage.tlvx.model.Interpreter;
import com.telelanguage.tlvx.model.InterpreterMissedCall;
import com.telelanguage.tlvx.model.InterpreterStatus;
import com.telelanguage.tlvx.server.AgentServiceImpl;
import com.telelanguage.videoapi.BlueStreamRequest;

/**
 * InterpreterManager
 */
public class InterpreterManager
{
	Random rand = new Random();
	Float percentIgnorePriority = -1F;
	
    private static final Logger LOG = Logger.getLogger(InterpreterManager.class);

    public static final String TL_INTERPRETER_LANGUAGE_SEARCH_REQUEST = "tl.interpreter.language.search.request";
    public static final String TL_INTERPRETER_LANGUAGE_SEARCH_RESPONSE = "tl.interpreter.language.search.response";
    public static final String TL_INTERPRETER_SEARCH_REQUEST = "tl.interpreter.search.request";
    public static final String TL_INTERPRETER_SEARCH_RESPONSE = "tl.interpreter.search.response";
    public static final String TL_INTERPRETER_STATUS_CHANGE = "TlInterpreterStatusChange";
    public static final String TL_CONNECT_INTERPRETER = "tl.connect.interpreter";
    public static final String TL_INTERPRETER_MUTE = "tl.interpreter.mute";
    public static final String TL_INTERPRETER_UNMUTE = "tl.interpreter.unmute";
    public static final String TL_INTERPRETER_DISCONNECT = "tl.interpreter.disconnect";
    public static final String TL_INTERPRETER_LIST = "tl.interpreter.list";
    
    public static Byte maxInterpreterMissedCalls = 3;
    private static Map<String, INTAPIClient> INTAPIClients = new HashMap<String, INTAPIClient>();
    
    public static Map<String, Float> languagePriorityLanguageMap = new HashMap<String, Float>();
    
    public InterpreterManager() {
    	String propertyignoresetting = (String) TLVXManager.getProperties().get("ignorePriorityPercentageAsFloat");
    	try {
    		percentIgnorePriority = Float.parseFloat(propertyignoresetting);
    	}
    	catch(Exception e) {
    		LOG.error("Unable to parse ignorePriorityPercentageAsFloat = "+propertyignoresetting);
    	}
    	for (Object key : TLVXManager.getProperties().keySet()) {
    		String keystr = (String) key;
    		if(keystr.startsWith("ignorePriorityPercentageAsFloat.")) {
    			String language = keystr.substring(32);
    			Float percentage = Float.parseFloat((String)TLVXManager.getProperties().getProperty(keystr));
    			languagePriorityLanguageMap.put(language, percentage);
    		}
    	}
    	System.out.println("Priority setting: "+percentIgnorePriority+" "+languagePriorityLanguageMap);
    }

    public Byte getMaxInterpreterMissedCalls()
    {
        return maxInterpreterMissedCalls;
    }

    public void setMaxInterpreterMissedCalls(Byte maxInterpreterMissedCalls)
    {
        InterpreterManager.maxInterpreterMissedCalls = maxInterpreterMissedCalls;
    }
      
    public void failedInterpreter(Interpreter interpreter, boolean manualDial, Long callId, String language, String reason, String callType)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("failedInterpreter - interpreter="+interpreter);
        }
        
        try
        {
            if (null != interpreter && interpreter.getActiveSession())
            {
            	boolean loggedOut = TLVXManager.interpreterDAO.interpreterFailed(interpreter, manualDial, reason);
            	if (interpreter.getOnWebSite() != null) {
	            	if (loggedOut) {
						try {
							TLVXManager.interpreterManager.getINTClient(interpreter.getOnWebSite())
								.interpreterLogout(getInterpreterInfo(interpreter));
						} catch (Exception e) {
							LOG.error("Unable to send message to interpreter server, log out interpreter",e);
							interpreter.setOnWebSite(null);
							interpreter.setWebPhone(false);
							interpreter.setVideo(false);
							TLVXManager.getSession().saveOrUpdate(interpreter);
						}
	            	} else {
	            		try {
							TLVXManager.interpreterManager.getINTClient(interpreter.getOnWebSite())
								.disconnect(getInterpreterInfo(interpreter));
						} catch (Exception e) {
							LOG.error("Unable to send message to interpreter server, log out interpreter",e);
							interpreter.setOnWebSite(null);
							interpreter.setWebPhone(false);
							interpreter.setVideo(false);
							TLVXManager.getSession().saveOrUpdate(interpreter);
						}
	            	}
            	}
            	if (!"clean up session".equals(reason)) {
	            	interpreter = TLVXManager.interpreterDAO.refreshInterpreter(interpreter);
	            	LOG.info("Updated interpreter "+interpreter.getNumMissedCalls());
	            	recordInterpreterMissedCall(interpreter, callId, language, reason, interpreter.getNumMissedCalls(), loggedOut, manualDial, callType);
	            	if (loggedOut) {
	            		interpreter.setActiveSession(false);
	            		interpreter.setOnWebSite(null);
						interpreter.setWebPhone(false);
						interpreter.setVideo(false);
	            	}
	            }
        	}
        }
        catch (Throwable t)
        {
            LOG.warn("Error occurred trying to mark interpreter as failed", t);
        }
    }
    
    private InterpreterCallInfo getInterpreterInfo(Interpreter interpreter) {
    	InterpreterCallInfo callInfo = new InterpreterCallInfo();
    	callInfo.missedCalls = ""+interpreter.getNumMissedCalls();
    	if (interpreter != null) callInfo.interpreterInfo = TLVXAPIServiceImpl.convertToInterpreterInfo(interpreter, null);
		return callInfo;
	}

	/**
     * @param callSession
     * @param exclude
     * @param language
     * @return 
     */ 
    public Boolean dispatchCallToAvailableInterpreter(Call call, List<Interpreter> history, String language, String interpreterGender, Boolean interpreterVideo, String id, boolean completeCall)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("dispatchCallToAvailableInterpreter - call="+call+",history="+history+",language="+language+",interpreterGender="+interpreterGender+",interpreter id="+id);
        }
        
        if ("".equals(language))
        {
            LOG.debug("received an empty interpreter string, setting to null");
        	language = null;
        }
        
        if (null != id)
       	{
       		Interpreter interpreter = TLVXManager.interpreterDAO.findInterpreterByID(id);
            if (interpreter != null && TLVXManager.interpreterDAO.markInterpreterOnCall(interpreter, call))
            {
                CallEvent callEvent = new CallEvent();
                callEvent.setCall(call);
                callEvent.setDate(new Date());
                callEvent.setPayload(id);
                callEvent.setPayload2(null);
                callEvent.setEventType(CallEvent.INTERPRETER_MANUAL_CONNECTING);
                TLVXManager.callEventDAO.save(callEvent);

                LOG.debug("found Interpreter => "+interpreter);
            	//TLVXManager.interpreterDAO.markInterpreterOnCall(interpreter);
                //setInterpreterStatus(interpreter, InterpreterStatus.INTERPRETER_STATUS_BUSY);  
                TLVXManager.callSessionManager.onCallSessionInterpreterConnect(call.getCallSessionId(), id, language, interpreterGender, true, interpreterVideo);
    	    	return true;
            }
            LOG.debug("after found Interpreter => "+interpreter);
       	} else
       	if (null != language)
       	{
       		try {
	       		boolean ignorePriority = percentIgnorePriority < rand.nextFloat();
	       		Float languagePriority = languagePriorityLanguageMap.get(language.toLowerCase());
	       		if (languagePriority != null) {
	       			ignorePriority = languagePriority < rand.nextFloat();
	       		}
	       		List<Object[]> results = TLVXManager.interpreterBlackListDAO.removeBlackListedInterpreters(call.getAccessCode(), TLVXManager.interpreterDAO.findActiveByLanguageSearch(call.getSubscriptionCode(),language,interpreterGender, interpreterVideo, ignorePriority, 10));
	        	for (Object[] result : results)
	        	{
	        		Interpreter interpreter = (Interpreter)result[0];
	        		if (false == isInterpreterExcluded(history, interpreter)) {
	        			if (TLVXManager.interpreterDAO.markInterpreterOnCall(interpreter, call)) {
		        		    //setInterpreterStatus(interpreter, InterpreterStatus.INTERPRETER_STATUS_BUSY);
		        	        TLVXManager.callSessionManager.onCallSessionInterpreterConnect(call.getCallSessionId(), interpreter.getInterpreterId(), language, interpreterGender, false, interpreterVideo);
		        	    	return true;
	        			}
	        			//TLVXManager.commit();  // release the lock on the failed to mark interpreter
	        		}
	        	}
       		} catch (Exception e) {
       			LOG.error("Error finding interpreter by language ",e);
       		}
       	}
       	call = TLVXManager.callDAO.findById(call.getId());
   		if ("BluestreamSign".equalsIgnoreCase(language)) { // there is no BluestreamSign so never send it.
   			if (completeCall) {
	   			// Send message to video client to call Bluestream
	   			Customer customer = TLVXManager.customerDAO.executeCustomerGet(call.getCustomer());
	   			BlueStreamRequest request = new BlueStreamRequest();
	   			request.customerId = customer.getCustomerId();
	   			request.customerName = customer.getName();
	   			request.language = language;
	   			TLVXManager.callSessionManager.onBluestreamInitiateCall(call, request);
	   			return true;
   			} else {
   				if (call.getAgent() != null) {
	   				HashMap data = new HashMap();
	   				data.put("status", "idle");
	   				TLVXManager.agentManager.dispatchAgentMessage(call.getAgent(), data, InterpreterManager.TL_INTERPRETER_STATUS_CHANGE);
	   				AgentServiceImpl.sendAgentMessage(call.getAgent(), "ErrorMessage:Use Complete Call for Sign Language.");
   				}
   			}
   		} else {
   	       	if (call.getAgent() != null) {
   	       		LOG.info("Unable to find interpreter to dial in Connect for call "+call.getId()+", agent already on call.");
   	       		HashMap data = new HashMap();
   				data.put("status", "idle");
   				TLVXManager.agentManager.dispatchAgentMessage(call.getAgent(), data, InterpreterManager.TL_INTERPRETER_STATUS_CHANGE);
   				AgentServiceImpl.sendAgentMessage(call.getAgent(), "ErrorMessage:There are no interpreters online and available, you can try to manually dial one.");
   	       	} else {
	       		LOG.info("Unable to find interpreter to dial for call "+call.getId()+", requeuing call.");
	       		TLVXManager.callSessionManager.onCallSessionAgentRequeue(call);
   	       	}
   		}
       	
       	return false;
    }
    
    private boolean isInterpreterExcluded(List<Interpreter> history, Interpreter interpreter)
    {
        if (null != history)
        {
            for (Interpreter exclude : history)
            {
                if (exclude.getInterpreterId().equals(interpreter.getInterpreterId()))
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void setInterpreterStatus(Interpreter interpreter, int status)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("status = " + status + ", interpreter " + interpreter);
        if (null != interpreter)
        {
        	//TLVXManager.interpreterStatusDAO.setInterpreterStatus(interpreter, status);
            if (status == InterpreterStatus.INTERPRETER_STATUS_IDLE) {
            	if (LOG.isDebugEnabled()) LOG.debug("status = " + status + ", interpreter " + interpreter.getInterpreterId() + " marking on_call=0");
            	TLVXManager.interpreterDAO.markInterpreterOffCall(interpreter);
            } else {
            	if (LOG.isDebugEnabled()) LOG.debug("status = " + status + ", interpreter " + interpreter.getInterpreterId() + " did not mark on_call = 0");
            }
        }
    }
    
    public void setInterpreterStatus(Interpreter interpreter, int status, long callId)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("status = " + status + ", interpreter " + interpreter);
        if (null != interpreter)
        {
        	//TLVXManager.interpreterStatusDAO.setInterpreterStatus(interpreter, status);
            if (status == InterpreterStatus.INTERPRETER_STATUS_IDLE) {
            	if (LOG.isDebugEnabled()) LOG.debug("status = " + status + ", interpreter " + interpreter.getInterpreterId() + " marking on_call=0");
            	TLVXManager.interpreterDAO.markInterpreterOffCall(interpreter, callId);
            } else {
            	if (LOG.isDebugEnabled()) LOG.debug("status = " + status + ", interpreter " + interpreter.getInterpreterId() + " did not mark on_call = 0");
            }
        }
    }

    public Boolean handleInterpreterConnect(String callId, String language, String interpreterGender, Boolean interpreterVideo, String interpreterid, boolean manualDial, boolean completeCall)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("connnect interpreter request = "+callId+" "+language);
    	
    	Call call = TLVXManager.callDAO.findById(Long.parseLong(callId));

    	synchronized(call) { 
	    	if (!manualDial)
	    	{
	            if (LOG.isDebugEnabled()) LOG.debug("dispatching interpreter call for language: "+language+" and gender "+interpreterGender);
	    		return dispatchCallToAvailableInterpreter(call, null, language, interpreterGender, interpreterVideo, null, completeCall);
	    	}
	    	else
	    	{
	            if (LOG.isDebugEnabled()) LOG.debug("dispatching interpreter call for interpreter id: "+interpreterid);
	            return dispatchCallToAvailableInterpreter(call, null, language, interpreterGender, interpreterVideo, interpreterid, completeCall);    		
	    	}
    	}
    }
    
    public String getPhoneType(Interpreter i) {
    	String type = getPhoneNumber(i);
    	if (i.getWebPhone()) type = "Web";
    	if (i.isVideo()) type = "Web & Video";
    	return type;
    }

    public String getPhoneNumber(Interpreter i)
    {
        String phoneNumber = i.getPhoneNumber();
        if (phoneNumber.length()>3 && phoneNumber.charAt(3)!='-')
        {
            phoneNumber = phoneNumber.substring(0,3) + '-' + phoneNumber.substring(3);
        }
        return "("+i.getAreaCode()+") "+phoneNumber;
    }

	synchronized public INTAPIClient getINTClient(String onWebSite) {
		if (INTAPIClients.containsKey(onWebSite)) return INTAPIClients.get(onWebSite);
		INTAPIClient newClient = new INTAPIClient(onWebSite);
		INTAPIClients.put(onWebSite, newClient);
		return newClient;
	}
	
	synchronized public void clearINTClient(String onWebSite) {
		INTAPIClients.remove(onWebSite);
	}

	public void onWebInterpreterAcceptVideo(InterpreterInfo interpreter) {
		if (LOG.isDebugEnabled()) LOG.debug("onWebInterpreterAcceptVideo: "+interpreter.email);
		TLVXManager.interpreterDAO.setVideo(interpreter.email, true, false);
	}
	
	public void onWebInterpreterAcceptVideoOnly(InterpreterInfo interpreter) {
		if (LOG.isDebugEnabled()) LOG.debug("onWebInterpreterAcceptVideo: "+interpreter.email);
		TLVXManager.interpreterDAO.setVideo(interpreter.email, true, true);
	}

	public void onWebInterpreterDontAcceptVideo(InterpreterInfo interpreter) {
		if (LOG.isDebugEnabled()) LOG.debug("onWebInterpreterDontAcceptVideo: "+interpreter.email);
		TLVXManager.interpreterDAO.setVideo(interpreter.email, false, false);
	}
	
	public void recordInterpreterMissedCall(Interpreter interpreter, Long callId, String language, String reason, Integer numberMissedCalls, Boolean loggedOut, Boolean manualDial, String callType) {
		InterpreterMissedCall imc = new InterpreterMissedCall();
		imc.setInterpreter(interpreter);
		imc.setMissedDate(new Date());
		imc.setCallId(callId);
		imc.setLanguage(language);
		imc.setReason(reason);
		imc.setManual(manualDial);
		imc.setCallType(callType);
		boolean reject = false;
		if (reason != null && (reason.equals("clicked reject button") || reason.equals("clicked hangup button") || reason.equals("clicked customer hangup (x)"))) {
			loggedOut = true;
			reject = true;
			numberMissedCalls = 0;
		}
		if (numberMissedCalls > -1 && numberMissedCalls < 10) imc.setCount(numberMissedCalls);
		imc.setReject(reject);
		imc.setLogout(loggedOut);
		imc.setEmailSent(false);
		TLVXManager.interpreterDAO.save(imc);
	}
}
