package com.telelanguage.interpreter.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.telelanguage.api.InterpreterInfo;
import com.telelanguage.interpreter.model.InterpreterStatus;
import com.telelanguage.interpreter.server.MessageWebSocket;
import com.telelanguage.interpreter.util.SipUtil;

/**
 * InterpreterManager
 */
public class InterpreterManager
{
    private static final Logger LOG = Logger.getLogger(InterpreterManager.class);
    
    public static Integer maxInterpreterMissedCalls = 5;
    
	public boolean logon(InterpreterInfo interpreter, String email, String accessCode, String browserSessionId, String ipAddress, String hostname, String browser, String referer, String serverId) {
		if (LOG.isDebugEnabled()) LOG.debug("interpreter "+email+" log-on request");
		if (interpreter == null) {
			LOG.debug("interpreter "+email+" unknown");
			return false;
		} 
		
		if (!accessCode.startsWith("777")) accessCode = "777"+accessCode;
		LOG.debug("ac: "+accessCode+" i.ac: "+interpreter.accessCode);
		if (interpreter.allowedIPAddresses != null && interpreter.allowedIPAddresses.length() > 6 && !interpreter.allowedIPAddresses.contains(ipAddress)) {
			LOG.debug("Interpreter "+accessCode+" login was declined because ip address was not in allowedIPAddresses ip: "+ipAddress+ " allowedIps: "+interpreter.allowedIPAddresses);
			return false;
		}
		if (accessCode.equalsIgnoreCase(interpreter.accessCode)) {
			interpreter.logon = true;
			//interpreter.active
			interpreter.resetNumMissedCalls = true;
			interpreter.videoEnabled = false;
			interpreter.videoOnly = false;
			// TODO set phone number
        	//TLVXManager.interpreterDAO.save(interpreter);
			//sendQueueStatusUpdates();
			interpreter.onWebPage = TLVXManager.getProperties().getProperty("thisApiUrl");
			TLVXManager.tlvxClient.saveInterpreter(interpreter);
	        dispatchInterpreterMessage(interpreter, getInterpreterDetails(interpreter), "status");
			return true;
		}
		return false;
	}
	
    public synchronized void logout(String emailAddress) 
    {
    	if (LOG.isDebugEnabled()) LOG.debug("interpreter "+emailAddress+" log-off request");
    	if (emailAddress == null) return;
    	InterpreterInfo interpreter = TLVXManager.tlvxClient.findInterpreterByEmail(emailAddress);
        if (null != interpreter)
        {
        	interpreter.onWebPage = null;
        	interpreter.activeSession = false;
        	interpreter.webPhone = false;
        	interpreter.webPhoneSipAddress = null;
        	interpreter.videoEnabled = false;
			interpreter.videoOnly = false;
        	TLVXManager.tlvxClient.saveInterpreter(interpreter);
        }
        dispatchInterpreterMessage(interpreter, getInterpreterDetails(interpreter), "status");
        MessageWebSocket.sendMessageToEmail(interpreter.email, "Logout:");
        //sendQueueStatusUpdates();
    }
    
    public void setInterpreterStatus(InterpreterInfo interpreter, int status)
    {
    	if (LOG.isDebugEnabled()) LOG.debug("status = " + status + ", interpreter " + interpreter);
        if (null != interpreter)
        {
        	//TLVXManager.interpreterStatusDAO.setInterpreterStatus(interpreter, status);
            if (status == InterpreterStatus.INTERPRETER_STATUS_IDLE) {
            	if (LOG.isDebugEnabled()) LOG.debug("status = " + status + ", interpreter " + interpreter.interpreterId + " marking on_call=0");
            	TLVXManager.tlvxClient.markInterpreterOffCall(interpreter);
            } else {
            	if (LOG.isDebugEnabled()) LOG.debug("status = " + status + ", interpreter " + interpreter.interpreterId + " did not mark on_call = 0");
            }
        }
    }

	public void changeInterpreterStatus(String email, boolean online) {
    	if (LOG.isDebugEnabled()) LOG.debug("interpreter "+email+" changing active session to "+online);
    	InterpreterInfo interpreter = TLVXManager.tlvxClient.findInterpreterByEmail(email);
    	if (interpreter != null) {
	        interpreter.activeSession = online;
	        TLVXManager.tlvxClient.saveInterpreter(interpreter);
	        dispatchInterpreterMessage(interpreter, getInterpreterDetails(interpreter), "status");
    	}
	}

	public void handleInterpreterPhoneChange(String email, boolean webrtc, String phoneNumber) {
    	InterpreterInfo interpreter = TLVXManager.tlvxClient.findInterpreterByEmail(email);
    	if (interpreter!=null)
    	{
    		if (webrtc) {
    			
    		} else {
	    		if (!SipUtil.validateNumber(phoneNumber)) {
	    			throw new RuntimeException("Invalid number "+phoneNumber+" passed into agent phone number.");
	    		} else {
	    			// TODO what about international numbers?
	    			if (phoneNumber.length() == 10) {
		    			String areaCode = phoneNumber.substring(0, 3);
		    			String number = phoneNumber.substring(3, 10);
			    		LOG.info("interpreter changed phone number from "+interpreter.areaCode+" "+interpreter.phoneNumber+" to "+areaCode + " " + number);
			    		interpreter.areaCode = areaCode;
			    		interpreter.phoneNumber = number;
	    			} else {
	    				throw new RuntimeException("Invalid phone number length.");
	    			}
	    		}
    		}
    		TLVXManager.tlvxClient.saveInterpreter(interpreter);
    		dispatchInterpreterMessage(interpreter, getInterpreterDetails(interpreter), "status");
    	}
	}
	
	public Map<String, String> getInterpreterDetails(InterpreterInfo interpreter) {
		Map<String, String> data = new HashMap<String, String>();
    	data.put("name",interpreter.firstName+" "+interpreter.lastName);
    	if (interpreter.activeSession) {
    		data.put("active","Available");
    	} else {
    		data.put("active","Not Available");
    	}
    	if (interpreter.webPhone) {
    		data.put("number","Web Phone");
    	} else {
    		data.put("number",interpreter.areaCode+interpreter.phoneNumber);
    	}
		return data;
	}
	
	public void dispatchInterpreterMessage(InterpreterInfo interpreter, Map<String, String> data, String type) {
		//InterpreterServiceImpl.sendInterpreterMessage(interpreter, type+":"+data.toString());
		//System.out.println("dispatchInterpreterMessage "+type+": "+data.toString());
		MessageWebSocket.sendMessageToEmail(interpreter.email, type+":"+data.toString());
	}

	public boolean updateStatus(String email) {
    	InterpreterInfo interpreter = TLVXManager.tlvxClient.findInterpreterByEmail(email);
        dispatchInterpreterMessage(interpreter, TLVXManager.interpreterManager.getInterpreterDetails(interpreter), "status");
        return interpreter.videoEnabled;
	}

	public void setWebPhoneEnabled(String email, boolean enabled) {
    	InterpreterInfo interpreter = TLVXManager.tlvxClient.findInterpreterByEmail(email);
    	if (interpreter == null) return;
		interpreter.webPhone = enabled;
		interpreter.webPhoneSipAddress = "sip:"+interpreter.accessCode+"@"+TLVXManager.getProperties().getProperty("webPhoneSipAddressEnding");
		TLVXManager.tlvxClient.saveInterpreter(interpreter);
        dispatchInterpreterMessage(interpreter, TLVXManager.interpreterManager.getInterpreterDetails(interpreter), "status");
	}
}
