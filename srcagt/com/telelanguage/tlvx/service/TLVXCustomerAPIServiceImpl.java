package com.telelanguage.tlvx.service;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.icoa.voice.api.voxeo.VoxeoService;
import com.telelanguage.tlvx.model.CredentialsRequest;
import com.telelanguage.tlvx.model.CustomerOptionContent;
import com.telelanguage.tlvx.model.VriLinks;
import com.telelanguage.tlvx.model.VriUser;
import com.telelanguage.videoapi.BlueStreamConnected;
import com.telelanguage.videoapi.CredentialRequest;
import com.telelanguage.videoapi.CustomerHangupRequest;
import com.telelanguage.videoapi.CustomerHangupResponse;
import com.telelanguage.videoapi.CustomerLoginRequest;
import com.telelanguage.videoapi.CustomerLoginResponse;
import com.telelanguage.videoapi.CustomerLogoutRequest;
import com.telelanguage.videoapi.CustomerLogoutResponse;
import com.telelanguage.videoapi.CustomerRegistrationRequest;
import com.telelanguage.videoapi.CustomerVideoEstablished;
import com.telelanguage.videoapi.InterpreterRequest;
import com.telelanguage.videoapi.ScheduleInterpreterRequest;
import com.telelanguage.videoapi.TLVXCustomerAPIService;

public class TLVXCustomerAPIServiceImpl implements TLVXCustomerAPIService {
	
	private static final Logger LOG = Logger.getLogger(TLVXCustomerAPIServiceImpl.class);

	@Override
	public CustomerLoginResponse registerCustomer(CustomerRegistrationRequest request) {
		LOG.info("registerCustomer: "+request);
		return null;
	}
	
	public static String sha256(String base) {
	    try{
	        MessageDigest digest = MessageDigest.getInstance("SHA-256");
	        byte[] hash = digest.digest(base.getBytes("UTF-8"));
	        StringBuffer hexString = new StringBuffer();

	        for (int i = 0; i < hash.length; i++) {
	            String hex = Integer.toHexString(0xff & hash[i]);
	            if(hex.length() == 1) hexString.append('0');
	            hexString.append(hex);
	        }

	        return hexString.toString();
	    } catch(Exception ex){
	       throw new RuntimeException(ex);
	    }
	}

	@Override
	public CustomerLoginResponse loginCustomer(CustomerLoginRequest request) {
		LOG.info("loginCustomer: "+request);
		CustomerLoginResponse response = new CustomerLoginResponse();
		VriUser vriUser = null;
		if (request.loginToken != null && request.email == null && request.password == null) {
			vriUser = TLVXManager.userDAO.findVriUserByToken(request.loginToken);
		} else if (request.email != null && request.password != null) {
			vriUser = TLVXManager.userDAO.findVriUser(request.email);
			String passwordHash = sha256(request.password+vriUser.getSval());
			if (!passwordHash.equals(vriUser.getPassword())) {
				vriUser = null;
			}
		}
		VriLinks links = TLVXManager.userDAO.getVriLinks();
		LOG.info("links: "+links);
		response.marketingText = links.getMarketingText();
		response.forgotPasswordLink = links.getForgotPasswordLink();
		response.openAnAccountLink = links.getOpenAnAccountLink();
		response.alreadyCustomerLink = links.getAlreadyCustomerLink();
		response.preScheduleInterpreterLink = links.getPreScheduleInterpreterLink();
		if (vriUser != null) {
			if (request.loginToken != null) {
				vriUser.setLogin_token(request.loginToken);
				TLVXManager.userDAO.save(vriUser);
			}
			response.email = vriUser.getEmail();
			response.token = vriUser.getLogin_token();
			response.accessCode = vriUser.getCustomer().getCode();
			if (vriUser.getDepartment() != null) {
				response.deptCode = vriUser.getDepartment().getCode();
			}
			if (TLVXManager.companyManager.shouldAskDepartmentCode(vriUser.getCustomer().getCode())) {
				String deptLabel = vriUser.getCustomer().getDdc();
				String deptQuestion = vriUser.getCustomer().getDdn();
				if (deptLabel == null || deptLabel.length()<3) deptLabel = "Department";
				if (deptQuestion == null || deptQuestion.length()<3) deptQuestion = "Department";
				response.deptLabel = deptLabel;
				response.deptQuestion = deptQuestion;
			}
			List<CustomerOptionContent> contents = TLVXManager.customerOptionContentDAO.findByCustomerId(vriUser.getCustomer().getCustomerId());
			response.questionId = new ArrayList<String>();
			response.questionLabel = new ArrayList<String>();
			response.questionPlaceholder = new ArrayList<String>();
			int i=0;
			for (CustomerOptionContent content: contents) {
				response.questionId.add(""+i);
				response.questionLabel.add(content.getOptionLabel());
				String placeholder = content.getOptionContent();
				if (placeholder.contains("_")) placeholder = placeholder.substring(placeholder.indexOf("_")+1);
				response.questionPlaceholder.add(placeholder);
				i++;
			}
		}
		return response;
	}
	
	@Override
	public CustomerLogoutResponse logoutCustomer(CustomerLogoutRequest request) {
		VriUser vriUser = TLVXManager.userDAO.findVriUserByToken(request.loginToken);
		if (vriUser != null) {
			vriUser.setLogin_token(null);
		}
		return new CustomerLogoutResponse();
	}

	@Override
	public void requestInterpreter(InterpreterRequest request) {
		LOG.info("requestInterpreter: "+request);
		String ccxmlServer = TLVXManager.getProperties().getProperty("CCXMLHost");
		String tempSessionId = TLVXManager.grabNewUuid();
		TLVXManager.callSessionManager.addRequest(tempSessionId, request);
		VoxeoService.startNewSession(ccxmlServer, tempSessionId); // replace null with ccxml server with fewest calls?
		//TLVXManager.callSessionManager.createSession(ccxmlServer, request.sipUri);
	}
	
	@Override
	public CustomerHangupResponse hangupCustomer(CustomerHangupRequest request) {
		LOG.info("hangupCustomer: "+request);
		TLVXManager.callSessionManager.onCustomerClickedHangup(request.callSessionId);
		return null;
	}

	public static void main(String[] args) {
		String sval="20170414095010";
		String password="497acf478a490fca4925b8041232fbc0c1a48707a713cb378010ad8f9e7f6c59";
		String passwordHash = sha256("56745"+sval);
		if (password.equals(passwordHash)) System.out.println("Matches");
		System.out.println(passwordHash);
	}

	@Override
	public void videoSessionStarted(CustomerVideoEstablished request) {
		LOG.info("videoSessionStarted: "+request);
		TLVXManager.callSessionManager.onCustomerVideoSessionStarted(request.callSessionId);
	}

	@Override
	public void requestCredentials(CredentialRequest request) {
		LOG.info("requestCredentials: "+request);
		CredentialsRequest cr = new CredentialsRequest();
		cr.setCreatedAt(new Date());
		cr.setEmail(request.email);
		cr.setIsCustomer(request.custString);
		cr.setPhone(request.phone);
		cr.setName(request.name);
		cr.setOrg(request.org);
		cr.setUpdatedAt(new Date());
		cr.setEmailSent(false);
		TLVXManager.customerDAO.save(cr);
		TLVXManager.commit();
	}

	@Override
	public void scheduleInterpreter(ScheduleInterpreterRequest request) {
		LOG.info("scheduleInterpreter: "+request);
		com.telelanguage.tlvx.model.ScheduleInterpreterRequest sir = new com.telelanguage.tlvx.model.ScheduleInterpreterRequest();
		sir.setAccessCode(request.accessCode);
		sir.setVriLoginEmail(request.videoEmail);
		sir.setEmail(request.email);
		sir.setName(request.name);
		sir.setPhone(request.phone);
		sir.setOrg(request.org);
		sir.setLanguage(request.languageString);
		sir.setVideoOrPhone(request.typeString);
		sir.setScheduleTime(request.date);
		sir.setCreatedAt(new Date());
		sir.setUpdatedAt(new Date());
		sir.setTimezoneOffsetMinutes(request.timezone);
		sir.setEmailSent(false);
		TLVXManager.customerDAO.save(sir);
		TLVXManager.commit();
	}

	@Override
	public void bluestreamConnected(BlueStreamConnected request) {
		LOG.info("bluestreamConnected: "+request);
		TLVXManager.callSessionManager.onCustomerBluestreamConnected(request.callSessionId, request.bsCallId, request.bsInterpreterId, request.bsInterpreterName);
	}

	@Override
	public void bluestreamAgentRequest(BlueStreamConnected request) {
		LOG.info("bluestreamAgentRequest: "+request);
		
	}

	@Override
	public void bluestreamDisconnected(BlueStreamConnected request) {
		LOG.info("bluestreamDisconnected: "+request);
		
	}
}
