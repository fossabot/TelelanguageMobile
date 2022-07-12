package com.telelanguage.tlvx.service;

import org.apache.log4j.Logger;

import com.telelanguage.api.InterpreterInfo;
import com.telelanguage.api.TLVXAPIService;
import com.telelanguage.tlvx.model.Interpreter;

public class TLVXAPIServiceImpl implements TLVXAPIService {
	
	private static final Logger LOG = Logger.getLogger(TLVXAPIServiceImpl.class);

	public static InterpreterInfo convertToInterpreterInfo(Interpreter interpreter, Long callId) {
		if (interpreter == null) return null;
		InterpreterInfo ii = new InterpreterInfo();
		ii.interpreterId = interpreter.getInterpreterId();
		ii.accessCode = interpreter.getAccessCode();
		ii.activeSession = interpreter.getActiveSession();
		ii.areaCode = interpreter.getAreaCode();
		ii.email = interpreter.getEmail();
		ii.firstName = interpreter.getFirstName();
		ii.lastName = interpreter.getLastName();
		ii.phoneNumber = interpreter.getPhoneNumber();
		ii.webPhone = interpreter.getWebPhone();
		ii.webPhoneSipAddress = interpreter.getWebPhoneSipAddress();
		ii.onWebPage = interpreter.getOnWebSite();
		ii.allowVideo = interpreter.isAllowVideo();
		ii.videoEnabled = interpreter.isVideo();
		ii.videoOnly = interpreter.isVideoOnly();
		ii.forceVideoOnly = interpreter.isForceVideoOnly();
//		if (interpreter.getAllowedIPAddresses() != null && interpreter.getAllowedIPAddresses().length()>6) {
//			ii.allowedIPAddresses = interpreter.getAllowedIPAddresses();
//		}
		return ii;
	}
	
	@Override
	public InterpreterInfo findInterpreterByEmail(String email) {
		LOG.info(email);
		try {
			Interpreter interpreter = TLVXManager.interpreterDAO.findInterpreterByEmail(email);
			if (interpreter != null && (interpreter.isAllowVideo() || interpreter.isAllowWebphone())) {
				return convertToInterpreterInfo(TLVXManager.interpreterDAO.findInterpreterByEmail(email), null);
			} else {
				LOG.info("findInterpreterByEmail: "+email+" "+interpreter+" was denied.");
				return null;
			}
		}
		finally {
			TLVXManager.cleanupSession();
		}
	}

	@Override
	public synchronized void saveInterpreter(InterpreterInfo interpreter) {
		LOG.info(interpreter);
		try {
			Interpreter i = TLVXManager.interpreterDAO.findInterpreterByID(interpreter.interpreterId);
			if (i != null) {
				i.setActiveSession(interpreter.activeSession);
				i.setAreaCode(interpreter.areaCode);
				i.setPhoneNumber(interpreter.phoneNumber);
				i.setWebPhone(interpreter.webPhone);
				i.setWebPhoneSipAddress(interpreter.webPhoneSipAddress);
				i.setOnWebSite(interpreter.onWebPage);
				if (interpreter.resetNumMissedCalls) {
					i.setNumMissedCalls(0);
				}
				TLVXManager.getSession().save(i);
				if (interpreter.logon) {
					TLVXManager.interpreterDAO.markInterpreterOffCall(i);
				}
			}
		}
		finally {
			TLVXManager.cleanupSession();
		}
	}

	@Override
	public void markInterpreterOffCall(InterpreterInfo interpreter) {
		LOG.info(interpreter);
		try {
			Interpreter i = TLVXManager.interpreterDAO.findInterpreterByID(interpreter.interpreterId);
			if (i != null) {
				TLVXManager.interpreterDAO.markInterpreterOffCall(i);
			}
		}
		finally {
			TLVXManager.cleanupSession();
		}
	}

	@Override
	public Boolean checkCommunicationsOk(String thisServerUrl) {
		return Boolean.TRUE;
	}

	@Override
	public void hangupCall(InterpreterInfo interpreter) {
		LOG.info("Interpreter web hangupCall: "+interpreter);
		TLVXManager.callSessionManager.onCallSessionInterpreterDisconnect(""+interpreter.callId);
	}

	@Override
	public void acceptCall(InterpreterInfo interpreter) {
		LOG.info("Interpreter web acceptCall: "+interpreter);
		TLVXManager.callSessionManager.onWebInterpreterAcceptCall(interpreter);
	}

	@Override
	public void rejectCall(InterpreterInfo interpreter) {
		LOG.info("Interpreter web rejectCall callId: "+interpreter.callId);
		TLVXManager.callSessionManager.onWebInterpreterRejectCall(interpreter);
	}

	@Override
	public void requestAgent(InterpreterInfo interpreter) {
		LOG.info("Interpreter web requestAgent callId: "+interpreter.callId);
		TLVXManager.callSessionManager.onWebInterpreterRequestAgent(interpreter);
	}

	@Override
	public void acceptVideo(InterpreterInfo interpreter) {
		LOG.info("Interpreter web acceptVideo callId: "+interpreter.callId);
		TLVXManager.interpreterManager.onWebInterpreterAcceptVideo(interpreter);
	}
	
	@Override
	public void acceptVideoOnly(InterpreterInfo interpreter) {
		LOG.info("Interpreter web acceptVideoOnly callId: "+interpreter.callId);
		TLVXManager.interpreterManager.onWebInterpreterAcceptVideoOnly(interpreter);
	}

	@Override
	public void dontAcceptVideo(InterpreterInfo interpreter) {
		LOG.info("Interpreter web dontAcceptVideo callId: "+interpreter.callId);
		TLVXManager.interpreterManager.onWebInterpreterDontAcceptVideo(interpreter);
	}

	@Override
	public void playCustomerVideo(InterpreterInfo interpreter) {
		LOG.info("Interpreter web playCustomerVideo callId: "+interpreter.callId);
		TLVXManager.callSessionManager.onWebInterpreterRequestPlayCustomerVideo(interpreter);
	}

	@Override
	public void pauseCustomerVideo(InterpreterInfo interpreter) {
		LOG.info("Interpreter web pauseCustomerVideo callId: "+interpreter.callId);
		TLVXManager.callSessionManager.onWebInterpreterRequestPauseCustomerVideo(interpreter);
	}

	@Override
	public void videoSessionStarted(InterpreterInfo interpreter) {
		LOG.info("Interpreter web videoSessionStarted callId: "+interpreter.callId);
		TLVXManager.callSessionManager.onWebInterpreterVideoSessionStarted(interpreter);
	}

	@Override
	public void dialThirdParty(InterpreterInfo interpreter) {
		LOG.info("Interpreter web dialThirdParty callId: "+interpreter.callId + " " + interpreter.phoneNumber);
		//TLVXManager.callSessionManager.onWebInterpreterDialThirdParty(interpreter, phoneNumber);
		TLVXManager.callSessionManager.onCallSessionThirdpartyConnect(""+interpreter.callId, interpreter.phoneNumber);
	}

	@Override
	public void hangupThirdParty(InterpreterInfo interpreter) {
		System.out.println("Interpreter web hangupThirdParty callId: "+interpreter.callId + " " + interpreter.thirdPartyId);
		TLVXManager.callSessionManager.onCallSessionThirdpartyDisconnect(interpreter.thirdPartyId, ""+interpreter.callId);
	}
}
