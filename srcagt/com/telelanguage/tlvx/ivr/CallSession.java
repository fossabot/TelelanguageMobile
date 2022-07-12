package com.telelanguage.tlvx.ivr;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import com.telelanguage.api.InterpreterCallInfo;
import com.telelanguage.api.InterpreterInfo;
import com.telelanguage.tlvx.client.AgentMessages;
import com.telelanguage.tlvx.ivr.Connection.ConnectionState;
import com.telelanguage.tlvx.model.Agent;
import com.telelanguage.tlvx.model.Call;
import com.telelanguage.tlvx.model.CallDetailRecord;
import com.telelanguage.tlvx.model.CallEvent;
import com.telelanguage.tlvx.model.CallInformation;
import com.telelanguage.tlvx.model.Customer;
import com.telelanguage.tlvx.model.CustomerDNIS;
import com.telelanguage.tlvx.model.CustomerDepartment;
import com.telelanguage.tlvx.model.CustomerOption;
import com.telelanguage.tlvx.model.CustomerOptionData;
import com.telelanguage.tlvx.model.Department;
import com.telelanguage.tlvx.model.Interpreter;
import com.telelanguage.tlvx.model.InterpreterActivity;
import com.telelanguage.tlvx.model.InterpreterLine;
import com.telelanguage.tlvx.model.InterpreterStatus;
import com.telelanguage.tlvx.model.Language;
import com.telelanguage.tlvx.model.QuestionItem;
import com.telelanguage.tlvx.model.ThirdPartyActivity;
import com.telelanguage.tlvx.server.AgentServiceImpl;
import com.telelanguage.tlvx.service.AgentManager;
import com.telelanguage.tlvx.service.InterpreterManager;
import com.telelanguage.tlvx.service.TLVXAPIServiceImpl;
import com.telelanguage.tlvx.service.TLVXManager;
import com.telelanguage.tlvx.util.SipUtil;
import com.telelanguage.tlvx.util.TimeFormat;
import com.telelanguage.videoapi.BlueStreamRequest;
import com.telelanguage.videoapi.InterpreterRequest;
import com.telelanguage.videoapi.VideoCallInfo;
import com.telelanguage.videoapi.VideoCustomerInfo;

/**
 * CallSession
 */
public class CallSession implements Serializable {
	private static final long serialVersionUID = 4041302312250116581L;
	private static final Logger LOG = Logger.getLogger(CallSession.class);
	private static final int MAX_INTERPRETER_ATTEMPTS = 3;
	// private static final int MAX_INTERPRETERS_SHOW = 30;

	private Date started;
	private Date holdTime;
	private Connection customerConnection;
	private Connection agentConnection;
	private Connection interpreterConnection;
	private Map<String, Connection> thirdPartyConnections = new HashMap<String, Connection>();
	private Connection transferConnection;
	private String ccxmlServer;
	private String sessionId;
	private Long agentId;
	private Interpreter interpreter;
	private Interpreter previousInterpreter;
	private String interpreterLanguage;
	private String previousInterpreterLanguage;
	private String previousInterpreterGender;
	private Boolean previousInterpreterVideo;
	private boolean manualInterpreterDial;
	private int interpreterAttempts;
	private boolean agentRejected = false;
	private boolean requeueCall = false;
	private boolean interpreterValidated = false;
	private boolean dialingCustomer = false;
	private boolean dialingAgent = false;
	private boolean dialingInterpreter = false;
	private boolean dialingThirdParty = false;
	private boolean dialingTransfer = false;
	private boolean completeCallNoInterpreters = false;
	private boolean completeCallPressed = false;
	private boolean recordingStarted = false;
	private Call call;
	private Customer customer;
	private CustomerDNIS customerDNIS;
	private Conference conference;
	private Queue<Dialog> dialogQueue = new ConcurrentLinkedQueue<Dialog>();
	private Map<String, Dialog> currentDialogs = new ConcurrentHashMap<String, Dialog>();
	private Map<String, Connection> connections = new ConcurrentHashMap<String, Connection>();
	private Dialog processingDialog;
	private boolean destroyed;
	private List<Interpreter> interpreterHistory = new LinkedList<Interpreter>();
	private ArrayList<CustomerOptionData> optionData;
	private boolean interpreterOnline;
	private boolean interpreterBrowserSound = false;
	private boolean videoRequested = false;
	
	private InterpreterRequest videoInterpreterRequest;

	private Dialog callerOnHoldDialog;
	private boolean callerOnHold;
	private Dialog callerInitialDialog;
	private Dialog interpreterDialog;
	private Dialog interpreterOnHoldDialog;
	private boolean interpreterOnHold;
	private Map<String, Dialog> thirdPartyOnHoldDialogs = new ConcurrentHashMap<String, Dialog>();
	private Dialog ivrDialog;
	private Dialog interpreterPoundDialog;

	private String thirdPartyDestination;
	private String transferDestination;
	
	private String sipCallId;
	private String dtmfPin = null;

	private boolean ivrDialogStarted = false; // added this check/flag for teleauto compatibility call recording
	private String callRecordingChannelId;
	private String callRecordingIpAddress;
	private String callRecordingAni;
	private String callRecordingDnis;
	
	private Date interpreterStartTime = null;
	private boolean interpreterBillable = false;
	private Map<String, Date> thirdPartyStartTimes = new ConcurrentHashMap<String, Date>();
	private boolean videoStarting = false;
	private String rejectReason = null;
	
	private boolean bluestream = false;
	private String bsInterpreterId = null;
	private String bsCallId = null;
	
	private String interpreterJoinAction = null;
	private String interpreterJoinActionPhoneNumber = null;
	
	private class WebPhoneInterpreterTimeout extends TimerTask {
		@Override
		public void run() {
			LOG.info("WebPhoneInterpreterTimeout.run()");
			InterpreterInfo ii = new InterpreterInfo();
			ii.rejectReason = "accept timeout (s)";
			rejectInterpreterFromWeb(ii);
		}
	}
	private WebPhoneInterpreterTimeout webphoneTimeout;
	private Timer webphoneRejectTimer;

	public CallSession(String ccxmlServer, String sessionId, CallSessionManager callSessionManager, Connection caller,String connectionFrom, String sipCallId) {
		this.started = new Date();
		this.holdTime = new Date();
		this.ccxmlServer = ccxmlServer;
		this.sessionId = sessionId;
		this.customerConnection = caller;
		this.sipCallId = sipCallId;
		
		if (caller != null) {
			connections.put(caller.getConnectionId(), caller);
			customerConnection.setState(Connection.ConnectionState.CONNECTING);
			if (LOG.isDebugEnabled())
				LOG.debug("finding customer dnis for " + customerConnection);
			customerDNIS = callSessionManager.findCustomerDNIS(customerConnection);
			customer = callSessionManager.findCustomer(customerDNIS, customerConnection);
			call = new Call();
			call.setCallSessionId(sessionId);
			call.setStartDate(new Date());
			call.setAccessCode(customer != null ? customer.getCode() : null);
			call.setCustomer(customer != null ? customer.getCustomerId() : null);
			call.setCcxmlServer(ccxmlServer);
			call = TLVXManager.callDAO.save(call);
		} else {
			call = new Call();
			call.setCallSessionId(sessionId);
			call.setStartDate(new Date());
			call.setStatus(CallSessionManager.CALL_STATUS_OUTBOUND_READY);
			call.setCcxmlServer(ccxmlServer);
			//call.setVideo(true);  // can it not be video?
			call = TLVXManager.callDAO.save(call);
		}

		try {
			if (connectionFrom != null) {
				StringTokenizer st = new StringTokenizer(connectionFrom, ";");
				while (st.hasMoreTokens()) {
					String token = st.nextToken();
					if (token.indexOf("channel=channel") != -1) {
						callRecordingChannelId = token.substring(15);
					} else if (token.indexOf("<sip:") != -1) {
						StringTokenizer sipTokenizer = new StringTokenizer(
								token, "@");
						while (sipTokenizer.hasMoreTokens()) {
							String sipToken = sipTokenizer.nextToken();
							if (sipToken.indexOf(":") > -1)
								callRecordingIpAddress = sipToken.substring(0,
										sipToken.indexOf(":"));
							else if (sipToken.indexOf(">") > -1)
								callRecordingIpAddress = sipToken.substring(0,
										sipToken.indexOf(">"));
						}
					}
				}
				if (customerConnection != null) {
					callRecordingAni = customerConnection.getOrigination();
					callRecordingDnis = customerConnection.getDestination();
				}
			}
		} catch (Exception e) {
			if (LOG.isDebugEnabled())
				LOG.debug(
						"Error occurred trying to parse SIP URL for recording, recording now disabled",
						e);
		}

		if (LOG.isDebugEnabled())
			LOG.debug("Call recording info, callRecordingChannelId = "
					+ callRecordingChannelId + ", callRecordingIpAddress"
					+ callRecordingIpAddress + ", callRecordingAni = "
					+ callRecordingAni + ", callRecordingDnis = "
					+ callRecordingDnis);
	}

	public String toString() {
		String callId = "Unknown";
		if (call!= null) callId = call.getCallSessionId();
		return "[CallSession-" + sessionId + ", callid: "+callId + ccxmlServer
				+ ", customer = " + customer + "]";
	}

	public Date getStartTime() {
		return this.started;
	}

	public void setAgentConnection(Connection connection) {
		this.agentConnection = connection;
	}

	public void setInterpreterConnection(Connection connection) {
		this.interpreterConnection = connection;
	}

	public String getId() {
		return sessionId;
	}

	public void setAgent(Long agentId) {
		if (LOG.isDebugEnabled())
			LOG.debug("set agent request, agentId = " + agentId);

		if (destroyed == true) {
			return;
		}

		this.agentId = agentId;
		Agent agent = getAgent(agentId);
		call = TLVXManager.callDAO.findById(call.getId());
		call.setAgent(agent);
		call.setStatus(CallSessionManager.CALL_STATUS_FORWARDED_TO_AGENT);
		call.setLastAnsweredDate(new Date());
		call = TLVXManager.callDAO.save(call);

		Map<String, String> parameters = buildSessionDataObject();
		parameters.put("dest", agent.getSipUri());

		if (null != customerConnection) {
			parameters.put("callerid", customerConnection.getOrigination());
		}

		try {
			dialingAgent = true;
			agentRejected = false;
			parameters.put("timeout", TLVXManager.callSessionManager.getAgentTimeout());
			//parameters.put("connectionid", "agent");
			TLVXManager.ccxmlManager.createCall(parameters);
		} catch (Exception e) {
			LOG.warn("Exception caught trying to create call", e);
			handleServerError();
		}

		Map data = new HashMap();
		data.put("hold_time",
				TimeFormat.valueOf(new Date().getTime() - holdTime.getTime()));
		data.put("requeue", requeueCall);
		data.put("nointerpreters", completeCallNoInterpreters);

		String company = "";

		if (null != customerConnection) {
			if (null != customer) {
				company = customer.getName();
			}
		}

		data.put("ani", ""); //getAni()); // hide ani from agent
		data.put("company", company);
		data.put("priority", ""+call.isPriorityCall());
		data.put("video", ""+call.isVideo());
		data.put("callid", call.getId());
		if (call.getLastReason() != null)
			data.put("lastReason", call.getLastReason());

		dispatchAgentMessage(data, AgentManager.TL_CALL_INIT);

		if (null != customerConnection) {
			data = new HashMap();
			data.put("status", "connected");
			data.put("ani", "");//getAni());
			data.put("company", company);
			data.put("muted", customerConnection.isMuted());
			dispatchAgentMessage(data, AgentManager.TL_CUSTOMER_STATUS_CHANGE);
		}

		if (null != interpreter
				&& null != interpreterConnection
				&& interpreterConnection.getState() == Connection.ConnectionState.CONNECTED) {
			data = new HashMap();
			data.put("status", "connected");
			data.put("muted", interpreterConnection.isMuted());
			if (interpreter != null) {
				data.put("name", interpreter.getFirstName() + " " + interpreter.getLastName());
			}
			dispatchAgentMessage(data,
					InterpreterManager.TL_INTERPRETER_STATUS_CHANGE);
		}

		for (Connection thirdPartyConnection : thirdPartyConnections.values()) {
			if (thirdPartyConnection.getState() == Connection.ConnectionState.CONNECTED) {
				data = new HashMap();
				data.put("id", thirdPartyConnection.getConnectionId());
				data.put("status", "connected");
				data.put("muted", thirdPartyConnection.isMuted());
				data.put("name", SipUtil.getPhoneNumber(thirdPartyConnection.getDestination()));
				dispatchAgentMessage(data, AgentManager.TL_THIRDPARTY_STATUS_CHANGE);
			}
		}
	}

	private String getDnis() {
		String dnis = "unknown";
		if (null != customerConnection) {
			if (dialingCustomer == false) {
				dnis = customerConnection.getDestination();
			} else {
				dnis = customerConnection.getOrigination();
			}

			if (dnis != null && dnis.length() > 0) {
				dnis = SipUtil.getPhoneNumber(dnis);
			}
		}
		return dnis;
	}

	private String getAni() {
		String ani = "blocked";
		if (null != customerConnection) {
			if (videoInterpreterRequest != null && videoInterpreterRequest.videoCustomerInfo != null && videoInterpreterRequest.videoCustomerInfo.email != null) {
				ani = videoInterpreterRequest.videoCustomerInfo.email;
			} else {
				if (dialingCustomer == false) {
					ani = customerConnection.getOrigination();
				} else {
					ani = customerConnection.getDestination();
				}
	
				if (ani != null && ani.length() > 0) {
					ani = SipUtil.getPhoneNumber(ani);
				}
			}
		}

		return ani;
	}

	public CallInformation loadCallInformation() {
		Call call = TLVXManager.callDAO.findById(this.call.getId());
		CallInformation callInformation = new CallInformation();
		callInformation.callId = "" + call.getId();
		callInformation.company = "";
		callInformation.accessCode = "";
		callInformation.customerId = "";
		callInformation.language = call.getLanguage();
		callInformation.interpreterGender = previousInterpreterGender;
		callInformation.interpreterVideo = previousInterpreterVideo;
		callInformation.questions = new ArrayList<QuestionItem>();
		callInformation.departments = new ArrayList<Department>();
		callInformation.lastReason = call.getLastReason();
		if (customer != null) {
			LOG.debug("customerDeptVar = " + customer.getDeptVar());
			callInformation.deptVar = customer.getDeptVar();
			if (customer.getName() != null
					&& !customer.getName().equals("null"))
				callInformation.company = customer.getName();
			if (customer.getCode() != null
					&& !customer.getCode().equals("null"))
				callInformation.accessCode = customer.getCode();
			if (customer.getCustomerId() != null
					&& !customer.getCustomerId().equals("null"))
				callInformation.customerId = customer.getCustomerId();
			if (callInformation.language == null
					|| callInformation.language.length() == 0)
				if (previousInterpreterLanguage != null
						&& previousInterpreterLanguage.length() > 0)
					callInformation.language = previousInterpreterLanguage;
				else {
					String language = TLVXManager.callSessionManager
							.findCustomerLanguage(customerDNIS);
					if (null != language && language.length() > 0)
						callInformation.language = language;
					else if (call.getLanguage() != null
							&& !call.getLanguage().equals("null")
							&& call.getLanguage().length() > 0)
						callInformation.language = call.getLanguage();
				}
			System.out.println(">>>>>> language = " + callInformation.language);
			callInformation.questions = TLVXManager.companyManager
					.getAdditionalQuestions("" + call.getId(),
							customer.getCustomerId());
			//callInformation.askDepartment = false;
			callInformation.askDepartmentText = customer.getDdn();
			if (call.getDepartmentCode() != null
					&& !call.getDepartmentCode().equals("null"))
				callInformation.departmentCode = call.getDepartmentCode();
			else {
				if (null != customerDNIS) {
					CustomerDepartment department = TLVXManager.customerDepartmentDAO
							.findByDepartmentId(customerDNIS.getDepartmentId());
					if (null != department) {
						call.setDepartmentCode(department.getCode());
						if (department.getCode() != null
								&& !department.getCode().equals("null"))
							callInformation.departmentCode = department
									.getCode();
					}
//					if (false == customerDNIS.isAskDepartment()) {
//						callInformation.askDepartment = false;
//					}
				}
			}
			callInformation.departments = TLVXManager.companyManager
					.getDepartments(customer.getCustomerId());
			callInformation.hasDepartments = callInformation.departments != null && callInformation.departments.size()>0;
		}
		LOG.debug("set call data to = " + callInformation);
		return callInformation;
	}

	public CallInformation setCustomer(String customerId) {
		if (LOG.isDebugEnabled())
			LOG.debug("set customer request, customerId = " + customerId);

		customer = TLVXManager.customerDAO.findById(customerId);

		if (null != customer) {
			call = TLVXManager.callDAO.findById(call.getId());
			call.setCustomer(customer.getCustomerId());
			call.setSubscriptionCode(customer.getSubscriptionCode());
			call = TLVXManager.callDAO.save(call);
			return loadCallInformation();
		}
		return null;
	}

	public CallInformation setAccessCode(String code) {
		if (LOG.isDebugEnabled())
			LOG.debug("set access code request, code = " + code);

		customer = TLVXManager.customerDAO.findByCode(code);

		if (null != customer) {
			call = TLVXManager.callDAO.findById(call.getId());
			call.setAccessCode(code);
			call.setCustomer(customer.getCustomerId());
			call.setSubscriptionCode(customer.getSubscriptionCode());
			call = TLVXManager.callDAO.save(call);
			return loadCallInformation();
		} else {
			if (LOG.isDebugEnabled())
				LOG.debug("No customer found for code " + code);
			return null;
		}
	}

	public void startSession(Long savedCallId) {
		if (LOG.isDebugEnabled())
			LOG.debug("startSession " + savedCallId);

		if (null != savedCallId) {
			call = TLVXManager.callDAO.findById(call.getId());
			call.setCustomer(customer.getCustomerId());
			call.setSubscriptionCode(getSubscriptionCode());
			call = TLVXManager.callDAO.save(call);
		}

		Map parameters = buildSessionDataObject();
		parameters.put("confname", "CONF-" + sessionId);
		try {
			TLVXManager.ccxmlManager.createConference(parameters);
		} catch (Exception e) {
			LOG.warn("Exception caught trying to create conference", e);
			handleServerError();
		}
	}

	private Map buildSessionDataObject() {
		Map sessionDataObject = new HashMap();
		if (sessionId == null) sessionId = call.getCallSessionId();
		if (ccxmlServer == null) ccxmlServer = call.getCcxmlServer();
		sessionDataObject.put("sessionid", sessionId);
		sessionDataObject.put("remoteAddress", ccxmlServer);

		return sessionDataObject;
	}

	public void setPreviousLanguage(String language) {
		if (LOG.isDebugEnabled())
			LOG.debug("setPreviousLanguage called with language = " + language);
		if (language != null && !"".equals(language))
			previousInterpreterLanguage = language;
	}

	public List<InterpreterLine> getInterpretersAvailable() {
		if (LOG.isDebugEnabled())
			LOG.debug("sendInterpreterHistory called subscription = "
					+ call.getSubscriptionCode() + ", language = "
					+ previousInterpreterLanguage + ", video = "+previousInterpreterVideo);

		List<InterpreterLine> available = new ArrayList<InterpreterLine>();
		for (Object result[] : TLVXManager.interpreterBlackListDAO
				.removeBlackListedInterpreters(call.getAccessCode(),
						TLVXManager.interpreterDAO.findAllByLanguageSearch(
								call.getSubscriptionCode(),
								previousInterpreterLanguage,
								previousInterpreterGender,
								previousInterpreterVideo))) {
			Interpreter i = (Interpreter) result[0];
			String name = "";
			String note = "";
			if (i.getNote() != null && !"".equals(i.getNote())) {
			  note = " " + i.getNote();
			}
			if (i.getGender() != null && !"".equals(i.getGender())) {
				name = i.getFirstName() + " " + i.getLastName() + " ("
						+ i.getGender() + ")" + note;
			} else {
				name = i.getFirstName() + " " + i.getLastName();
				if (i.getNote() != null && !"".equals(i.getNote())) {
				  name += " - " + i.getNote();
				}
			}
			InterpreterLine obj = new InterpreterLine(i.getInterpreterId(),
					name, TLVXManager.interpreterManager.getPhoneType(i),
					i.getActiveSession() && !i.getOnCall(), i.getAccessCode());
			available.add(obj);
		}
		return available;
	}

	public List<InterpreterLine> getInterpreterHistory() {
		List<InterpreterLine> interpreterHistoryList = new ArrayList<InterpreterLine>();
		for (Interpreter interpreter : interpreterHistory) {
			InterpreterLine history = new InterpreterLine(
					interpreter.getInterpreterId(), interpreter.getFirstName()
							+ " " + interpreter.getLastName(),
					TLVXManager.interpreterManager.getPhoneNumber(interpreter),
					false, interpreter.getAccessCode());
			interpreterHistoryList.add(history);
		}
		return interpreterHistoryList;
	}

	private void dispatchAgentMessage(Map data, String type) {
		if (null != agentId) {
			Agent agent = getAgent(agentId);
			if (LOG.isDebugEnabled())
				LOG.debug("dispatching message to agent = " + agent
						+ ", data = " + data);
			TLVXManager.agentManager.dispatchAgentMessage(agent, data, type);
		} else
			LOG.warn("dispatchAgentMessage called with data=" + data
					+ ", type=" + type
					+ " -- however, no agent is connected for " + this);
	}

	private Agent getAgent(Long agentId) {
		return TLVXManager.agentDAO.findById(agentId);
	}

	public synchronized void onConnectionProgressing(Map request) {
		if (LOG.isDebugEnabled())
			LOG.debug("onConnectionProgressing received");

		if (true == dialingAgent) {
				if (true == dialingAgent) {
					String connectionId = (String) request.get("connectionid");
					String origination = (String) request.get("connectionLocal");
					String destination = (String) request.get("connectionRemote");

					agentConnection = new Connection(connectionId, destination, origination);
					agentConnection.setState(Connection.ConnectionState.CONNECTING);
					connections.put(connectionId, agentConnection);
			}
		} else if (true == dialingCustomer) {
			String connectionId = (String) request.get("connectionid");
			String origination = (String) request.get("connectionLocal");
			String destination = (String) request.get("connectionRemote");
			sipCallId = (String) request.get("sipCallId");

			customerConnection = new Connection(connectionId, destination, origination);
			customerConnection.setState(Connection.ConnectionState.CONNECTING);
			connections.put(connectionId, customerConnection);
		} else if (true == dialingInterpreter) {
			if (true == dialingInterpreter) {
				String connectionId = (String) request.get("connectionid");
				String origination = (String) request
						.get("connectionLocal");
				String destination = (String) request.get("connectionRemote");

				interpreterConnection = new Connection(connectionId, destination, origination);
				interpreterConnection.setState(Connection.ConnectionState.CONNECTING);
				connections.put(connectionId, interpreterConnection);
				
				if (!isConnected(customerConnection)) {
					// if the customer is hung up, lets lean up this call.
					LOG.debug("customer hung up during interpreter outdial command, end the outdial.");
					hangupConnection(interpreterConnection);
				}
			}
		} else if (true == dialingThirdParty) {
			String connectionId = (String) request.get("connectionid");
			String origination = (String) request.get("connectionLocal");
			String destination = (String) request.get("connectionRemote");

			Connection thirdPartyConnection = new Connection(connectionId, destination, origination);
			thirdPartyConnection.setState(Connection.ConnectionState.CONNECTING);
			connections.put(connectionId, thirdPartyConnection);
			thirdPartyConnections.put(connectionId, thirdPartyConnection);
			AgentServiceImpl.sendAgentMessage(getAgent(agentId), "TlThirdpartyStatusChange:{status=dialing,id="+connectionId+"}");
		} else if (true == dialingTransfer) {
			String connectionId = (String) request.get("connectionid");
			String origination = (String) request.get("connectionLocal");
			String destination = (String) request.get("connectionRemote");

			transferConnection = new Connection(connectionId, destination, origination);
			transferConnection.setState(Connection.ConnectionState.CONNECTING);
			connections.put(connectionId, transferConnection);
		} else {
			if (LOG.isDebugEnabled())
				LOG.debug("ignoring onConnectionProgressing event, must have already connected");
		}
	}

	public synchronized void onConnectionConnected(Map request) {
		try {
			if (LOG.isDebugEnabled())
				LOG.debug("onConnectionConnected received");

			String connectionId = (String) request.get("connectionid");
			if (dialingAgent || (null != agentConnection && connectionId.equals(agentConnection.getConnectionId()))) {
				if (null == agentConnection) {
					if (LOG.isDebugEnabled())
						LOG.debug("Voxeo sucks for giving connected event before progressing.");
					String origination = (String) request .get("connectionLocal");
					String destination = (String) request .get("connectionRemote");
					agentConnection = new Connection(connectionId, destination, origination);
					agentConnection .setState(Connection.ConnectionState.CONNECTING);
					connections.put(connectionId, agentConnection);
				}

				requeueCall = false;

				if (callerInitialDialog != null) {
					if (callerInitialDialog.getStarted())
						terminateDialog(callerInitialDialog);
					else
						callerInitialDialog.setFinished(true);
				}

				if ((callerOnHold || null != callerOnHoldDialog) && null == previousInterpreterLanguage) {
					callerOffHold();
				}

				if (interpreterOnHold) {
					interpreterOffHold();
				}

				for (String thirdPartyOnHoldDialogKey : thirdPartyOnHoldDialogs.keySet()) {
					LOG.debug("thirdPartyOnHoldDialog connectionId key = "+thirdPartyOnHoldDialogKey);
					thirdPartyOffHold(thirdPartyOnHoldDialogKey);
				}

				agentConnection.setState(Connection.ConnectionState.CONNECTED);

				dialingAgent = false;
				completeCallPressed = false;
				ivrDialogStarted = false;

				Map data = new HashMap();
				data.put("callid", call.getId());
				dispatchAgentMessage(data, AgentManager.TL_CALL_START);

				call = TLVXManager.callDAO.findById(call.getId());
				call.setAgent(getAgent(agentId));
				call.setReservedAgent(null);
				call.setStatus(CallSessionManager.CALL_STATUS_AGENT_ANSWERED);
				call = TLVXManager.callDAO.save(call);

				TLVXManager.agentManager.sendQueueStatusUpdates();

				TLVXManager.agentDAO.updateAgentLastCall(agentId);

				saveCallEvent(CallEvent.AGENT_CONNECT, String.valueOf(agentId));

				if (null == customerConnection
						|| customerConnection.getState() != Connection.ConnectionState.CONNECTED) {
					processCustomerDisconnect(false,
							"On Agent Connect: customer no longer on the line.");
				} else {
					conferenceConnection(agentConnection, "full", "*", null);
				}
				if (!recordingStarted
						&& !TLVXManager.subscribedClientsDIDDAO
								.isDontRecordCall(customerConnection
										.getDestination())) {
					recordingStarted = true;
					confRecordingStart();
				}
			} else if (null != customerConnection && connectionId.equals(customerConnection.getConnectionId())) {
				if (null == customerConnection) {
					if (LOG.isDebugEnabled())
						LOG.debug("Voxeo sucks for giving customer connected event before progressing.");
					String origination = (String) request .get("connectionLocal");
					String destination = (String) request .get("connectionRemote");
					customerConnection = new Connection(connectionId, destination, origination);
					customerConnection .setState(Connection.ConnectionState.CONNECTING);
					connections.put(connectionId, customerConnection);
				}
				customerConnection.setState(Connection.ConnectionState.CONNECTED);
				saveCallEvent(CallEvent.CUSTOMER_CONNECT, getAni(), getDnis());

				if (!TLVXManager.subscribedClientsDIDDAO.isDontRecordCall(customerConnection.getDestination())) {
					callRecordingStartRecording();
				}
				Map parameters;
				if (false == dialingCustomer) {
					// determine the customer
					call = TLVXManager.callDAO.findById(call.getId());
					call.setPriorityCall(isPriorityCall());
					call.setSubscriptionCode(getSubscriptionCode());
					call = TLVXManager.callDAO.save(call);

					boolean ivrEnabled = TLVXManager.subscribedClientsDIDDAO
							.isIVREnabled(customerConnection.getDestination());
					LOG.debug("GOT HERE 1, SC isIVREnabled = " + ivrEnabled + ", customDNIS = "+customerConnection
							.getDestination());
					if (customerDNIS == null && ivrEnabled) {
						LOG.debug("GOT HERE 2: Customer DNI is NULL AND SC isIVREnabled is TRUE");
						String src = TLVXManager.callSessionManager.getTeleAutoURI() + "?";
						String greetingWave = TLVXManager.subscribedClientsDIDDAO
								.getGreetingWave(customerConnection.getDestination());
						String greetingSentence = TLVXManager.subscribedClientsDIDDAO
								.getGreetingSentence(customerConnection.getDestination());

						if (greetingWave != null && greetingWave.length()>0) {
							String promptURL = TLVXManager.callSessionManager
									.getCustomerPromptBaseURL()
									+ "/"
									+ greetingWave;
							src += "promptURL=" + promptURL + "&";
						} else if (greetingSentence != null) {
							String promptText = greetingSentence;
							src += "promptText=" + promptText + "&";
						}

						String subscriptionCode = TLVXManager.subscribedClientsDIDDAO
								.getSubscriptionCode(customerConnection
										.getDestination());
						if (subscriptionCode != null) {
							src += "subscriptionCode=" + subscriptionCode + "&";
						}

						parameters = buildSessionDataObject();
						parameters.put("src", src);
						parameters.put("connectionid", customerConnection.getConnectionId());
						ivrDialog = new Dialog(parameters);
						ivrDialogStarted = true; // added for teleauto
													// compatibility
						startDialog(ivrDialog);
						call.setStatus(CallSessionManager.CALL_STATUS_IVR);
						call = TLVXManager.callDAO.save(call);
						saveCallEvent(CallEvent.IVR_CONNECT, null, null);
					} else if (customerDNIS != null
							&& customerDNIS.isIvrEnabled() && customer != null) {
						LOG.debug("Customer DNIS is NOT NULL AND Customer isIVREnabled is TRUE and Customer is NOT NULL");

						parameters = buildSessionDataObject();
						String src = TLVXManager.callSessionManager
								.getTeleAutoURI() + "?";

						if (false == customerDNIS.isAskDepartment()
								&& null != customerDNIS.getDepartmentId()) {
							CustomerDepartment department = TLVXManager.customerDepartmentDAO
									.findByDepartmentId((customerDNIS
											.getDepartmentId()));
							if (null != department) {
								src += "deptcode=" + department.getCode() + "&";
							}
						} else if (false == customerDNIS.isAskDepartment()) {
							src += "deptcode=dontask&";
						}
						
						if (false == customerDNIS.isAskDtmfPin()) {
							src += "dtmfpin=dontask&";
						}

						if (false == customerDNIS.isAskLanguage()
								&& null != customerDNIS.getLanguageId()) {
							String language = TLVXManager.callSessionManager
									.findCustomerLanguage(customerDNIS);
							if (null != language) {
								src += "language=" + language + "&";
							}
						}

						if (false == customer.isAskCode()
								&& null != customer.getCode()) {
							src += "accesscode=" + customer.getCode() + "&";
						}

						if (customer.getGreetingWave() != null && customer.getGreetingWave().length()>0) {
							String promptURL = TLVXManager.callSessionManager
									.getCustomerPromptBaseURL()
									+ "/"
									+ customer.getGreetingWave();
							src += "promptURL=" + promptURL + "&";
						} else if (customer.getGreetingSentence() != null) {
							String promptText = customer.getGreetingSentence();
							src += "promptText=" + promptText + "&";
						}

						if (customer.getDdnWave() != null
								&& !"".equals(customer.getDdnWave())) {
							String customDeptPromptURL = TLVXManager.callSessionManager
									.getCustomDeptPromptBaseURL()
									+ "/"
									+ customer.getDdnWave();
							src += "customerDeptPromptURL="
									+ customDeptPromptURL + "&";
						}

						parameters.put("src", src);
						parameters.put("connectionid",
								customerConnection.getConnectionId());
						ivrDialog = new Dialog(parameters);
						ivrDialogStarted = true; // added for teleauto
													// compatibility
						startDialog(ivrDialog);
						call.setStatus(CallSessionManager.CALL_STATUS_IVR);
						call = TLVXManager.callDAO.save(call);
						saveCallEvent(CallEvent.IVR_CONNECT, null, null);
					} else {
						LOG.debug("two conditions above this section have failed. this is not an iVR call.");
						//Thread.sleep(500);
						parameters = buildSessionDataObject();
						parameters.put("src", TLVXManager.callSessionManager
								.getInitialDialogURI());
						parameters.put("connectionid",
								customerConnection.getConnectionId());
						callerInitialDialog = new Dialog(parameters);
						startDialog(callerInitialDialog);

						// moving the queue until after dialog is started
//							call.setStatus(CallSessionManager.CALL_STATUS_QUEUED);
//							call = TLVXManager.callDAO.save(call);
//							saveCallEvent(CallEvent.CUSTOMER_QUEUED, null);
					}
				} else {
					call = TLVXManager.callDAO.findById(call.getId());
					call.setStatus(CallSessionManager.CALL_STATUS_AGENT_ANSWERED);
					call = TLVXManager.callDAO.save(call);
					
					// Try to connect via IVR rules otherwise send to agent
					// Start ringing
					parameters = buildSessionDataObject();
					parameters.put("src", TLVXManager.callSessionManager
							.getInitialDialogURI());
					parameters.put("connectionid",
							customerConnection.getConnectionId());
					callerInitialDialog = new Dialog(parameters);
					videoStarting = true;
					startDialog(callerInitialDialog);
					
					Thread.sleep(2000);
					dialingCustomer = false;
					TLVXManager.callSessionManager.onCallSessionAccessCode(""+call.getId(), videoInterpreterRequest.videoCustomerInfo.accessCode);
					previousInterpreterVideo = videoInterpreterRequest.videoCustomerInfo.requireVideo;
					previousInterpreterGender = videoInterpreterRequest.videoCustomerInfo.interpreterGender;
					call.setVideo(videoInterpreterRequest.videoCustomerInfo.requireVideo);
					boolean validated = saveVideoCustomerInfo(videoInterpreterRequest.videoCustomerInfo);
					if (!validated) call.setLastReason("Please verify customer information.");
					if (TLVXManager.companyManager.shouldAskDepartmentCode(customer.getCode())) {
						if (!TLVXManager.companyManager.validateDepartmentCode(customer, videoInterpreterRequest.videoCustomerInfo.departmentCode)) {
							validated = false;
							call.setLastReason("Video User entered invalid dept code: "+videoInterpreterRequest.videoCustomerInfo.departmentCode);
						} else {
							call.setDepartmentCode(videoInterpreterRequest.videoCustomerInfo.departmentCode);
						}
						call = TLVXManager.callDAO.save(call);
					}
					if ((!validated || !completeCall(false, call, videoInterpreterRequest.videoCustomerInfo.language)) && !bluestream) {
						requeueCall(call);
					}
					//conferenceConnection(customerConnection, "full", null, null);

					//Map data = new HashMap();
					//data.put("status", "connected");
					//data.put("muted", customerConnection.isMuted());
					//data.put("ani", getAni());
					//dispatchAgentMessage(data, AgentManager.TL_CUSTOMER_STATUS_CHANGE);
				}
				dialingCustomer = false;
			} else if (dialingInterpreter
					|| (null != interpreterConnection && connectionId
							.equals(interpreterConnection.getConnectionId()))) {
				// fix for getActiveSession()?
				dialingInterpreter = false;
				synchronized (this) {
					if (interpreter == null) return;  // agent clicked (x) prior to connect?
					interpreter = TLVXManager.interpreterDAO.findInterpreterByID(interpreter.getInterpreterId());
					if (null == interpreterConnection
							|| !connectionId.equals(interpreterConnection
									.getConnectionId())) {
						if (LOG.isDebugEnabled())
							LOG.debug("Voxeo sucks for giving interpreter connected event before progressing.");
						String origination = (String) request
								.get("connectionLocal");
						String destination = (String) request
								.get("connectionRemote");
						interpreterConnection = new Connection(connectionId, destination, origination);
						interpreterConnection.setState(Connection.ConnectionState.CONNECTING);
						connections.put(connectionId, interpreterConnection);
					}
					interpreterConnection.setState(Connection.ConnectionState.CONNECTED);
					String languageId = TLVXManager.callSessionManager
							.findLanguageId(previousInterpreterLanguage);
					saveCallEvent(CallEvent.INTERPRETER_CONNECT,
							String.valueOf(interpreter.getInterpreterId()),
							languageId);

					if (false == isConnected(customerConnection)) {
						if (LOG.isDebugEnabled())
							LOG.debug("interpreter connected, but customer already disconnected");
						disconnectInterpreter("customer already disconnected");
						return;
					}
					
					if (false == isConnected(agentConnection) && videoInterpreterRequest != null) {
						callerOffHold();
					}

					// github #132 always ask to press 1 unless manual dial?  did interpreter log out in last second (maybe pressed 2 on another call?)
					if (/*interpreter.getActiveSession() &&*/ manualInterpreterDial == false) {
						
						if (interpreter.getOnWebSite() != null) {
							try {
								TLVXManager.interpreterManager.getINTClient(interpreter.getOnWebSite())
									.askToAcceptCall(getInterpreterCallInfo());
								webphoneRejectTimer = new Timer();
								webphoneTimeout = new WebPhoneInterpreterTimeout();
								webphoneRejectTimer.schedule(webphoneTimeout, 15000);
							} catch (Exception e) {
								LOG.error("Unable to send message to interpreter server, log out interpreter",e);
								interpreter.setOnWebSite(null);
								interpreter.setWebPhone(false);
								interpreter.setVideo(false);
								TLVXManager.getSession().saveOrUpdate(interpreter);
							}
						}

						if (interpreter.getWebPhone()) {
							// send on hold message to interpreter server
							
						} else {
							String accesscode = interpreter.getAccessCode();
							String accesscode_short = accesscode.substring(
									accesscode.length() - 5, accesscode.length());
							if (LOG.isDebugEnabled())
								LOG.debug("interpreter accesscode = " + accesscode
										+ ", accesscode_short = "
										+ accesscode_short);
							Map parameters = buildSessionDataObject();
							String promptURL = "";
							if (customer != null && customer.isMssg2IntrpActive()) {
								promptURL = TLVXManager.callSessionManager
										.getInterpreterPromptBaseURL()
										+ "/"
										+ customer.getMssg2IntrpWave();
							}
							String language = "";
							if (interpreterLanguage != null && TLVXManager.languageDAO.getLanguageCountForInterpreter(interpreter.getInterpreterId()).intValue()>1 ) {
								language = interpreterLanguage.replaceAll(" ",
										"%20");
							}
							String src = TLVXManager.callSessionManager
									.getInterpreterDialogURI()
									+ "?accesscode="
									+ accesscode
									+ "&accesscodeshort="
									+ accesscode_short
									+ "&language="
									+ language
									+ "&promptURL=" + promptURL;
							try {
								parameters.put("src", new URI(src).toString());
							} catch (Exception e) {
								LOG.warn("Error trying to encode URI " + src);
								parameters.put("src", src);
							}
							parameters.put("connectionid",
									interpreterConnection.getConnectionId());
							interpreterDialog = new Dialog(parameters);
							//Thread.sleep(750);
							startDialog(interpreterDialog);
						}
					} else {
						if (LOG.isDebugEnabled()) {
							LOG.debug(" 2 interpreterValidated = true callId = "
									+ call.getId() + " this =" + this);
						}
						interpreterValidated = true;
						if (interpreter.getNumMissedCalls() > 0) {
							LOG.info("setNumMissedCalls to 0");
							interpreter.setNumMissedCalls(0); // reset missed calls on manual dial
							TLVXManager.getSession().saveOrUpdate(interpreter);
						}
						if (interpreter.getOnWebSite() != null) {
							try {
								TLVXManager.interpreterManager.getINTClient(interpreter.getOnWebSite())
									.interpreterOffHold(getInterpreterCallInfo());
							} catch (Exception e) {
								LOG.error("Unable to send message to interpreter server, log out interpreter",e);
								interpreter.setOnWebSite(null);
								interpreter.setWebPhone(false);
								TLVXManager.getSession().saveOrUpdate(interpreter);
							}
						}
						
						conferenceConnection(interpreterConnection, "full", "#", null);

						Map data = new HashMap();
						data.put("status", "connected");
						data.put("muted", interpreterConnection.isMuted());
						if (interpreter != null) {
							data.put("name", interpreter.getFirstName() + " "
									+ interpreter.getLastName());
						}
						dispatchAgentMessage(data,
								InterpreterManager.TL_INTERPRETER_STATUS_CHANGE);

						// sendInterpreterHistory();
						saveCallEvent(CallEvent.INTERPRETER_ON_CALL,
								String.valueOf(interpreter.getInterpreterId()),
								languageId);
						startInterpreterTime();
						interpreterOnline = true;
						call.setLanguage(previousInterpreterLanguage);
						
						if (null == agentId) {
							callerOffHold();

							saveCallEvent(
									CallEvent.CUSTOMER_INTERPRETER_CONNECT,
									null);
							interpreterAttempts = 0;
							interpreterIsBillable();
						}
					}

					dialingInterpreter = false;
				}
				if (!recordingStarted
						&& !TLVXManager.subscribedClientsDIDDAO
								.isDontRecordCall(customerConnection
										.getDestination())) {
					recordingStarted = true;
					confRecordingStart();
				}
			} else if (thirdPartyConnections.keySet().contains(connectionId)) {
				Connection thirdPartyConnection = thirdPartyConnections.get(connectionId);
				thirdPartyConnection.setState(Connection.ConnectionState.CONNECTED);
				
				dialingThirdParty = false;
				
				if (interpreter != null && interpreter.getOnWebSite() != null) {
					try {
						TLVXManager.interpreterManager.getINTClient(interpreter.getOnWebSite())
							.callStatusUpdate(getInterpreterCallInfo());
					} catch (Exception e) {
						LOG.error("Unable to send message to interpreter server, log out interpreter",e);
						interpreter.setOnWebSite(null);
						interpreter.setWebPhone(false);
						interpreter.setVideo(false);
						TLVXManager.getSession().saveOrUpdate(interpreter);
					}
				}

				conferenceConnection(thirdPartyConnection, "full", null, null);

				Map data = new HashMap();
				data.put("id", thirdPartyConnection.getConnectionId());
				data.put("status", "connected");
				data.put("muted", thirdPartyConnection.isMuted());
				data.put("name", SipUtil.getPhoneNumber(thirdPartyConnection.getDestination()));
				dispatchAgentMessage(data, AgentManager.TL_THIRDPARTY_STATUS_CHANGE);

				saveCallEvent(CallEvent.THIRDPARTY_CONNECT,
						thirdPartyDestination);
				startThirdPartyTime(connectionId);
				if (!recordingStarted
						&& !TLVXManager.subscribedClientsDIDDAO
								.isDontRecordCall(customerConnection
										.getDestination())) {
					recordingStarted = true;
					confRecordingStart();
				}
			} else if (null != transferConnection
					&& connectionId
							.equals(transferConnection.getConnectionId())) {
				transferConnection
						.setState(Connection.ConnectionState.CONNECTED);
				dialingTransfer = false;
				conferenceConnection(transferConnection, "full", null, null);
				callerOffHold();
				saveCallEvent(CallEvent.TRANSFER_CONNECT, transferDestination);
				if (!recordingStarted
						&& !TLVXManager.subscribedClientsDIDDAO
								.isDontRecordCall(customerConnection
										.getDestination())) {
					recordingStarted = true;
					confRecordingStart();
				}
			}
		} catch (Throwable t) {
			LOG.warn("Exception occured onConnectionConnected", t);
			t.printStackTrace();
		}
	}
	
	static private int nextRoomNumber = 1;
	
	private InterpreterCallInfo getInterpreterCallInfo() {
		InterpreterCallInfo callInfo = new InterpreterCallInfo();
		if (interpreter != null) callInfo.interpreterInfo = TLVXAPIServiceImpl.convertToInterpreterInfo(interpreter, call.getId());
		callInfo.interpreterInfo.callId = call.getId();
		callInfo.callId = call.getId();
		if (videoInterpreterRequest != null && call.isVideo()) {
			callInfo.videoJanusServer = videoInterpreterRequest.videoCustomerInfo.janusServer;
			if (videoInterpreterRequest.videoCustomerInfo.roomNumber == null) {
				videoInterpreterRequest.videoCustomerInfo.roomNumber = ""+nextRoomNumber;
				nextRoomNumber++;
				if (nextRoomNumber > 28) nextRoomNumber = 1;
				// TODO need to check out/in rooms
			}
			callInfo.roomNumber = videoInterpreterRequest.videoCustomerInfo.roomNumber;
		}
		callInfo.agentOnCall = (call.getAgent() != null);
		callInfo.interpreterValidated = interpreterValidated;
		callInfo.onHold = interpreterOnHold;
		callInfo.language = previousInterpreterLanguage;
		callInfo.interpreterBrowserSound = interpreterBrowserSound;
		callInfo.missedCalls = "0";
		if (interpreter != null) {
			callInfo.missedCalls = ""+interpreter.getNumMissedCalls();
		}
		callInfo.thirdPartyNumber = new ArrayList<String>();
		callInfo.thirdPartyId = new ArrayList<String>();
		for (Connection thirdParty: thirdPartyConnections.values()) {
			callInfo.thirdPartyId.add(thirdParty.getConnectionId());
			callInfo.thirdPartyNumber.add(SipUtil.getUnformattedPhoneNumberFromSipUri(thirdParty.getDestination()));
		}
		if (customer != null && customer.isMssg2IntrpActive()) {
			callInfo.instructionsText = customer.getMssg2IntrpText();
		}
		callInfo.dialingThirdParty = dialingThirdParty;
		return callInfo;
	}
	
	private VideoCallInfo getVideoCallInfo() {
		VideoCallInfo callInfo = new VideoCallInfo();
		
		if (videoInterpreterRequest != null) {
			if (videoInterpreterRequest.videoCustomerInfo.roomNumber == null) {
				videoInterpreterRequest.videoCustomerInfo.roomNumber = ""+nextRoomNumber;
				nextRoomNumber++;
				if (nextRoomNumber > 28) nextRoomNumber = 1;
				// TODO need to check out/in rooms
			}
			callInfo.interpreterInfo = videoInterpreterRequest.videoCustomerInfo;
			callInfo.interpreterInfo.callId = call.getId();
			callInfo.interpreterInfo.roomNumber = videoInterpreterRequest.videoCustomerInfo.roomNumber;
			callInfo.agentOnCall = (agentConnection != null && (agentConnection.getState() == ConnectionState.CONNECTED));
			callInfo.interpreterOnCall = (interpreterConnection != null && (interpreterConnection.getState() == ConnectionState.CONNECTED));
			callInfo.thirdPartyOnCall = (thirdPartyConnections.size() > 0 );
			callInfo.interpreterValidated = interpreterValidated;
			callInfo.onHold = callerOnHold;
			callInfo.language = previousInterpreterLanguage;
			callInfo.callSessionId = call.getCallSessionId();
			callInfo.isVideo = videoRequested;
			callInfo.callId = call.getId();
		}
		return callInfo;
	}

	private void startInterpreterTime() {
		interpreterStartTime = new Date();
		interpreterBillable = false;
	}
	
	private void interpreterIsBillable() {
		interpreterBillable = true;
	}
	
	private void saveInterpreterTime() {
		try {
			if (interpreterStartTime != null && interpreterBillable) {
				InterpreterActivity interpreterActivity = new InterpreterActivity();
				interpreterActivity.setCallId(call.getId());
				interpreter = TLVXManager.interpreterDAO.findInterpreterByID(interpreter.getInterpreterId());
				interpreterActivity.setInterpreter(interpreter);
				Language l = TLVXManager.languageDAO.findByName(interpreterLanguage);
				interpreterActivity.setLanguage(l);
				interpreterActivity.setStartTime(interpreterStartTime);
				interpreterActivity.setEndTime(new Date());
				if (interpreterConnection != null) {
					interpreterActivity.setVideo(interpreterConnection.isVideo());
				} else {
					LOG.warn("saveInterpreterTime() interpreterConnection is null");
				}
				TLVXManager.callDAO.save(interpreterActivity);
				interpreterStartTime = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void startThirdPartyTime(String connectionId) {
		thirdPartyStartTimes.put(connectionId, new Date());
	}
	
	private void saveThirdPartyTime(String connectionId) {
		try {
			if (thirdPartyStartTimes.containsKey(connectionId)) {
				ThirdPartyActivity thirdPartyActivity = new ThirdPartyActivity();
				thirdPartyActivity.setCallId(call.getId());
				thirdPartyActivity.setThirdpartyAni(SipUtil.getUnformattedPhoneNumberFromSipUri(thirdPartyConnections.get(connectionId).getDestination()));
				thirdPartyActivity.setStartTime(thirdPartyStartTimes.get(connectionId));
				thirdPartyActivity.setEndTime(new Date());
				TLVXManager.callDAO.save(thirdPartyActivity);
				thirdPartyStartTimes.remove(connectionId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void onConnectionDisconnected(Map request) {
		if (LOG.isDebugEnabled())
			LOG.debug("onConnectionDisconnected received");

		String connectionId = (String) request.get("connectionid");
		try {
			synchronized (this) {
				if (null != customerConnection
						&& connectionId.equals(customerConnection
								.getConnectionId())) {
					processCustomerDisconnect(false, null);
				} else if (null != agentConnection
						&& connectionId.equals(agentConnection
								.getConnectionId())) {
					
					if (isConnected(agentConnection)) {
						processAgentDisconnect(false, null);
					} else {
						processAgentDisconnect(true, "agent not connected yet");
					}
					
					// TODO possible fix for stuck race condition
					if (interpreterDialog != null && interpreterValidated && !interpreterDialog.getStarted()) {
						interpreterDialog.setFinished(true);
					}
					
					if (interpreter != null && interpreter.getOnWebSite() != null) {
						interpreter = TLVXManager.interpreterDAO.findInterpreterByID(interpreter.getInterpreterId());
						
						if (interpreter != null && interpreter.getOnWebSite() != null) {
							try {
								TLVXManager.interpreterManager.getINTClient(interpreter.getOnWebSite())
									.agentDisconnected(getInterpreterCallInfo());
							} catch (Exception e) {
								LOG.error("Unable to send message to interpreter server, log out interpreter",e);
								interpreter.setOnWebSite(null);
								interpreter.setWebPhone(false);
								interpreter.setVideo(false);
								TLVXManager.getSession().saveOrUpdate(interpreter);
							}
						}
					}
				} else if (null != interpreterConnection
						&& connectionId.equals(interpreterConnection
								.getConnectionId())) {
					processInterpreterDisconnect(false, "line disconnected");
				} else if (thirdPartyConnections.containsKey(connectionId)) {
					processThirdPartyDisconnect(connectionId, false, null);
				} else if (null != transferConnection
						&& connectionId.equals(transferConnection
								.getConnectionId())) {
					processTransferDisconnect(false, null);
				}

				if (false == isAnyConnected()) {
					cleanUpSession();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processCustomerDisconnect(boolean failed, String reason) {
		if (LOG.isDebugEnabled())
			LOG.debug("processCustomerDisconnect, failed = " + failed
					+ ", reason = " + reason);
		
		if (videoInterpreterRequest != null && videoInterpreterRequest.videoCustomerInfo.videoServer != null) {
			try {
				TLVXManager.customerUserManager.getVideoAPIClient(videoInterpreterRequest.videoCustomerInfo.videoServer).disconnect(getVideoCallInfo());
			} catch (Exception e) {
				LOG.error("Unable to send message to video call server, ending call",e);
				hangupConnection(customerConnection);
			}
		}

		if (false == dialingCustomer) {
			customerConnection.setState(Connection.ConnectionState.DISCONNECTED);
			call = TLVXManager.callDAO.findById(call.getId());
			call.setStatus(CallSessionManager.CALL_STATUS_FINISHED);
			if (call.getEndDate() == null) call.setEndDate(new Date());
			call = TLVXManager.callDAO.save(call);

			if (agentConnection != null && true == isConnected(agentConnection)) {
				hangupConnection(agentConnection);
			} else {
				LOG.debug("not hanging up agentConnection: "+agentConnection);
			}

			if (interpreterConnection != null && true == isConnected(interpreterConnection)) {
				hangupConnection(interpreterConnection);
			} else {
				LOG.debug("not hanging up interpreterConnection: "+interpreterConnection);
			}
      if (interpreter != null) {
        TLVXManager.interpreterManager.setInterpreterStatus(interpreter,
            InterpreterStatus.INTERPRETER_STATUS_IDLE, call.getId());
      }

			for (Connection thirdPartyConnection : thirdPartyConnections.values()) {
				if (thirdPartyConnection != null && true == isConnected(thirdPartyConnection)) {
					hangupConnection(thirdPartyConnection);
				}else {
					LOG.debug("not hanging up thirdPartyConnection: "+thirdPartyConnection);
				}
			}

			if (transferConnection != null && true == isConnected(transferConnection)) {
				hangupConnection(transferConnection);
			} else {
				LOG.debug("not hanging up transferConnection: "+transferConnection);
			}
		} else {
			dialingCustomer = false;

			Map data = new HashMap();
			data.put("status", "idle");
			if (null != customerConnection) {
				data.put("muted", customerConnection.isMuted());
			}
			dispatchAgentMessage(data, AgentManager.TL_CUSTOMER_STATUS_CHANGE);
		}

		saveCallEvent(CallEvent.CUSTOMER_DISCONNECT, reason);
	}

	private void processAgentDisconnect(boolean failed, String reason) {
		if (LOG.isDebugEnabled())
			LOG.debug("processAgentDisconnect, failed = " + failed
					+ ", reason = " + reason + ", agentRejected = "
					+ agentRejected + ", requeueCall = " + requeueCall);

		if (false == destroyed && agentId != null) {
			if (agentConnection != null)
				agentConnection
						.setState(Connection.ConnectionState.DISCONNECTED);
			agentConnection = null; // this will fix logic with out of order CallProgressing event
			dialingAgent = false;

			sendAgentIdleMessages();
			call = TLVXManager.callDAO.findById(call.getId());
			call.setAgent(null);
			call = TLVXManager.callDAO.save(call);

			// disconnect this agent
			saveCallEvent(CallEvent.AGENT_DISCONNECT, String.valueOf(agentId));

			TLVXManager.agentManager.agentCallDisconnected(getAgent(agentId), call);
			agentId = null;
			
//			if (interpreterValidated && !agentRejected && !failed && !requeueCall) {
//				callerOffHold();
//			}
			
			if (bluestream) return;  // the video app does the call for bluesteam, don't hang it up.

			if ((true == agentRejected || true == failed || true == requeueCall)
					&& isConnected(customerConnection)) {
				call.setStatus(CallSessionManager.CALL_STATUS_QUEUED);
				call = TLVXManager.callDAO.save(call);
				saveCallEvent(CallEvent.CUSTOMER_QUEUED, null);
			} else {
				if ((!isConnected(interpreterConnection) || isDisconnecting(interpreterConnection)) && !dialingInterpreter && !dialingTransfer 
						&& !dialingThirdParty
						&& (thirdPartyConnections.size() == 0)
						&& isConnected(customerConnection)
						&& !isConnected(transferConnection)) {
					hangupConnection(customerConnection);
				} else if (!completeCallPressed && !dialingTransfer){
					call.setStatus(CallSessionManager.CALL_STATUS_QUEUED);
					call = TLVXManager.callDAO.save(call);
					saveCallEvent(CallEvent.CUSTOMER_QUEUED, "requeuing after agent hangup?");
//					if (interpreter != null) {
//		        TLVXManager.interpreterManager.setInterpreterStatus(interpreter,
//		            InterpreterStatus.INTERPRETER_STATUS_IDLE, call.getId());
//		      }
				}
				// this might be a good place to add this logic: customer
				// disconnect on limbo
				// cond: customer is on the phone, interpreter is NOT connected
				// to the customer,
				// agent just hung up. there is no third party.
				// this condition has to be checked AFTER the eventType =
				// Agent_Disconnect
				// from Hamed:
				// check for
				// not dialing interpreter,
				// not interpreter connected,
				// not dialing third party,
				// not third party connected,
			}
		}
	}

	private void sendAgentIdleMessages() {
		Map data = new HashMap();
		data.put("status", "idle");
		dispatchAgentMessage(data, AgentManager.TL_CUSTOMER_STATUS_CHANGE);

		data = new HashMap();
		data.put("status", "idle");
		data.put("oncall", "false");
		dispatchAgentMessage(data,
				InterpreterManager.TL_INTERPRETER_STATUS_CHANGE);

		for (String connectionId : thirdPartyConnections.keySet()) {
			Connection thirdPartyConnection = thirdPartyConnections.get(connectionId);
			data = new HashMap();
			data.put("status", "idle");
			data.put("id", connectionId);
			data.put("name", SipUtil.getPhoneNumber(thirdPartyConnection.getDestination()));
			dispatchAgentMessage(data, AgentManager.TL_THIRDPARTY_STATUS_CHANGE);
		}

		data = new HashMap();
		dispatchAgentMessage(data, AgentManager.TL_CALL_STOP);
	}

	private void processInterpreterDisconnect(boolean failed, String reason) {
		if (LOG.isDebugEnabled())
			LOG.debug("processInterpreterDisconnect, failed = " + failed + " interpreterLanguage = "+ interpreterLanguage
					+ ", reason = " + reason + ", for " + this);
		synchronized(this) {
			if (interpreter == null) return;
			interpreter = TLVXManager.interpreterDAO.findInterpreterByID(interpreter.getInterpreterId());
			if (interpreter.getOnWebSite() != null) {
				try {
					TLVXManager.interpreterManager.getINTClient(interpreter.getOnWebSite())
						.disconnect(getInterpreterCallInfo());
				} catch (Exception e) {
					LOG.error("error sending interpreter session, logging them out", e);
					interpreter.setOnWebSite(null);
					interpreter.setWebPhone(false);
					interpreter.setVideo(false);
					TLVXManager.getSession().saveOrUpdate(interpreter);
				}
			}
			saveInterpreterTime();
			if (interpreterConnection != null) {
				interpreterConnection.setState(Connection.ConnectionState.DISCONNECTED);
			}
			dialingInterpreter = false;
			if (processingDialog == interpreterDialog) {
				processingDialog = null;
				processNextDialog();
			}
	
			TLVXManager.interpreterManager.setInterpreterStatus(interpreter, InterpreterStatus.INTERPRETER_STATUS_IDLE, call.getId());
	
			if (LOG.isDebugEnabled()) {
				LOG.debug(" 1 interpreterValidated == " + interpreterValidated
						+ " callId = " + call.getId() + " this =" + this);
			}
	
			if (false == interpreterValidated || true == failed) {
				String callType = "pstn";
				if (interpreter.getOnWebSite() != null) {
					if (videoInterpreterRequest != null && videoInterpreterRequest.videoCustomerInfo.videoServer != null) {
						callType = "video";
					} else {
						callType = "webphone";
					}
				}
				TLVXManager.interpreterManager.failedInterpreter(interpreter, manualInterpreterDial, call.getId(), interpreterLanguage, reason, callType);
				interpreter = TLVXManager.interpreterDAO.findInterpreterByID(interpreter.getInterpreterId());
	
				interpreterAttempts++;
	
				if (interpreter != null) {
					saveCallEvent(CallEvent.INTERPRETER_REJECTED, String.valueOf(interpreter.getInterpreterId()), reason);
				} else {
					saveCallEvent(CallEvent.INTERPRETER_REJECTED, null, reason);
				}
	
				clearInterpreter();
				if ((interpreterAttempts < MAX_INTERPRETER_ATTEMPTS || interpreterLanguage == null) && false == destroyed && false == manualInterpreterDial) {
					if (null != customerConnection
							&& customerConnection.getState() == Connection.ConnectionState.CONNECTED) {
						TLVXManager.interpreterManager
								.dispatchCallToAvailableInterpreter(call,
										interpreterHistory, interpreterLanguage,
										previousInterpreterGender, previousInterpreterVideo, null, completeCallPressed);
					}
				} else {
					if ((isConnected(agentConnection) == false || isDisconnecting(agentConnection))
							&& agentId != null) {
						requeueCall = true;
					} else if (agentId != null) {
						HashMap data = new HashMap();
						data.put("status", "idle");
						if (null != interpreterConnection) {
							data.put("muted", interpreterConnection.isMuted());
						}
						dispatchAgentMessage(data,
								InterpreterManager.TL_INTERPRETER_STATUS_CHANGE);
						// sendInterpreterHistory();
					} else if (false == destroyed) {
						completeCallNoInterpreters = true;
						call = TLVXManager.callDAO.findById(call.getId());
						call.setStatus(CallSessionManager.CALL_STATUS_QUEUED);
						call = TLVXManager.callDAO.save(call);
						if (interpreterAttempts < MAX_INTERPRETER_ATTEMPTS) {
							saveCallEvent(CallEvent.CUSTOMER_QUEUED,
									"no interpreters available");
						} else {
							saveCallEvent(CallEvent.CUSTOMER_QUEUED,
									"max interpreter attempts: "
											+ interpreterAttempts);
						}
					}
					interpreterAttempts = 0;
				}
			} else {
				interpreterAttempts = 0;
	
				Map data = new HashMap();
				data.put("status", "idle");
				dispatchAgentMessage(data, InterpreterManager.TL_INTERPRETER_STATUS_CHANGE);
	
				if (interpreter != null) {
					if (LOG.isDebugEnabled())
						LOG.debug("pid INTERPRETER_DISCONNECT from disconnectInterpreter()");
					saveCallEvent(CallEvent.INTERPRETER_DISCONNECT,
							String.valueOf(interpreter.getInterpreterId()), reason);
				}
				
				clearInterpreter();
				if (agentConnection == null
						|| false == isConnected(agentConnection)) {
					if (customerConnection != null
							&& true == isConnected(customerConnection)) {
						hangupConnection(customerConnection);
					}
				}
			}
		}
	}

	private void processThirdPartyDisconnect(String connectionId, boolean failed, String reason) {
		if (LOG.isDebugEnabled())
			LOG.debug("processThirdPartyDisconnect, failed = " + failed
					+ ", reason = " + reason + " for " + this);
		
		Connection thirdPartyConnection = thirdPartyConnections.get(connectionId);
		
		thirdPartyConnection.setState(Connection.ConnectionState.DISCONNECTED);
		dialingThirdParty = false;

		if (true == failed) {
			Map data = new HashMap();
			data.put("id", connectionId);
			data.put("status", "failed");
			data.put("muted", thirdPartyConnection.isMuted());
			data.put("name", SipUtil.getPhoneNumber(thirdPartyConnection.getDestination()));
			dispatchAgentMessage(data, AgentManager.TL_THIRDPARTY_STATUS_CHANGE);

			saveCallEvent(CallEvent.THIRDPARTY_REJECTED, SipUtil.getUnformattedPhoneNumberFromSipUri(thirdPartyConnection.getDestination()));
		}

		Map data = new HashMap();
		data.put("status", "idle");
		data.put("id", connectionId);
		data.put("muted", thirdPartyConnection.isMuted());
		dispatchAgentMessage(data, AgentManager.TL_THIRDPARTY_STATUS_CHANGE);

		saveCallEvent(CallEvent.THIRDPARTY_DISCONNECT, SipUtil.getUnformattedPhoneNumberFromSipUri(thirdPartyConnection.getDestination()));
		saveThirdPartyTime(connectionId);
		thirdPartyConnections.remove(connectionId);
		
		if (interpreter != null && interpreter.getOnWebSite() != null) {
			try {
				TLVXManager.interpreterManager.getINTClient(interpreter.getOnWebSite())
					.callStatusUpdate(getInterpreterCallInfo());
			} catch (Exception e) {
				LOG.error("Unable to send message to interpreter server, log out interpreter",e);
				interpreter.setOnWebSite(null);
				interpreter.setWebPhone(false);
				interpreter.setVideo(false);
				TLVXManager.getSession().saveOrUpdate(interpreter);
			}
		}
	}

	private void processTransferDisconnect(boolean failed, String reason) {
		if (LOG.isDebugEnabled())
			LOG.debug("processTransferDisconnect, failed = " + failed
					+ ", reason = " + reason + " for " + this);

		transferConnection.setState(Connection.ConnectionState.DISCONNECTED);
		dialingTransfer = false;

		if (true == failed) {
			call = TLVXManager.callDAO.findById(call.getId());
			call.setStatus(CallSessionManager.CALL_STATUS_QUEUED);
			call = TLVXManager.callDAO.save(call);
			saveCallEvent(CallEvent.CUSTOMER_QUEUED, null);

			saveCallEvent(CallEvent.TRANSFER_REJECTED, transferDestination);
		}

		saveCallEvent(CallEvent.TRANSFER_DISCONNECT, transferDestination);
	}

	public synchronized void onConnectionFailed(Map request) {
		if (LOG.isDebugEnabled())
			LOG.debug("onConnectionFailed received for " + this + " request = " + request);

		String connectionId = (String) request.get("connectionid");
		String reason = (String) request.get("reason");

		synchronized(this) {
			if (reason.equals("unknown") && rejectReason != null) {
				reason = rejectReason;
				rejectReason = null;
			}
			if (agentConnection != null
					&& connectionId.equals(agentConnection.getConnectionId())) {
				processAgentDisconnect(true, reason);
			} else if (interpreterConnection != null
					&& connectionId.equals(interpreterConnection.getConnectionId())) {
				processInterpreterDisconnect(true, reason);
			} else if (thirdPartyConnections.containsKey(connectionId)) {
				processThirdPartyDisconnect(connectionId, true, reason);
			} else if (customerConnection != null
					&& connectionId.equals(customerConnection.getConnectionId())) {
				processCustomerDisconnect(true, reason);
			} else if (transferConnection != null
					&& connectionId.equals(transferConnection.getConnectionId())) {
				processTransferDisconnect(true, reason);
			} else if (true == dialingAgent || true == dialingInterpreter
					|| true == dialingThirdParty || true == dialingCustomer
					|| true == dialingTransfer) {
				onConnectionProgressing(request);
				onConnectionFailed(request);
			} else {
				LOG.warn("Unable to handle onConnectionFailed request, ignoring");
			}
	
			if (false == isAnyConnected()) {
				cleanUpSession();
			}
		}
	}

	public synchronized void onConferenceCreated(Map request) {
		if (LOG.isDebugEnabled())
			LOG.debug("onConferenceCreated received for " + this);

		conference = new Conference((String) request.get("conferenceid"));

		if (null != customerConnection) {
			Map parameters = buildSessionDataObject();
			parameters.put("connectionid", customerConnection.getConnectionId());
			try {
				TLVXManager.ccxmlManager.accept(parameters);
			} catch (Exception e) {
				LOG.warn("Exception caught while trying to accept call", e);
				handleServerError();
			}
		} else {
			if (LOG.isDebugEnabled())
				LOG.debug("callerConnection = null, must dial agent first");
			// setAgent(savedCall.getAgent().getId());
		}
	}

	public void onErrorSemantic(Map<String, String> request) {
		String keyValues = "";
		for (String key : request.keySet()) {
			keyValues += key + ": " + request.get(key) + " ";
		}
		if (LOG.isDebugEnabled())
			LOG.debug("onErrorSemantic received " + keyValues);
		String tag = (String) request.get("tagname");
		if ("dialogterminate".equals(tag)) {
			synchronized (this) {
				callerOnHoldDialog = null;
				callerOnHold = false;
				interpreterOnHoldDialog = null;
				interpreterOnHold = false;
				thirdPartyOnHoldDialogs.clear();
				processingDialog = null;
				processNextDialog();
			}
		} else if ("disconnect".equals(tag)
				&& request.keySet().contains("connectionid")) {
			onConnectionDisconnected(request);
		} else if ("dialogstart".equals(tag)) {
			synchronized (this) {
				processingDialog = null;
				processNextDialog();
			}
		} else if ("disconnect".equals(tag) && agentConnection != null && agentConnection.getState() == ConnectionState.DISCONNECTING) {
			LOG.debug("agent disconnect call not found voxeo forgot connectionid");
			request.put("connectionid", agentConnection.getConnectionId());
			onConnectionDisconnected(request);
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("onErrorSemantic UNHANDLED need logic for " + tag
						+ " " + keyValues);
			}
		}
	}

	public synchronized void onConnectionWrongState(Map request) {
		if (LOG.isDebugEnabled())
			LOG.debug("onConnectionWrongState received");
		String tag = (String) request.get("tagname");
		if ("dialogstart".equals(tag)) {
			processNextDialog();
		}
		if ("join".equals(tag)) {
			String connectionId = (String) request.get("connectionid");
			if (agentConnection != null && isConnected(agentConnection)
					&& connectionId.equalsIgnoreCase(agentConnection
							.getConnectionId())) {
				agentConnection.setConferenced(false);
				if (agentConnection.wrongErrorStates > 10)
					return;
				agentConnection.wrongErrorStates++;
				if (agentConnection.wrongErrorStates>2) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				conferenceConnection(agentConnection, "full", "*", null);
			} else if (customerConnection != null && isConnected(customerConnection)
					&& connectionId.equalsIgnoreCase(customerConnection
							.getConnectionId())) {
				customerConnection.setConferenced(false);
				if (customerConnection.wrongErrorStates > 10)
					return;
				customerConnection.wrongErrorStates++;
				if (customerConnection.wrongErrorStates > 2) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (callerOnHoldDialog != null) {
					callerOffHold();
				} else {
					conferenceConnection(customerConnection, "full", null, null);
				}
			}
			for (Connection thirdPartyConnection : thirdPartyConnections.values()) {
				if (thirdPartyConnection != null && isConnected(thirdPartyConnection)
						&& connectionId.equalsIgnoreCase(thirdPartyConnection
								.getConnectionId())) {
					thirdPartyConnection.setConferenced(false);
					if (thirdPartyConnection.wrongErrorStates > 10)
						return;
					thirdPartyConnection.wrongErrorStates++;
					if (thirdPartyConnection.wrongErrorStates>2) {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					conferenceConnection(thirdPartyConnection, "full", null, null);
				}
			}
			if (interpreterConnection != null && isConnected(interpreterConnection)
					&& connectionId.equalsIgnoreCase(interpreterConnection
							.getConnectionId())) {
				interpreterConnection.setConferenced(false);
				if (interpreterConnection.wrongErrorStates > 10)
					return;
				interpreterConnection.wrongErrorStates++;
				if (interpreterConnection.wrongErrorStates>2) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				conferenceConnection(interpreterConnection, "full", "#", null);
			}
		}
	}

	public synchronized void onConferenceDestroyed(Map request) {
		if (LOG.isDebugEnabled())
			LOG.debug("onSignalingConferenceDestroyed received");
	}

	public synchronized void onConferenceJoined(Map request) {
		synchronized(this) {
			String connectionid = (String) request.get("id2");
			//System.out.println("Conference joined interpreterJoinAction: "+connectionid+" "+interpreterJoinAction+" "+interpreterJoinActionPhoneNumber);
			//if (interpreterConnection != null) System.out.println("interpreter.connectionid: "+interpreterConnection.getConnectionId());
			if (LOG.isDebugEnabled())
				LOG.debug("onConferenceJoined received for " + this);
			if (customerConnection != null) {
				customerConnection.wrongErrorStates = 0;
//				for(Dialog dialog : currentDialogs.values()) {
//					if (customerConnection.getConnectionId().equals(dialog.getParameters().get("connectionid"))) {
//						currentDialogs.remove(dialog);
//						break;
//					}
//				}
			}
			if (agentConnection != null)
				agentConnection.wrongErrorStates = 0;
			for (Connection thirdPartyConnection : thirdPartyConnections.values()) {
				thirdPartyConnection.wrongErrorStates = 0;
			}
			if (interpreterConnection != null && connectionid != null) {
				
				interpreterConnection.wrongErrorStates = 0;
				if (interpreterConnection.getConnectionId().equals(connectionid) && "dialthirdparty".equals(interpreterJoinAction) && interpreterJoinActionPhoneNumber != null) {
					connectThirdParty(interpreterJoinActionPhoneNumber);
					interpreterJoinAction = null;
					interpreterJoinActionPhoneNumber = null;
				};
				if (interpreterConnection.getConnectionId().equals(connectionid) && "hangupthirdparty".equals(interpreterJoinAction)) {
					try { Thread.sleep(500); } catch (InterruptedException e) { }
					for(Connection tpc : thirdPartyConnections.values()) {
						hangupConnection(tpc);
					}
					interpreterJoinAction = null;
					interpreterJoinActionPhoneNumber = null;
				};
			}
			if (agentConnection != null && !agentConnection.isConferenced()) {
				conferenceConnection(agentConnection, "full", "*", null);
				agentConnection.setConferenced(true);
			}
		}
		updateVideoClient();
	}

	public synchronized void onConferenceUnjoined(Map request) {
		if (LOG.isDebugEnabled())
			LOG.debug("onConferenceUnjoined received for " + this);

			if (true == destroyed) {
				return;
			}
	
			String id1 = (String) request.get("id1");
			String id2 = (String) request.get("id2");
			String termdigit = (String) request.get("termdigit");
	
			if ("#".equals(termdigit) || "*".equals(termdigit)) {
	
				if (interpreterConnection != null
						&& (id1.equals(interpreterConnection.getConnectionId()) || id2
								.equals(interpreterConnection.getConnectionId()))
						&& isConnected(interpreterConnection)
						&& isConnected(agentConnection)
						&& isConnected(customerConnection)
						&& !interpreterConnection.isMuted()) {
					if (LOG.isDebugEnabled())
						LOG.debug("maybe interpreterer accidently hit # while agent online, so try and rejoin him");
					interpreterConnection.setConferenced(false);
					conferenceConnection(interpreterConnection, "full", "#", null);
					return;
				}
	
				if (agentConnection != null
						&& (id1.equals(agentConnection.getConnectionId()) || id2
								.equals(agentConnection.getConnectionId()))) {
					if (!completeCallPressed
							&& agentConnection.getState() != Connection.ConnectionState.DISCONNECTED) {
						AgentServiceImpl.sendAgentMessage(call.getAgent(),
								AgentMessages.SaveCallInformation.toString() + ":");
						requeueCall(call);
					}
				}
	
				if (interpreterConnection != null
						&& isConnected(interpreterConnection)
						&& !isConnected(agentConnection)
						&& isConnected(customerConnection)
						&& (id1.equals(interpreterConnection.getConnectionId()) || id2
								.equals(interpreterConnection.getConnectionId()))) {
					if (false == completeCallPressed && false == ivrDialogStarted) {
						if (LOG.isDebugEnabled())
							LOG.debug("maybe interpreterer accidently hit #, so try and rejoin him");
						conferenceConnection(interpreterConnection, "full", "#", null);
					} else {
						if (call.getStatus() != CallSessionManager.CALL_STATUS_FINISHED
								&& customerConnection != null
								&& customerConnection.getState() == Connection.ConnectionState.CONNECTED
								&& interpreterConnection.getState() == Connection.ConnectionState.CONNECTED) {
							// start new interpreter ivr dialog
							Map parameters = buildSessionDataObject();
							parameters.put("connectionid", interpreterConnection.getConnectionId());
							parameters.put("src", "telelanguage/interpreter-pound-vxml.xml");
							interpreterPoundDialog = new Dialog(parameters);
							startDialog(interpreterPoundDialog);

							/* this is for option 3 in dialog 
							call.setStatus(CallSessionManager.CALL_STATUS_QUEUED);
							call.setLastReason("Interpreter sent call back to agent with #.");
							call = TLVXManager.callDAO.save(call);
							saveCallEvent(CallEvent.CUSTOMER_QUEUED, "Interpreter #");
							*/
						} else {
							callerOnHold(false);
							interpreterOnHold(true);
						}
						interpreterConnection.setConferenced(false);
						call = TLVXManager.callDAO.findById(call.getId());
						TLVXManager.agentManager.sendQueueStatusUpdates();
					}
				}
			}
		updateVideoClient();
	}
	
	private void updateVideoClient() {
		if (videoInterpreterRequest != null && videoInterpreterRequest.videoCustomerInfo.videoServer != null) {
			try {
				TLVXManager.customerUserManager.getVideoAPIClient(videoInterpreterRequest.videoCustomerInfo.videoServer).statusChanged(getVideoCallInfo());
			} catch (Exception e) {
				LOG.error("Unable to send message to video call server, ending call",e);
				hangupConnection(customerConnection);
			}
		}
	}

	public synchronized void acceptInterpreterFromWeb(InterpreterInfo interpreterInfo) {
		if (webphoneRejectTimer != null) {
			webphoneRejectTimer.cancel();
		}
		interpreter.setNumMissedCalls(0);
		TLVXManager.getSession().saveOrUpdate(interpreter);
		if (interpreterDialog != null) {
			Dialog terminateDialogTemp = interpreterDialog;
			interpreterDialog = null;
			terminateDialog(terminateDialogTemp);
			try {
				Thread.currentThread().sleep(750);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		interpreterValidated = true;
		String languageId = TLVXManager.callSessionManager.findLanguageId(previousInterpreterLanguage);
		if (interpreter == null) {
			System.out.println("isnull!");
		}
		saveCallEvent(CallEvent.INTERPRETER_ON_CALL, String.valueOf(interpreter.getInterpreterId()), languageId);
		call.setLanguage(previousInterpreterLanguage);
		startInterpreterTime();

		if (false == completeCallPressed && false == ivrDialogStarted) {
			if (LOG.isDebugEnabled())
				LOG.debug("interpreter dialog exited, not complete call, putting interpreter on hold");
			interpreterOnHold(true);
		} else {
			callerOffHold();
			conferenceConnection(interpreterConnection, "full", "#", null);

			for (String thirdPartyOnHoldDialogKey : thirdPartyOnHoldDialogs.keySet()) {
				LOG.debug("thirdPartyOnHoldDialog connectionId key = "+thirdPartyOnHoldDialogKey);
				thirdPartyOffHold(thirdPartyOnHoldDialogKey);
			}

			saveCallEvent(CallEvent.CUSTOMER_INTERPRETER_CONNECT, null);
			interpreterAttempts = 0;
			interpreterIsBillable();
			if (interpreter.getOnWebSite() != null) {
				try {
					TLVXManager.interpreterManager.getINTClient(interpreter.getOnWebSite())
						.interpreterOffHold(getInterpreterCallInfo());
				} catch (Exception e) {
					LOG.error("Unable to send message to interpreter server, log out interpreter",e);
					interpreter.setOnWebSite(null);
					interpreter.setWebPhone(false);
					interpreter.setVideo(false);
					TLVXManager.getSession().saveOrUpdate(interpreter);
				}
			}
		}
		interpreterOnline = true;
	}
	
	public synchronized void rejectInterpreterFromWeb(InterpreterInfo interpreterInfo) {
		if (webphoneRejectTimer != null) {
			webphoneRejectTimer.cancel();
		}
		if (interpreterDialog != null) {
			Dialog terminateDialogTemp = interpreterDialog;
			interpreterDialog = null;
			terminateDialog(terminateDialogTemp);
			try {
				Thread.currentThread().sleep(750);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		interpreterValidated = false;
		String onlineStatus = "unknown";
		if (interpreter != null) {
			onlineStatus = ""+interpreter.getActiveSession();
		}
		saveCallEvent(CallEvent.INTERPRETER_IVR_EXIT_REASON, interpreterConnection.getDestination(), ""+interpreterInfo.rejectReason+", activeSession = "+onlineStatus);
		rejectReason = interpreterInfo.rejectReason;
		if (interpreterConnection != null && interpreterConnection.getState() != Connection.ConnectionState.DISCONNECTED) {
			hangupConnection(interpreterConnection);
		}
	}
	
	public synchronized void onDialogExit(Map request) {
		if (LOG.isDebugEnabled()) LOG.debug("onDialogExit received :" + request);
			Dialog finishedDialog = currentDialogs.remove((String) request.get("dialogid"));
			String connectionId = (String) request.get("connectionid");
	
			if (finishedDialog == interpreterPoundDialog) {
				String action = (String) request.get("action");
				String dialogreason = (String) request.get("dialogreason");
				//System.out.println("Pound Diaglog Exit: "+action+" "+dialogreason);
				if (action == null || "agent".equals(action)) {
					interpreterOnHold(true);
					callerOnHold(false);
					call.setStatus(CallSessionManager.CALL_STATUS_QUEUED);
					call.setLastReason("Interpreter sent call back to agent with #.");
					call = TLVXManager.callDAO.save(call);
					saveCallEvent(CallEvent.CUSTOMER_QUEUED, "Interpreter #");
				} else if ("hangupthirdparty".equals(action)) {
					interpreterJoinAction = "hangupthirdparty";
					interpreterOffHold();
					// loop through and hang up third party connections
//					for(Connection tpc : thirdPartyConnections.values()) {
//						hangupConnection(tpc);
//					}
					dialingInterpreter = false;
				} else if ("dialthirdparty".equals(action)) {
					interpreterOffHold();
					String phonenumber = (String) request.get("phonenumber");
					//System.out.println("dialing "+phonenumber);
					interpreterJoinAction = "dialthirdparty";
					interpreterJoinActionPhoneNumber = phonenumber;
					//connectThirdParty(phonenumber);
				} else if ("return".equals(action)) {
					interpreterOffHold();
				}
			} else if (finishedDialog == interpreterDialog) {
				interpreterDialog = null;
				if (interpreterConnection != null && 
						(interpreterConnection.getState() != Connection.ConnectionState.DISCONNECTED
						 && interpreterConnection.getState() != Connection.ConnectionState.DISCONNECTING)) {
	
					interpreterValidated = Boolean.parseBoolean((String) request.get("validated"));
					if (LOG.isDebugEnabled()) {
						LOG.debug(" 3 interpreterValidated = "
								+ interpreterValidated + " callId = "
								+ call.getId() + " this =" + this);
					}
	
					if (true == interpreterValidated) {
						String languageId = TLVXManager.callSessionManager.findLanguageId(previousInterpreterLanguage);
						saveCallEvent(CallEvent.INTERPRETER_ON_CALL, String.valueOf(interpreter.getInterpreterId()), languageId);
						call.setLanguage(previousInterpreterLanguage);
						startInterpreterTime();
						interpreter.setNumMissedCalls(0);
						TLVXManager.getSession().saveOrUpdate(interpreter);
						if (false == completeCallPressed && false == ivrDialogStarted) {
							if (LOG.isDebugEnabled())
								LOG.debug("interpreter dialog exited, not complete call, putting interpreter on hold");
							interpreterOnHold(true);
						} else {
							callerOffHold();
							conferenceConnection(interpreterConnection, "full", "#", null);
	
							for (String thirdPartyOnHoldDialogKey : thirdPartyOnHoldDialogs.keySet()) {
								LOG.debug("thirdPartyOnHoldDialog connectionId key = "+thirdPartyOnHoldDialogKey);
								thirdPartyOffHold(thirdPartyOnHoldDialogKey);
							}
	
							saveCallEvent(CallEvent.CUSTOMER_INTERPRETER_CONNECT, null);
							interpreterAttempts = 0;
							interpreterIsBillable();
							if (interpreter.getOnWebSite() != null) {
								try {
									TLVXManager.interpreterManager.getINTClient(interpreter.getOnWebSite())
										.interpreterOffHold(getInterpreterCallInfo());
								} catch (Exception e) {
									LOG.error("Unable to send message to interpreter server, log out interpreter",e);
									interpreter.setOnWebSite(null);
									interpreter.setWebPhone(false);
									interpreter.setVideo(false);
									TLVXManager.getSession().saveOrUpdate(interpreter);
								}
							}
						}
						interpreterOnline = true;
					} else {
						String onlineStatus = "unknown";
						if (interpreter != null) {
							onlineStatus = ""+interpreter.getActiveSession();
						}
						String dialogReason = (String) request.get("dialogreason");
						if (dialogReason != null && dialogReason.startsWith("pressed")) {
							//pressed something other than 1
							TLVXManager.interpreterDAO.logOutInterpreter(interpreter.getInterpreterId());
							dialogReason+=" & logged out";
						}
						saveCallEvent(CallEvent.INTERPRETER_IVR_EXIT_REASON, interpreterConnection.getDestination(), ""+dialogReason+", activeSession = "+onlineStatus);
						if (interpreterConnection != null && interpreterConnection.getState() != Connection.ConnectionState.DISCONNECTED) {
							hangupConnection(interpreterConnection);
						}
						if (interpreter.getOnWebSite() != null) {
							try {
								TLVXManager.interpreterManager.getINTClient(interpreter.getOnWebSite())
									.interpreterNotAcceptingCalls(getInterpreterCallInfo());
							} catch (Exception e) {
								LOG.error("Unable to send message to interpreter server, log out interpreter",e);
								interpreter.setOnWebSite(null);
								interpreter.setWebPhone(false);
								interpreter.setVideo(false);
								TLVXManager.getSession().saveOrUpdate(interpreter);
							}
						}
					}
				}
			} else if (finishedDialog == callerOnHoldDialog) {
				callerOnHoldDialog = null;
				callerOnHold = false;
				if (customerConnection != null && 
						(customerConnection.getState() != Connection.ConnectionState.DISCONNECTED
						 && customerConnection.getState() != Connection.ConnectionState.DISCONNECTING)) {

					conferenceConnection(customerConnection, "full", null, null);
					Map data = new HashMap();
					data.put("status", "connected");
					data.put("muted", customerConnection.isMuted());
					dispatchAgentMessage(data, AgentManager.TL_CUSTOMER_STATUS_CHANGE);
				}
			} else /* if (finishedDialog == callerInitialDialog) {
				callerOnHoldDialog = null;
				callerOnHold = false;
				callerInitialDialog = null;
				if (customerConnection != null && 
						(customerConnection.getState() != Connection.ConnectionState.DISCONNECTED
						 && customerConnection.getState() != Connection.ConnectionState.DISCONNECTING)) {

					conferenceConnection(customerConnection, "full", null, null);
					Map data = new HashMap();
					data.put("status", "connected");
					data.put("muted", customerConnection.isMuted());
					data.put("priority", call.isPriorityCall());
					dispatchAgentMessage(data, AgentManager.TL_CUSTOMER_STATUS_CHANGE);
				}
			} else */ if (finishedDialog == interpreterOnHoldDialog) {
				interpreterOnHoldDialog = null;
				interpreterOnHold = false;
				if (interpreterConnection != null && 
						(interpreterConnection.getState() != Connection.ConnectionState.DISCONNECTED
						 && interpreterConnection.getState() != Connection.ConnectionState.DISCONNECTING)) {

					if (interpreter != null) {
						conferenceConnection(interpreterConnection, "full", "#", null);
		
						Map data = new HashMap();
						data.put("status", "connected");
						data.put("muted", interpreterConnection.isMuted());
						if (interpreter != null) {
							data.put("name", interpreter.getFirstName() + " "
									+ interpreter.getLastName());
						}
						dispatchAgentMessage(data,
								InterpreterManager.TL_INTERPRETER_STATUS_CHANGE);
					}
				}
			} else if (thirdPartyOnHoldDialogs.containsKey(connectionId)) {
				//thirdPartyOnHoldDialog = null;
				LOG.debug("onDialogExit third party removing connectionId from dialogs :" + connectionId);
				thirdPartyOnHoldDialogs.remove(connectionId);
				Connection thirdPartyConnection = thirdPartyConnections.get(connectionId);
				if (thirdPartyConnection != null && 
						(thirdPartyConnection.getState() != Connection.ConnectionState.DISCONNECTED
						 && thirdPartyConnection.getState() != Connection.ConnectionState.DISCONNECTING)) {
					conferenceConnection(thirdPartyConnection, "full", null, null);
					Map data = new HashMap();
					data.put("id", thirdPartyConnection.getConnectionId());
					data.put("status", "connected");
					data.put("muted", thirdPartyConnection.isMuted());
					data.put("name", SipUtil.getPhoneNumber(thirdPartyConnection.getDestination()));
					dispatchAgentMessage(data, AgentManager.TL_THIRDPARTY_STATUS_CHANGE);
				}
			} else  if (finishedDialog == callerInitialDialog) {
				callerInitialDialog = null;
	
				if (null != customerConnection) {
					//if (previousInterpreterLanguage == null) {
						conferenceConnection(customerConnection, "full", null, null);
					//}
					Map data = new HashMap();
					data.put("status", "connected");
					data.put("muted", customerConnection.isMuted());
					data.put("priority", call.isPriorityCall());
					dispatchAgentMessage(data,
							AgentManager.TL_CUSTOMER_STATUS_CHANGE);
				}
			} else if (finishedDialog == ivrDialog) {
				ivrDialog = null;
				saveCallEvent(CallEvent.IVR_DISCONNECT, null, null);
				String accesscode = (String) request.get("accesscode");
				String deptcode = (String) request.get("deptcode");// .equals("null")?"":request.getString("deptcode");
				String language = (String) request.get("language");// .equals("null")?"":request.getString("language");
				String calltype = (String) request.get("calltype");// .equals("null")?"":request.getString("calltype");
				dtmfPin = (String) request.get("dtmfpin");
				LOG.debug("DTMFPIN = "+dtmfPin);
	
				if (isConnected(customerConnection)) {
					if (("2").equals(calltype)) {
						try {
							if (LOG.isDebugEnabled())
								LOG.debug("Redirecting call from IVR, calltype = " + calltype);
							Map parameters = buildSessionDataObject();
							parameters.put("callerid", "telelanguage");
							parameters.put("dest","sip:"+ TLVXManager.callSessionManager.getIvrTransferTarget1()+ "@"+ TLVXManager.callSessionManager.getProxyAddress());
							parameters.put("connectionid", customerConnection.getConnectionId());
							TLVXManager.ccxmlManager.redirect(parameters);
						} catch (Exception e) {
							LOG.warn(
									"Exception caught trying to redirect call for "
											+ this, e);
							handleServerError();
						}
					} else {
						if (false == ("").equals(accesscode)) {
							saveCallEvent(CallEvent.ACCESS_CODE_CONFIRMED,
									accesscode, null);
							customer = TLVXManager.customerDAO
									.findByCode(accesscode);
							if (customer != null) {
								call = TLVXManager.callDAO.findById(call.getId());
								call.setAccessCode(accesscode);
								call.setCustomer(customer.getCustomerId());
								call.setSubscriptionCode(customer
										.getSubscriptionCode());
								if (customer.getSendToAgent() != null
										&& customer.getSendToAgent()) {
									call.setLastReason("From IVR:  DO NOT CONNECT");
								}
								if (false == ("").equals(language)
										&& false == ("null").equals(language)) {
									interpreterLanguage = language;
									saveCallEvent(
											CallEvent.LANGUAGE_CODE_CONFIRMED,
											language, null);
								}
							}
							if (false == ("").equals(deptcode)
									&& false == ("dontask").equals(deptcode)) {
								call.setDepartmentCode(deptcode);
								saveCallEvent(CallEvent.DEPT_CODE_CONFIRMED,
										deptcode, null);
							}
							call = TLVXManager.callDAO.save(call);
						}
	
						if (interpreterLanguage == null || ("").equals(deptcode)
								|| ("null").equals(deptcode)) {
							if (interpreterLanguage != null
									&& !interpreterLanguage.equals("")
									&& !interpreterLanguage.equals("null"))
								call.setLanguage(interpreterLanguage);
							callerOnHold(false);
							call = TLVXManager.callDAO.findById(call.getId());
							call.setStatus(CallSessionManager.CALL_STATUS_QUEUED);
							call = TLVXManager.callDAO.save(call);
							saveCallEvent(CallEvent.CUSTOMER_QUEUED, null);
						} else {
							callerOnHold(false);
							call = TLVXManager.callDAO.findById(call.getId());
							call.setLanguage(interpreterLanguage);
							if (null != customerConnection
									&& customerConnection.getState() == Connection.ConnectionState.CONNECTED
									&& !interpreterOnline) {
								TLVXManager.interpreterManager
										.dispatchCallToAvailableInterpreter(call,
												null, interpreterLanguage,
												previousInterpreterGender, previousInterpreterVideo, null, completeCallPressed);
							}
						}
					}
				}
	
				if (LOG.isDebugEnabled())
					LOG.debug("finished IVR dialog, accessCode = " + accesscode
							+ ", deptcode = " + deptcode + ", language = "
							+ language + ", calltype = " + calltype+", dtmfpin = "+dtmfPin);
			} else {
				// sync error conference in connection
				LOG.debug("SYNC attempting to recover dialog missing");
				if (connectionId != null) {
					if (customerConnection != null
							&& (connectionId.equals(customerConnection.getConnectionId()) )
							&& isConnected(customerConnection)) {
						LOG.debug("SYNC attempting to conference in customer");
						conferenceConnection(customerConnection, "full", null, null);
					} else if (interpreterConnection != null
							&& (connectionId.equals(interpreterConnection.getConnectionId()))
							&& isConnected(interpreterConnection)) {
						LOG.debug("SYNC attempting to conference in interpreter");
						conferenceConnection(interpreterConnection, "full", "#", null);
					}
				}
			}
		}

	public synchronized void onErrorDialogNotStarted(Map<String, String> request) {
		if (LOG.isDebugEnabled())
			LOG.debug("onErrorDialogNotStarted received for " + request);
			processingDialog = null;
			processNextDialog();
	}

	public synchronized void onDialogStarted(Map request) {
		if (LOG.isDebugEnabled())
			LOG.debug("onDialogStarted received for " + request);

		String dialogId = (String) request.get("dialogid");
		String connectionId = (String) request.get("connectionid");
		if (connectionId != null && connectionId.length() > 5) { // ignore the conference dialog to record call
			Dialog dialog = currentDialogs.get(dialogId);
			if (dialog == null) {
				if (processingDialog != null) {
					processingDialog.setDialogId(dialogId);
					dialog = processingDialog;
				} else {
					dialog = new Dialog(request);
				}
			}
			dialog.setStarted(true);
			currentDialogs.put(dialogId, dialog);
			if (dialog.getFinished() == true) {
				terminateDialog(dialog);
			}
			if (videoStarting && dialog == callerInitialDialog) {
				videoStarting = false;
				callerInitialDialog = dialog;
			} else if (callerInitialDialog != null && dialog == callerInitialDialog) {
				LOG.debug("Welcome dialog started, queuing the call to an agent " + call.getId()+ " "+call.getCallSessionId());
				call.setStatus(CallSessionManager.CALL_STATUS_QUEUED);
				call = TLVXManager.callDAO.save(call);
				saveCallEvent(CallEvent.CUSTOMER_QUEUED, null);
			}
			if (interpreterOnHoldDialog != null && dialog == interpreterOnHoldDialog) {
				if (requestInterpreterOffHoldWhenDialogStarts) {
					LOG.debug("onDialogStarted requestInterpreterOffHoldWhenDialogStarts is true, taking interpreter off hold");
					interpreterOffHold();
					requestInterpreterOffHoldWhenDialogStarts = false;
				}
			}
			if (callerOnHoldDialog != null && dialog == callerOnHoldDialog) {
				if (requestCallerOffHoldWhenDialogStarts) {
					LOG.debug("onDialogStarted requestCallerOffHoldWhenDialogStarts is true, taking caller off hold");
					callerOffHold();
					requestCallerOffHoldWhenDialogStarts = false;
				} else if (completeCallPressed && interpreterConnection != null && 
						interpreterConnection.getState() == ConnectionState.CONNECTED && call.getStatus() != CallSessionManager.CALL_STATUS_QUEUED &&
						call.getStatus() != CallSessionManager.CALL_STATUS_FORWARDED_TO_AGENT && interpreterValidated) {
					LOG.debug("SYNC CallerOnHoldDialog started after complete call and interpreter is connected ");
					callerOffHold();
				}
			}
//			for (Dialog thirdPartyOnHoldDialog : thirdPartyOnHoldDialogs.values()) {
//				if (thirdPartyOnHoldDialog.getConnectionId().equals(connectionId)) {
//					LOG.debug("onDialogStarted found third party on hold dialog, adding to on hold third parties");
//					thirdPartyOnHoldDialogs.put(connectionId, dialog);
//				}
//			}
			processNextDialog();
		} else {
			if (LOG.isDebugEnabled())
				LOG.debug("onDialogStarted received for the recordcall "
						+ request);
		}
	}
	
	public boolean saveVideoCustomerInfo(VideoCustomerInfo videoCustomerInformation) {
		synchronized(this) {
			boolean validated = true;
			if (LOG.isDebugEnabled())
				LOG.debug("saveVideoCustomerInfo:" + videoCustomerInformation);
	
			call = TLVXManager.callDAO.findById(call.getId());
			call.setDepartmentCode(videoCustomerInformation.departmentCode);
			call.setLanguage(videoCustomerInformation.language);
			setPreviousInterpreterGender(videoCustomerInformation.interpreterGender);
			call = TLVXManager.callDAO.save(call);

			List<CustomerOption> options = TLVXManager.customerOptionDAO.findByCustomerId(call.getCustomer());
			if (options.size() == videoCustomerInformation.questionInputs.size()) {
				for (int i=0; i<options.size(); i++) {
					String value = videoCustomerInformation.questionInputs.get(i);
					CustomerOption option = options.get(i);
					if (value != null && value.length()>=3) {
						CustomerOptionData d = TLVXManager.customerOptionDataDAO
								.findByCallIdOptionId(""+call.getId(),
										option.getId());
						if (d == null) {
							d = new CustomerOptionData();
							d.setCustomerOptionId(option.getOptionContentId());
							d.setCall(call);
						}
						d.setValue(value);
						LOG.info("saving saveVideoCustomerInfo looping = " + d.toString());
						TLVXManager.customerOptionDataDAO.save(d);
					} else validated = false;
				}
			} else validated = false;
			return validated;
		}
	}

	public void saveCallInformation(CallInformation callInformation) {
		synchronized(this) {
			List<CustomerOptionData> optionData = TLVXManager.customerOptionDataDAO
					.findByCallId(callInformation.callId);
			if (LOG.isDebugEnabled())
				LOG.debug("saving callInfo:" + callInformation);
	
			call = TLVXManager.callDAO.findById(call.getId());
			call.setDepartmentCode(callInformation.departmentCode);
			call.setLanguage(callInformation.language);
			setPreviousInterpreterGender(callInformation.interpreterGender);
			call = TLVXManager.callDAO.save(call);
	
			for (QuestionItem item : callInformation.questions) {
				if (item.getValue() != null) {
					CustomerOptionData d = TLVXManager.customerOptionDataDAO
							.findByCallIdOptionId(callInformation.callId,
									item.getOptionId());
					if (d == null) {
						d = new CustomerOptionData();
						d.setCustomerOptionId(item.getOptionId());
						d.setCall(call);
					}
					d.setValue(item.getValue());
					LOG.info("saving options looping = " + d.toString());
					TLVXManager.customerOptionDataDAO.save(d);
				}
			}
		}
	}

	public synchronized Boolean completeCall(boolean hangupCaller, Call call, String language) {
			completeCallNoInterpreters = false;
			interpreterLanguage = language;
			if (LOG.isDebugEnabled())
				LOG.debug("completeCall request, hangupCaller = "
						+ hangupCaller + ", completeCallPressed = "
						+ completeCallPressed);

			if (completeCallPressed == true) {
				if (LOG.isDebugEnabled())
					LOG.debug("agent already pressed complete call! ignoring this duplicate request!!!");
				return false;
			}

			completeCallPressed = true;
			ivrDialogStarted = true;

			if (false == hangupCaller && call != null) {
				call.setLastReason(null);
				TLVXManager.callDAO.save(call);
			}
	
			if (true == hangupCaller) {
				hangupConnection(customerConnection);
			} else {
				saveCallEvent(CallEvent.AGENT_COMPLETE_CALL, interpreterLanguage, String.valueOf(agentId));
	
				if ((null != interpreterConnection) && (interpreterConnection.getState() == Connection.ConnectionState.CONNECTED)) {
				LOG.debug("interpreter connected callerOnHold = "+callerOnHold+" interpreterOnHold = "+interpreterOnHold+" thirdPartyOnHoldDialogs="+thirdPartyOnHoldDialogs);
					if (null == interpreterDialog || manualInterpreterDial) {
						if (callerOnHold) {
							callerOffHold();
						}
						if (interpreterOnHold) {
							// hack to play sound on browser (interpreter conf-in.wav on WebRTC was silent)
							interpreterBrowserSound = interpreter.getWebPhone();
							interpreterOffHold();
						}
						for (String thirdPartyOnHoldDialogKey : thirdPartyOnHoldDialogs.keySet()) {
							LOG.debug("thirdPartyOnHoldDialog connectionId key = "+thirdPartyOnHoldDialogKey);
							thirdPartyOffHold(thirdPartyOnHoldDialogKey);
						}
	
						saveCallEvent(CallEvent.CUSTOMER_INTERPRETER_CONNECT, null);
						interpreterIsBillable();
						interpreterAttempts = 0;
					} else {
						callerOnHold(true);
					}
				} else {
					callerOnHold(true);
	
					if (false == dialingInterpreter
							&& null != customerConnection
							&& customerConnection.getState() == Connection.ConnectionState.CONNECTED) {
						TLVXManager.interpreterManager
								.dispatchCallToAvailableInterpreter(call, null,
										interpreterLanguage,
										previousInterpreterGender, previousInterpreterVideo, null, completeCallPressed);
						if (interpreter == null) {
							completeCallNoInterpreters = true;
						}
					}
				}
				if (!completeCallNoInterpreters) {
					hangupConnection(agentConnection);
				} else {
					completeCallPressed = false;
				}
			}
		return completeCallPressed;
	}

	public void transferCall(String dest) {
		if (LOG.isDebugEnabled())
			LOG.debug("transferCall request, dest = " + dest + ", for " + this);

		if (null != dest) {
			transferDestination = dest;
			dialingTransfer = true;
			
			if (agentConnection != null && true == isConnected(agentConnection)) {
				hangupConnection(agentConnection);
			}

			if (interpreterConnection != null
					&& true == isConnected(interpreterConnection)) {
				hangupConnection(interpreterConnection);
			}

			for(Connection thirdPartyConnection : thirdPartyConnections.values()) {
				if (thirdPartyConnection != null
						&& true == isConnected(thirdPartyConnection)) {
					hangupConnection(thirdPartyConnection);
				}
			}

			callerOnHold(false);

			Map parameters = buildSessionDataObject();
			parameters.put("callerid", "telelanguage");
			if (TLVXManager.callSessionManager.getPstnOutboundAddress() != null) {
				parameters.put("dest", "sip:" + TLVXManager.callSessionManager.getPstnOutboundPrefix() + dest + "@" + TLVXManager.callSessionManager.getPstnOutboundAddress());
			} else {
				parameters.put("dest", "sip:" + dest + "@" + TLVXManager.callSessionManager.getProxyAddress());
			}

			try {
				parameters.put("timeout",
						TLVXManager.callSessionManager.getThirdPartyTimeout());
				//parameters.put("connectionid", "transfer");
				TLVXManager.ccxmlManager.createCall(parameters);
			} catch (Exception e) {
				LOG.warn("Exception caught trying to create call for " + this,
						e);
				handleServerError();
			}
		}
	}

	public void agentRejected() {
		if (LOG.isDebugEnabled())
			LOG.debug("agentRejected request for " + this);

		hangupConnection(agentConnection);
		agentRejected = true;
		saveCallEvent(CallEvent.AGENT_REJECT, String.valueOf(agentId));
	}

	public synchronized void requeueCall(Call callPassed) {
		if (LOG.isDebugEnabled())
			LOG.debug("requeueCall request for " + this);


		// saveCallInformation(callPassed.getLanguage(),
		// callPassed.getDepartmentCode(), null);

		hangupConnection(agentConnection);
		requeueCall = true;
		completeCallNoInterpreters = false;
		saveCallEvent(CallEvent.AGENT_REQUEUE_CALL, null);

		callerOnHold(false);

		if(interpreterValidated) {
			if (isConnected(interpreterConnection)) {
				interpreterOnHold(false);
			}
		} else {
			disconnectInterpreter("Agent requeued call");
		}

		for(Connection thirdPartyConnection : thirdPartyConnections.values())
		if (isConnected(thirdPartyConnection)) {
			thirdPartyOnHold(thirdPartyConnection.getConnectionId());
		}
		
		try {
			Thread.sleep(500);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// refresh the call (in case the reserved agent was set)
		call = TLVXManager.callDAO.findCallByCallSessionId(callPassed.getCallSessionId());
		if (agentConnection == null || agentConnection.getState() == ConnectionState.DISCONNECTED) {
			call.setStatus(CallSessionManager.CALL_STATUS_QUEUED);
			TLVXManager.callDAO.save(call);
		}
	}

	public void callerOnHold(boolean completeCall) {
		if (LOG.isDebugEnabled())
			LOG.debug("callerOnHold request for " + this);
		
		if (callerOnHoldDialog == null && callerInitialDialog == null) {
			for(Dialog dialog : currentDialogs.values()) {
				if (!dialog.getFinished() && customerConnection.getConnectionId().equals(dialog.getParameters().get("connectionid"))) {
					System.out.println("Found dialog setting callerOnHoldDialog "+customerConnection.getConnectionId());
					callerOnHoldDialog = dialog;
					callerOnHold = true;
					callerOffHold();
					return;
				}
			}
		}

		if (callerOnHoldDialog == null) {
			unconferenceConnection(customerConnection);

			Map data = new HashMap();
			data.put("status", "connected");
			data.put("muted", customerConnection.isMuted());
			dispatchAgentMessage(data, AgentManager.TL_CUSTOMER_STATUS_CHANGE);

			Map parameters = buildSessionDataObject();
			
			callerOnHold = true;
			LOG.debug(videoInterpreterRequest);
			if (videoInterpreterRequest != null && videoInterpreterRequest.videoCustomerInfo.videoServer != null) {
				try {
					TLVXManager.customerUserManager.getVideoAPIClient(videoInterpreterRequest.videoCustomerInfo.videoServer).videoCallOnHold(getVideoCallInfo());
				} catch (Exception e) {
					LOG.error("Unable to send message to video call server, ending call",e);
					hangupConnection(customerConnection);
					return;
				}
			} 
			LOG.debug("callerInitialDialog: "+callerInitialDialog);
			if (callerInitialDialog == null || callerInitialDialog.getFinished()) {
				if (customer != null && customer.getHoldMusicWave() != null && !"".equals(customer.getHoldMusicWave())) {
					String promptURL = "";
					promptURL = TLVXManager.callSessionManager.getCustomerPromptBaseURL()+ "/" + customer.getHoldMusicWave();
					parameters.put("src", TLVXManager.callSessionManager.getCustomerCompleteCallHoldURI() + "?promptURL=" + promptURL);
				} else {
					parameters.put("src", "telelanguage/default-hold-vxml.xml");
				}
				parameters.put("connectionid", customerConnection.getConnectionId());
				callerOnHoldDialog = new Dialog(parameters);
				holdTime = new Date();
				startDialog(callerOnHoldDialog);
			}
		} else {
			LOG.debug("callerOnHold, ignoring caller on hold dialog");
		}
	}
	
	boolean requestCallerOffHoldWhenDialogStarts = false;

	public synchronized void callerOffHold() {
		if (LOG.isDebugEnabled())
			LOG.debug("callerOffHold request for " + this);
		
		if (callerInitialDialog != null && !callerInitialDialog.getStarted()) {
			LOG.debug("Caller Off Hold Request delayed until Dialog Starting.");
			callerInitialDialog.setFinished(true);
		} else if (callerOnHoldDialog != null && !callerOnHoldDialog.getStarted()) {
			LOG.debug("Caller Off Hold Request delayed until Dialog Starting.");
			callerOnHoldDialog.setFinished(true);
		} else if (callerOnHoldDialog != null && callerOnHoldDialog.getDialogId() != null && currentDialogs.containsKey(callerOnHoldDialog.getDialogId())) {
			if (videoInterpreterRequest != null && videoInterpreterRequest.videoCustomerInfo.videoServer != null) {
				try {
					TLVXManager.customerUserManager.getVideoAPIClient(videoInterpreterRequest.videoCustomerInfo.videoServer).videoCallOffHold(getVideoCallInfo());
				} catch (Exception e) {
					LOG.error("Unable to send message to video call server, ending call",e);
					hangupConnection(customerConnection);
				}
			}
			terminateDialog(callerOnHoldDialog);
		} else if (callerInitialDialog != null && callerInitialDialog.getDialogId() != null && currentDialogs.containsKey(callerInitialDialog.getDialogId())) {
			if (videoInterpreterRequest != null && videoInterpreterRequest.videoCustomerInfo.videoServer != null) {
				try {
					TLVXManager.customerUserManager.getVideoAPIClient(videoInterpreterRequest.videoCustomerInfo.videoServer).videoCallOffHold(getVideoCallInfo());
				} catch (Exception e) {
					LOG.error("Unable to send message to video call server, ending call",e);
					hangupConnection(customerConnection);
				}
			}
			terminateDialog(callerInitialDialog);
		} else {
			if (callerOnHoldDialog != null && callerOnHoldDialog.getDialogId() == null) {
				// interpreterOnHold means on hold was requested but dialog has stared, set it to false
				LOG.debug("callerOffHold request, waiting for dialog to start.");
				requestCallerOffHoldWhenDialogStarts = true;
			} else {
				callerOnHold = false;
				if (videoInterpreterRequest != null && videoInterpreterRequest.videoCustomerInfo.videoServer != null) {
					try {
						TLVXManager.customerUserManager.getVideoAPIClient(videoInterpreterRequest.videoCustomerInfo.videoServer).videoCallOffHold(getVideoCallInfo());
					} catch (Exception e) {
						LOG.error("Unable to send message to video call server, ending call",e);
						hangupConnection(customerConnection);
					}
				}
				terminateDialog(callerOnHoldDialog);
//				conferenceConnection(customerConnection, "full", null, null);
//				Map data = new HashMap();
//				data.put("status", "connected");
//				data.put("muted", customerConnection.isMuted());
//				dispatchAgentMessage(data, AgentManager.TL_CUSTOMER_STATUS_CHANGE);
			}
		}
	}
	
	boolean requestInterpreterOffHoldWhenDialogStarts = false;

	public synchronized void interpreterOffHold() {
		if (LOG.isDebugEnabled())
			LOG.debug("interpreterOffHold request, interpreterOnHoldDialog = "
					+ interpreterOnHoldDialog);
		
		if (interpreter == null) {
			if (LOG.isDebugEnabled())
				LOG.debug("Interpreter is already null");
			return;
		}

		interpreter = TLVXManager.interpreterDAO.findInterpreterByID(interpreter.getInterpreterId());
		
		if (interpreter.getOnWebSite() != null) {
			try {
				TLVXManager.interpreterManager.getINTClient(interpreter.getOnWebSite())
					.interpreterOffHold(getInterpreterCallInfo());
				interpreterBrowserSound = false;
			} catch (Exception e) {
				LOG.error("Unable to send message to interpreter server, log out interpreter",e);
				interpreter.setOnWebSite(null);
				interpreter.setWebPhone(false);
				interpreter.setVideo(false);
				TLVXManager.getSession().saveOrUpdate(interpreter);
			}
		}
		
		if (interpreterOnHoldDialog != null && !interpreterOnHoldDialog.getStarted()) {
			LOG.debug("Interpreter Off Hold Request delayed until Dialog Starting.");
			interpreterOnHoldDialog.setFinished(true);
		} else if (interpreterOnHoldDialog != null && interpreterOnHoldDialog.getDialogId() != null && currentDialogs.containsKey(interpreterOnHoldDialog.getDialogId())) {
			terminateDialog(interpreterOnHoldDialog);
		} else {
			if (interpreterOnHoldDialog != null && interpreterOnHoldDialog.getDialogId() == null) {
				// interpreterOnHold means on hold was requested but dialog has stared, set it to false
				LOG.debug("interpreterOffHold request, waiting for dialog to start.");
				requestInterpreterOffHoldWhenDialogStarts = true;
			} else {
				interpreterOnHoldDialog = null;
				interpreterOnHold = false;
				if (interpreter != null) {
					conferenceConnection(interpreterConnection, "full", "#", null);
				}
				
				Map data = new HashMap();
				data.put("status", "connected");
				data.put("muted", interpreterConnection.isMuted());
				if (interpreter != null) {
					data.put("name", interpreter.getFirstName() + " "
							+ interpreter.getLastName());
				}
				dispatchAgentMessage(data, InterpreterManager.TL_INTERPRETER_STATUS_CHANGE);
			}
		}
	}

	public synchronized void interpreterOnHold(boolean start) {
		if (LOG.isDebugEnabled())
			LOG.debug("interpreterOnHold request");
		
		interpreter = TLVXManager.interpreterDAO.findInterpreterByID(interpreter.getInterpreterId());
		
		if (interpreter.getOnWebSite() != null) {
			try {
				TLVXManager.interpreterManager.getINTClient(interpreter.getOnWebSite())
					.interpreterOnHold(getInterpreterCallInfo());
				interpreterOnHold = true;
			} catch (Exception e) {
				LOG.error("Unable to send message to interpreter server, log out interpreter",e);
				interpreter.setOnWebSite(null);
				interpreter.setWebPhone(false);
				interpreter.setVideo(false);
				TLVXManager.getSession().saveOrUpdate(interpreter);
			}
		}
		
		if (interpreterOnHoldDialog == null) {
			for(Dialog dialog : currentDialogs.values()) {
				if (interpreterConnection.getConnectionId().equals(dialog.getParameters().get("connectionid"))) {
					interpreterOnHoldDialog = dialog;
					interpreterOnHold = true;
					interpreterOffHold();
					return;
				}
			}
		}

		if (interpreterOnHoldDialog == null) {
			if (false == start) {
				unconferenceConnection(interpreterConnection);
			}

			Map data = new HashMap();
			data.put("status", "connected");
			data.put("muted", interpreterConnection.isMuted());
			if (interpreter != null) {
				data.put(
						"name",
						interpreter.getFirstName() + " "
								+ interpreter.getLastName());
			}
			dispatchAgentMessage(data,
					InterpreterManager.TL_INTERPRETER_STATUS_CHANGE);

			interpreterOnHold = true;
			if (interpreter.getWebPhone()) {
				// send command to interpreter server
				if (interpreter.getOnWebSite() != null) {
					TLVXManager.interpreterManager.getINTClient(interpreter.getOnWebSite())
						.interpreterOnHold(getInterpreterCallInfo());
					interpreterOnHold = true;
				}
			} else {
				Map parameters = buildSessionDataObject();
				parameters.put("src", TLVXManager.callSessionManager
						.getInterpreterHoldDialogURI());
				parameters.put("connectionid",
						interpreterConnection.getConnectionId());
				interpreterOnHoldDialog = new Dialog(parameters);
				startDialog(interpreterOnHoldDialog);
			}
		} else {
			LOG.debug("ignoring interpreter on hold dialog");
		}
	}

	public void markAsInterpreter() {
		if (LOG.isDebugEnabled())
			LOG.debug("markAsInterpreter request for " + this);

		if (null == interpreterConnection && isConnected(customerConnection)) {
			interpreterConnection = customerConnection;
			customerConnection = null;
			customer = null;

			Map data = new HashMap();
			data.put("status", "idle");
			dispatchAgentMessage(data, AgentManager.TL_CUSTOMER_STATUS_CHANGE);

			data = new HashMap();
			data.put("status", "connected");
			data.put("muted", interpreterConnection.isMuted());
			dispatchAgentMessage(data,
					InterpreterManager.TL_INTERPRETER_STATUS_CHANGE);
		}
	}

	public void thirdPartyOnHold(String connectionId) {
		if (LOG.isDebugEnabled())
			LOG.debug("thirdPartyOnHold request for " + connectionId + " " + this);
		
		Dialog thirdPartyOnHoldDialog = thirdPartyOnHoldDialogs.get(connectionId);
		Connection thirdPartyConnection = thirdPartyConnections.get(connectionId);

		if (thirdPartyOnHoldDialog == null) {
			for(Dialog dialog : currentDialogs.values()) {
				if (thirdPartyConnection.getConnectionId().equals(dialog.getParameters().get("connectionid"))) {
					thirdPartyOnHoldDialog = dialog;  // TODO find others setters and add to map
					thirdPartyOnHoldDialogs.put(connectionId, thirdPartyOnHoldDialog);
					thirdPartyOffHold(connectionId);
					return;
				}
			}
		}
		
		if (thirdPartyOnHoldDialog == null) {
			unconferenceConnection(thirdPartyConnection);

			Map data = new HashMap();
			data.put("status", "connected");
			data.put("muted", thirdPartyConnection.isMuted());
			data.put("id", connectionId);
			data.put("name", SipUtil.getPhoneNumber(thirdPartyConnection.getDestination()));
			dispatchAgentMessage(data, AgentManager.TL_THIRDPARTY_STATUS_CHANGE);

			Map parameters = buildSessionDataObject();
			parameters.put("src", TLVXManager.callSessionManager.getHoldDialogURI());
			parameters.put("connectionid", thirdPartyConnection.getConnectionId());
			thirdPartyOnHoldDialog = new Dialog(parameters);
			thirdPartyOnHoldDialogs.put(connectionId, thirdPartyOnHoldDialog);
			LOG.debug("thirdPartyOnHold adding dialog for connectionId "+connectionId+" "+thirdPartyOnHoldDialog);
			startDialog(thirdPartyOnHoldDialog);
		} else {
			LOG.debug("ignoring third party on hold dialog");
		}
	}

	public void thirdPartyOffHold(String connectionId) {
		if (LOG.isDebugEnabled())
			LOG.debug("thirdPartyOffHold request "+ connectionId );

		Dialog thirdPartyOnHoldDialog = thirdPartyOnHoldDialogs.get(connectionId);
		LOG.debug("thirdPartyOffHold existing dialog: "+ thirdPartyOnHoldDialog);
		Connection thirdPartyConnection = thirdPartyConnections.get(connectionId);
		
		if (thirdPartyOnHoldDialog != null && !thirdPartyOnHoldDialog.getStarted()) {
			LOG.debug("Caller Off Hold Request delayed until Dialog Starting.");
			thirdPartyOnHoldDialog.setFinished(true);
		} else if (thirdPartyOnHoldDialog != null && thirdPartyOnHoldDialog.getDialogId() != null && currentDialogs.containsKey(thirdPartyOnHoldDialog.getDialogId())) {
			terminateDialog(thirdPartyOnHoldDialog);
		} else {
			thirdPartyOnHoldDialog = null;
			
			conferenceConnection(thirdPartyConnection, "full", null, null);
			Map data = new HashMap();
			data.put("status", "connected");
			data.put("muted", thirdPartyConnection.isMuted());
			data.put("id", connectionId);
			data.put("name", SipUtil.getPhoneNumber(thirdPartyConnection.getDestination()));
			dispatchAgentMessage(data, AgentManager.TL_THIRDPARTY_STATUS_CHANGE);
		}
	}

	public void thirdPartyDisconnect(String connectionId, boolean hangupCaller) {
		if (LOG.isDebugEnabled())
			LOG.debug("thirdPartyDisconnect request for " + connectionId + " " + this);
		
		Connection thirdPartyConnection = thirdPartyConnections.get(connectionId);

		if (null != thirdPartyConnection
				&& (thirdPartyConnection.getState() == Connection.ConnectionState.CONNECTED || thirdPartyConnection
						.getState() == Connection.ConnectionState.CONNECTING)) {
			hangupConnection(thirdPartyConnection);
		}
		
		dialingThirdParty = false;
	}

	private boolean isConnected(Connection connection) {
		return (null != connection && connection.getState() != Connection.ConnectionState.DISCONNECTED);
	}

	private boolean isDisconnecting(Connection connection) {
		return (connection.getState() == Connection.ConnectionState.DISCONNECTING);
	}

	private boolean isAnyConnected() {
		boolean connectionConnected = (isConnected(agentConnection)
				|| isConnected(customerConnection)
				|| isConnected(interpreterConnection)
				
				|| isConnected(transferConnection));
		
		for (Connection thirdPartyConnection : thirdPartyConnections.values()) {
			connectionConnected = connectionConnected
				|| isConnected(thirdPartyConnection);
		}

		boolean dialing = (dialingAgent || dialingCustomer
				|| dialingInterpreter || dialingThirdParty || dialingTransfer);

		return (connectionConnected || dialing);
	}

	private void hangupConnection(Connection connection) {
		if (true == destroyed || connection == null) {
			return;
		}
		synchronized(this) {
			Map parameters = buildSessionDataObject();
			parameters.put("connectionid", connection.getConnectionId());
	
			try {
				if (Connection.ConnectionState.DISCONNECTED != connection.getState()) {
					connection.setState(Connection.ConnectionState.DISCONNECTING);
					TLVXManager.ccxmlManager.disconnect(parameters);
				} else {
					LOG.warn("Connection "+connection.getConnectionId()+" is not connected state is: "+connection.getState());
				}
			} catch (Exception e) {
				LOG.warn("Exception caught while trying to disconnect call", e);
				handleServerError();
			}
		}
	}

	private void conferenceConnection(Connection connection, String duplex,
			String termDigits, String entertone) {
		if (false == connection.isConferenced()) {
			if (conference == null) {
				if (LOG.isDebugEnabled())
					LOG.debug("conferenceConnection connection = null!, duplex = "
							+ duplex
							+ ", termDigits = "
							+ termDigits
							+ " ignoring.");
				return;
			}
			if (LOG.isDebugEnabled())
				LOG.debug("conferenceConnection connection = "
						+ connection.getConnectionId() + ", duplex = " + duplex
						+ ", termDigits = " + termDigits);

			Map parameters = buildSessionDataObject();
			parameters.put("id1", connection.getConnectionId());
			parameters.put("id2", conference.getConferenceId());
			parameters.put("duplex", duplex);

			if (null != termDigits) {
				parameters.put("termdigits", termDigits);
			}
			
			if (null != entertone) {
				parameters.put("entertone", entertone);
			}

			try {
				TLVXManager.ccxmlManager.join(parameters);
			} catch (Exception e) {
				LOG.warn("Exception caught while trying to join", e);
				handleServerError();
			}

			connection.setConferenced(true);
		} else {
			if (LOG.isDebugEnabled())
				LOG.debug("Ignoring conference connection request, already conferenced");
		}
	}

	private void unconferenceConnection(Connection connection) {
		if (conference != null && connection != null && true == connection.isConferenced()) {
			Map parameters = buildSessionDataObject();
			parameters.put("id1", connection.getConnectionId());
			parameters.put("id2", conference.getConferenceId());

			try {
				TLVXManager.ccxmlManager.unjoin(parameters);
			} catch (Exception e) {
				LOG.warn("Exception caught while trying to join", e);
				handleServerError();
			}

			connection.setConferenced(false);
		} else {
			if (LOG.isDebugEnabled())
				LOG.debug("Ignoring unconference connection request, not in conference");
		}
	}

	public synchronized void connectInterpreter(String interpreterId, String language,
			String interpreterGender, boolean manual, Boolean interpreterVideo) {
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("connectInterpreter called with interpreterId="
					+ interpreterId + ",language=" + language
					+ ",interpreterGender=" + interpreterGender + ", manual="
					+ manual +", interpreterVideo = " + interpreterVideo + " for " + this);
		}
		
		if (interpreterVideo != null) videoRequested = interpreterVideo;
		else {
			if (previousInterpreterVideo != null)
			videoRequested = previousInterpreterVideo;
		}
		LOG.debug("videoRequested = "+videoRequested);
		manualInterpreterDial = manual;

		if (destroyed == true) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("connectInterpreter called but already destroyed for "
						+ this);
			}
			clearInterpreter();
			Interpreter interpreter = TLVXManager.interpreterDAO
					.findInterpreterByID(interpreterId);
			if (interpreter != null) {
				TLVXManager.interpreterManager.setInterpreterStatus(
						interpreter, InterpreterStatus.INTERPRETER_STATUS_IDLE, call.getId());
			}
			return;
		}

		if (language != null) {
			previousInterpreterLanguage = language;
		}

		if (interpreterGender != null) {
			previousInterpreterGender = interpreterGender;
		}

		if (null == interpreterId) {
			interpreterAttempts = 0;

			if ((isConnected(agentConnection) == false || isDisconnecting(agentConnection))
					&& null != agentId) {
				requeueCall = true;
			} else if (null != agentId) {
				Map data = new HashMap();
				data.put("status", "idle");
				if (null != interpreterConnection) {
					data.put("muted", interpreterConnection.isMuted());
				}
				dispatchAgentMessage(data,
						InterpreterManager.TL_INTERPRETER_STATUS_CHANGE);

				data = new HashMap();
				data.put("status", "unavailable");
				if (null != interpreterConnection) {
					data.put("muted", interpreterConnection.isMuted());
				}
				dispatchAgentMessage(data,
						InterpreterManager.TL_INTERPRETER_STATUS_CHANGE);
				completeCallNoInterpreters = true;
			} else {
				completeCallNoInterpreters = true;
				callerOnHold(false);
				synchronized(this) {
					call = TLVXManager.callDAO.findById(call.getId());
					call.setStatus(CallSessionManager.CALL_STATUS_QUEUED);
					call.setLastReason("Call Requires Additional Assistance");
					call = TLVXManager.callDAO.save(call);
				}
				saveCallEvent(CallEvent.CUSTOMER_QUEUED,
						"no interpreters available");
			}
		} else {
			// if existing interpreter, then unset busy for that one?
			synchronized (this) {
				// TODO this happens sometimes if interpreter is pulled for 2 calls at once, so we should
				//      probably not change the status
				if (interpreter != null) {
					if (interpreterConnection != null)
						hangupConnection(interpreterConnection);
					// put in check for interpreter.callid, if not this callid don't clear
					if (call.getId() == interpreter.getCallId()) {
						TLVXManager.interpreterManager.setInterpreterStatus(
								interpreter,
								InterpreterStatus.INTERPRETER_STATUS_IDLE, call.getId());
					}
				}
				interpreterConnection = null;

				call = TLVXManager.callDAO.findById(call.getId());
				if (call.getStatus() == CallSessionManager.CALL_STATUS_FINISHED)
					return;

				setInterpreter(TLVXManager.interpreterDAO
						.findInterpreterByID(interpreterId));
				previousInterpreter = interpreter;

				interpreterLanguage = language;

				Map parameters = buildSessionDataObject();
				parameters.put("callerid", "Telelanguage");
				
				if (interpreter.getOnWebSite() != null) {
					try {
						TLVXManager.interpreterManager.getINTClient(interpreter.getOnWebSite())
							.callIncoming(getInterpreterCallInfo());
					} catch (Exception e) {
						LOG.error("Unable to send message to interpreter server, log out interpreter",e);
						interpreter.setOnWebSite(null);
						interpreter.setWebPhone(false);
						interpreter.setVideo(false);
						TLVXManager.getSession().saveOrUpdate(interpreter);
					}
				}
				
				if (interpreter.getWebPhone()) {
					//send to web phone instead of PSTN
					if ("https://tlvx-interpreter.interpret.com/api/".equals(interpreter.getOnWebSite())) {
						parameters.put("dest", "sip:3" + interpreter.getAccessCode() + "@" + TLVXManager.callSessionManager.getProxyAddress());
					} else if ("https://interpret.interpret.com/api/".equals(interpreter.getOnWebSite())) {
						parameters.put("dest", "sip:" + interpreter.getAccessCode() + "@" + TLVXManager.callSessionManager.getProxyAddress());
//					} else if ("https://interpret1.telelanguage.com/api/".equals(interpreter.getOnWebSite())) {
//						parameters.put("dest", "sip:" + interpreter.getAccessCode() + "@" + TLVXManager.callSessionManager.getProxyAddress());
					} else if ("https://interpret3.telelanguage.com/api/".equals(interpreter.getOnWebSite())) {
						parameters.put("dest", "sip:3" + interpreter.getAccessCode() + "@" + TLVXManager.callSessionManager.getProxyAddress());
					} else if ("https://interpret4.telelanguage.com/api/".equals(interpreter.getOnWebSite())) {
						parameters.put("dest", "sip:4" + interpreter.getAccessCode() + "@" + TLVXManager.callSessionManager.getProxyAddress());
					} else if ("https://interpret5.telelanguage.com/api/".equals(interpreter.getOnWebSite())) {
						parameters.put("dest", "sip:5" + interpreter.getAccessCode() + "@" + TLVXManager.callSessionManager.getProxyAddress());
					} else if ("https://interpret6.telelanguage.com/api/".equals(interpreter.getOnWebSite())) {
						parameters.put("dest", "sip:6" + interpreter.getAccessCode() + "@" + TLVXManager.callSessionManager.getProxyAddress());
					} else if ("https://interpret7.telelanguage.com/api/".equals(interpreter.getOnWebSite())) {
						parameters.put("dest", "sip:7" + interpreter.getAccessCode() + "@" + TLVXManager.callSessionManager.getProxyAddress());
					} else if ("https://interpret8.telelanguage.com/api/".equals(interpreter.getOnWebSite())) {
						parameters.put("dest", "sip:8" + interpreter.getAccessCode() + "@" + TLVXManager.callSessionManager.getProxyAddress());
					} else if ("https://interpret9.telelanguage.com/api/".equals(interpreter.getOnWebSite())) {
						parameters.put("dest", "sip:9" + interpreter.getAccessCode() + "@" + TLVXManager.callSessionManager.getProxyAddress());
					} else {
					//parameters.put("dest", "sip:" + interpreter.getAccessCode() + "@" + TLVXManager.getProperties().getProperty("webPhoneSipAddressEnding"));
						parameters.put("dest", interpreter.getWebPhoneSipAddress());
					}
				} else {
					String number = interpreter.getAreaCode() + interpreter.getPhoneNumber();
					if (TLVXManager.callSessionManager.getPstnOutboundAddress() != null) {
						parameters.put("dest", "sip:" + TLVXManager.callSessionManager.getPstnOutboundPrefix() + number + "@" + TLVXManager.callSessionManager.getPstnOutboundAddress());
					} else {
						parameters.put("dest", "sip:" + number + "@" + TLVXManager.callSessionManager.getProxyAddress());
					}
				}
				saveCallEvent(CallEvent.INTERPRETER_CONNECTING, interpreterId, (String)parameters.get("dest"));

				try {
					dialingInterpreter = true;
					if (LOG.isDebugEnabled()) {
						LOG.debug(" 1 interpreterValidated = false callId = "
								+ call.getId() + " this =" + this);
					}
					interpreterValidated = false;
					parameters.put("timeout", TLVXManager.callSessionManager
							.getInterpreterTimeout());
					// parameters.put("connectionid", "interpreter");
					parameters.put("cpa", "true");
					TLVXManager.ccxmlManager.createCall(parameters);
				} catch (Exception e) {
					LOG.warn("Exception caught trying to create call for " + this, e);
					handleServerError();
				}

				interpreterHistory.add(interpreter);
			}
		}
	}
	
	public void onCustomerClickedHangup() {
		saveCallEvent(CallEvent.CUSTOMER_CLICKED_HANGUP, null, "customer clicked disconnect on GUI");
		if (interpreter != null && interpreter.getOnWebSite() != null) {
			try {
				TLVXManager.interpreterManager.getINTClient(interpreter.getOnWebSite())
					.disconnect(getInterpreterCallInfo());
				LOG.info("disconnect sent");
			} catch (Exception e) {
				LOG.error("error sending interpreter session, logging them out", e);
				interpreter.setOnWebSite(null);
				interpreter.setWebPhone(false);
				interpreter.setVideo(false);
				TLVXManager.getSession().saveOrUpdate(interpreter);
			}
		} else {
			LOG.info("disconnect NOT sent");
		}
		processCustomerDisconnect(false, "customer clicked disconnect on GUI");
	}

	public synchronized void disconnectInterpreter(String reason) {
		if (LOG.isDebugEnabled())
			LOG.debug("disconnectInterpreter");
		if (interpreter != null) {
			if (interpreterConnection != null) {
				interpreterConnection.setState(Connection.ConnectionState.DISCONNECTING);
				if (LOG.isDebugEnabled())
					LOG.debug("di INTERPRETER_DISCONNECT from disconnectInterpreter()");
				TLVXManager.interpreterManager.setInterpreterStatus(interpreter,
						InterpreterStatus.INTERPRETER_STATUS_IDLE, call.getId());
				//interpreter.setOnCall(false);
				if (interpreter != null) {
					saveCallEvent(CallEvent.INTERPRETER_DISCONNECT, interpreter.getInterpreterId(), reason);
				}
				hangupConnection(interpreterConnection);
				Map data = new HashMap();
				data.put("status", "idle");
				dispatchAgentMessage(data, InterpreterManager.TL_INTERPRETER_STATUS_CHANGE);
			} else {
				throw new RuntimeException("Outdial hasn't started yet, try again in a second.");
			}
		}
		manualInterpreterDial = true;  // keep from autodialing more interpreters when agent cancels the autodial
		dialingInterpreter = false;
	}

	public void connectCustomer(InterpreterRequest request) {
		if (LOG.isDebugEnabled())
			LOG.debug("connectCustomer request, request = " + request
					+ ", customer = " + customer);

		videoInterpreterRequest = request;

		Map parameters = buildSessionDataObject();
		parameters.put("callerid", "telelanguage");

		if (request.videoCustomerInfo.webPhoneSipAddress.startsWith("sip:vid")) {
			String address = request.videoCustomerInfo.webPhoneSipAddress.substring(0, request.videoCustomerInfo.webPhoneSipAddress.indexOf("@")+1);
			parameters.put("dest", address+TLVXManager.callSessionManager.getProxyAddress());
		} else if (request.videoCustomerInfo.webPhoneSipAddress.startsWith("sip:")) {
			parameters.put("dest", request.videoCustomerInfo.webPhoneSipAddress);
		} else {
			parameters.put("dest", "sip:" + request.videoCustomerInfo.webPhoneSipAddress + "@" + TLVXManager.callSessionManager.getProxyAddress());
		}

		try {
			dialingCustomer = true;
			parameters.put("timeout", TLVXManager.callSessionManager.getThirdPartyTimeout());
			//parameters.put("connectionid", "customer");
			TLVXManager.ccxmlManager.createCall(parameters);
		} catch (Exception e) {
			LOG.warn("Exception caught trying to create call for " + this, e);
			handleServerError();
		}
	}

	public void connectThirdParty(String phonenumber) {
		if (LOG.isDebugEnabled())
			LOG.debug("connectThirdParty request, phonenumber = " + phonenumber);
		
		if (dialingThirdParty) {
			throw new RuntimeException("Third party dialing currently in progress.");
		}
		
		if (thirdPartyConnections.size()>=4) {
			throw new RuntimeException("Only four third parties allowed on a call.");
		}
		
		phonenumber = phonenumber.replaceAll(" ", "");
		phonenumber = phonenumber.replaceAll("-", "");
		phonenumber = phonenumber.replaceAll("\\(", "");
		phonenumber = phonenumber.replaceAll("\\)", "");
		phonenumber = phonenumber.replaceAll("\\+", "");
		phonenumber = phonenumber.replaceAll("\\.", "");
		phonenumber = phonenumber.replaceAll(",","");

		Map parameters = buildSessionDataObject();
		parameters.put("callerid", "telelanguage");
		if (TLVXManager.callSessionManager.getPstnOutboundAddress() != null) {
			parameters.put("dest", "sip:" + TLVXManager.callSessionManager.getPstnOutboundPrefix() + phonenumber + "@" + TLVXManager.callSessionManager.getPstnOutboundAddress());
		} else {
			parameters.put("dest", "sip:" + phonenumber + "@" + TLVXManager.callSessionManager.getProxyAddress());
		}

		LOG.debug(">>>> "+parameters);
		
		try {
			thirdPartyDestination = phonenumber;
			dialingThirdParty = true;
			
			if (interpreter != null && interpreter.getOnWebSite() != null) {
				try {
					TLVXManager.interpreterManager.getINTClient(interpreter.getOnWebSite())
						.callStatusUpdate(getInterpreterCallInfo());
				} catch (Exception e) {
					LOG.error("Unable to send message to interpreter server, log out interpreter",e);
					interpreter.setOnWebSite(null);
					interpreter.setWebPhone(false);
					interpreter.setVideo(false);
					TLVXManager.getSession().saveOrUpdate(interpreter);
				}
			}
			
			parameters.put("timeout", TLVXManager.callSessionManager.getThirdPartyTimeout());
			parameters.put("connectionid", "thirdparty");
			TLVXManager.ccxmlManager.createCall(parameters);
			
		} catch (Exception e) {
			LOG.warn("Exception caught trying to create call", e);
			handleServerError();
		}
	}

	synchronized private void startDialog(Dialog dialog) {
		if (LOG.isDebugEnabled())
			LOG.debug("startDialog request");

		try {
			Thread.sleep(250);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		if (null == processingDialog) {
			processingDialog = dialog;

			try {
				TLVXManager.ccxmlManager.dialogStart(dialog.getParameters());
			} catch (Exception e) {
				LOG.warn("Exception caught while trying to start dialog", e);
				handleServerError();
			}
		} else {
			if (LOG.isDebugEnabled())
				LOG.debug("currently processing another dialog, queuing request");
			dialogQueue.add(dialog);
		}
	}

	synchronized private void processNextDialog() {
		if (LOG.isDebugEnabled())
			LOG.debug("processNextDialog request");

		processingDialog = dialogQueue.poll();

		if (null == processingDialog) {
			if (LOG.isDebugEnabled())
				LOG.debug("empty dialog queue, nothing to process");
		} else {
			try {
				TLVXManager.ccxmlManager.dialogStart(processingDialog.getParameters());
			} catch (Exception e) {
				LOG.warn("Exception caught while trying to start dialog", e);
				handleServerError();
			}
		}
	}

	private void terminateDialog(Dialog dialog) {
		if (LOG.isDebugEnabled())
			LOG.debug("terminateDialog request");

		if (null != dialog) {
			Map parameters = buildSessionDataObject();
			parameters.put("dialogid", dialog.getDialogId());

			try {
				TLVXManager.ccxmlManager.dialogTerminate(parameters);
			} catch (Exception e) {
				LOG.warn("Exception caught while trying to start dialog", e);
				handleServerError();
			}
		}
	}

	private void saveCallEvent(int eventType, String payload) {
		saveCallEvent(eventType, payload, null);

		TLVXManager.agentManager.sendQueueStatusUpdates();
	}

	private void saveCallEvent(int eventType, String payload, String payload2) {
		call = TLVXManager.callDAO.findById(call.getId());
		CallEvent callEvent = new CallEvent();
		callEvent.setCall(call);
		callEvent.setDate(new Date());
		callEvent.setPayload(payload);
		callEvent.setPayload2(payload2);
		callEvent.setEventType(eventType);
		TLVXManager.callEventDAO.save(callEvent);
	}

	private void handleServerError() {
		if (LOG.isDebugEnabled())
			LOG.debug("handleServerError for call " + call);

		saveCallEvent(CallEvent.CCXML_SERVER_ERROR, null);
		cleanUpSession();
	}

	public void onSessionDestroyed(Map request) {
		if (LOG.isDebugEnabled())
			LOG.debug("onSessionDestroyed, destroyed = " + destroyed);

		cleanUpSession();
	}

	private void cleanUpSession() {
		if (LOG.isDebugEnabled())
			LOG.debug("cleanUpSession, destroyed = " + destroyed);

		// if (null != savedCall && null != customerConnection)
		// {
		// // delete it once we retrieve info
		// savedCallDAO.delete(savedCall);
		// savedCall = null;
		// }

		if (false == destroyed) {
			synchronized (this) {
				try {
					if (null != optionData) {
						// save the customer options
						LOG.info("saving options data " + optionData.toString());
						TLVXManager.customerOptionDataDAO.saveAll(optionData);
					}
				} catch (Exception e) {
					LOG.warn("Error occured trying to save options data = "
							+ optionData.toString(), e);
				}
	
				if (agentId != null) {
					processAgentDisconnect(false, null);
				}
	
				if (interpreter != null) {
					processInterpreterDisconnect(false, "clean up session");
				}

				Map parameters = buildSessionDataObject();
				if (conference != null) {
					parameters.put("conferenceid", conference.getConferenceId());
					try {
						TLVXManager.ccxmlManager.destroyConference(parameters);
						conference = null;
					} catch (Exception e) {
						LOG.warn("Exception caught trying to destroy conference", e);
					}
				}

				try {
					Map data = buildSessionDataObject();
					TLVXManager.ccxmlManager.destroySession(data);
				} catch (Exception e) {
					LOG.warn("Exception caught trying to destroy session", e);
				}
	
				TLVXManager.callSessionManager.endCallSession(this, call);
	
				destroyed = true;
				clearInterpreter();

				try {
					call = TLVXManager.callDAO.findById(call.getId());
					
					if (bluestream) {
						saveCallEvent(CallEvent.INTERPRETER_DISCONNECT, "bluestream:"+bsInterpreterId, bsCallId);
					}

					CallDetailRecord cdr = TLVXManager.callDetailRecordDAO
							.getCDRByCallId(call);
					if (cdr == null)
						cdr = new CallDetailRecord();
					cdr.setCall(call);
					cdr.setStartTime(call.getStartDate());
					cdr.setEndTime(call.getEndDate());
					String tempAni = customerConnection.getOrigination();
					if (videoInterpreterRequest != null && videoInterpreterRequest.videoCustomerInfo != null && videoInterpreterRequest.videoCustomerInfo.email != null) {
						tempAni = videoInterpreterRequest.videoCustomerInfo.email;
					}
					cdr.setAni(tempAni);
					cdr.setDnis(customerConnection.getDestination());
					cdr.setDeptCode(call.getDepartmentCode());
					if (cdr.getDeptCode() == null && customerDNIS != null
							&& customerDNIS.getDepartmentId() != null) {
						try {
							CustomerDepartment department = TLVXManager.customerDepartmentDAO
									.findByDepartmentId(customerDNIS
											.getDepartmentId());
							if (department != null) {
								cdr.setDeptCode(department.getCode());
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					cdr.setLanguage(call.getLanguage());
					cdr.setSubscriptionCode(call.getSubscriptionCode());
					if (null != customer) {
						cdr.setAccessCode(customer.getCode());
					}
					if (null != previousInterpreter) {
						cdr.setInterpreterId(previousInterpreter
								.getInterpreterId());
					}
					cdr.setIvr(TLVXManager.callEventDAO.isIVR(call));
					cdr.setAgent_connects(TLVXManager.callEventDAO
							.getAgentConnects(call));
					cdr.setRequeues(TLVXManager.callEventDAO.getRequeues(call));
					cdr.setInterpreter_attempts(TLVXManager.callEventDAO
							.getInterpreterAttempts(call));
					cdr.setInterpreter_rejects(TLVXManager.callEventDAO
							.getInterpreterRejects(call));
					cdr.setCall_queue_time(TLVXManager.callEventDAO
							.getCallQueueTime(call));

					cdr.setInterpreter_connect_time(TLVXManager.callEventDAO
							.getInterpreterConnectTime(call, true));
					if (cdr.getInterpreter_connect_time() == 0) {
						float time = TLVXManager.callEventDAO
								.getInterpreterConnectTime(call, false);
						if (time > 0) {
							cdr.setInterpreter_connect_time(time);
						}
					}

					cdr.setInterpreter_dial_time(TLVXManager.callEventDAO
							.getInterpreterDialTime(call));
					cdr.setInterpreter_talk_time(TLVXManager.callEventDAO
							.getInterpreterTalkTime(call));
					cdr.setInterpreter_start_time(TLVXManager.callEventDAO
							.getInterpreterStartTime(call));
					cdr.setInterpreter_end_time(TLVXManager.callEventDAO
							.getInterpreterEndTime(call));
					cdr.setThirdparty_talk_time(TLVXManager.callEventDAO
							.getThirdPartyTalkTime(call));
					cdr.setIvr_time(TLVXManager.callEventDAO.getIVRTime(call));
					List<QuestionItem> questions = TLVXManager.companyManager
							.getAdditionalQuestions("" + call.getId(),
									call.getCustomer());
					if (null != questions && false == questions.isEmpty()) {
						if (questions.size() > 0) {
							cdr.setOption_1(questions.get(0).getValue());
						}
						if (questions.size() > 1) {
							cdr.setOption_2(questions.get(1).getValue());
						}
						if (questions.size() > 2) {
							cdr.setOption_3(questions.get(2).getValue());
						}
						if (questions.size() > 3) {
							cdr.setOption_4(questions.get(3).getValue());
						}
						if (questions.size() > 4) {
							cdr.setOption_5(questions.get(4).getValue());
						}
						if (questions.size() > 5) {
							cdr.setOption_6(questions.get(5).getValue());
						}
					}
					cdr.setSipCallId(sipCallId);
					if (customerConnection != null) {
						cdr.setVideo(customerConnection.isVideo());
					}
					cdr = TLVXManager.callDetailRecordDAO.save(cdr);
					if (LOG.isDebugEnabled())
						LOG.debug("Saved call detail record " + cdr);
				} catch (Throwable t) {
					LOG.warn("Error saving call detail record for " + call, t);
				}
			}

			callRecordingStopRecording();
			callRecordingUpdateInfo();
		}
	}

	private boolean isPriorityCall() {
		return (customer != null) ? customer.getCustomerDNIS().isPriority()
				: false;
	}

	private String getSubscriptionCode() {
		return (customer != null) ? customer.getSubscriptionCode() : null;
	}

	private void callRecordingStartRecording() {
		LOG.debug("Start Customer Recording " + call.getCallSessionId()+" " +call.getId());
	}

	private void confRecordingStart() {
		LOG.debug("Start Conference Recording " + call.getCallSessionId()+" " +call.getId());
	}

	private void callRecordingUpdateInfo() {
	}

	private void callRecordingStopRecording() {
	}

	public void setPreviousInterpreterGender(String interpreterGender) {
		previousInterpreterGender = interpreterGender;
	}

	public boolean verifyCustomerOnCall() {
		if (customerConnection != null
				&& customerConnection.getState() != Connection.ConnectionState.DISCONNECTED)
			return true;
		if (LOG.isDebugEnabled())
			LOG.debug("CallId: " + call.getId()
					+ " verifyCustomerOnCall customer disconnected ");
		destroyed = true;
		call = TLVXManager.callDAO.findById(call.getId());
		call.setStatus(CallSessionManager.CALL_STATUS_FINISHED);
		if (call.getEndDate() == null)
			call.setEndDate(new Date());
		call = TLVXManager.callDAO.save(call);
		return false;
	}

	private void clearInterpreter() {
		if (LOG.isDebugEnabled() && interpreter != null)
			LOG.debug("CallId: " + call.getId() + " clearInterpreter = "
					+ interpreter.getAccessCode());
		synchronized(this) {
			if (interpreter != null) {
				if (interpreterConnection != null)
					hangupConnection(interpreterConnection);
				TLVXManager.interpreterManager.setInterpreterStatus(interpreter,
						InterpreterStatus.INTERPRETER_STATUS_IDLE, call.getId());
			}
			interpreter = null;
		}
	}

	private void setInterpreter(Interpreter interpreter) {
		if (LOG.isDebugEnabled() && interpreter != null)
			LOG.debug("CallId: " + call.getId() + " setInterpreter = "
					+ interpreter.getAccessCode());
		synchronized(this) {
			if (interpreter != null)
				clearInterpreter();
			TLVXManager.interpreterManager.setInterpreterStatus(interpreter,
					InterpreterStatus.INTERPRETER_STATUS_BUSY);
			this.interpreter = interpreter;
		}
	}

	public boolean isDestroyedAndAllCallsDisconnected() {
		boolean allDisconnected = destroyed 
				&& (customerConnection == null || customerConnection.getState() == ConnectionState.DISCONNECTED)
				&& (interpreterConnection == null || interpreterConnection.getState() == ConnectionState.DISCONNECTED)
				&& (agentConnection == null || agentConnection.getState() == ConnectionState.DISCONNECTED);
		for(Connection thirdPartyConnection : thirdPartyConnections.values()) {
			allDisconnected = allDisconnected
				&& (thirdPartyConnection == null || thirdPartyConnection.getState() == ConnectionState.DISCONNECTED);	
		}
		return allDisconnected;
	}

	public synchronized void interpreterWebAgentOutdialRequest(InterpreterInfo interpreter2) {
		callerOnHold(false);
		interpreterOnHold(true);
		for (String thirdPartyConnectionId : thirdPartyConnections.keySet()) {
			thirdPartyOnHold(thirdPartyConnectionId);
		}
		call = TLVXManager.callDAO.findById(call.getId());
		if (call.getStatus() != CallSessionManager.CALL_STATUS_FINISHED
				&& customerConnection != null
				&& customerConnection.getState() == Connection.ConnectionState.CONNECTED
				&& interpreterConnection.getState() == Connection.ConnectionState.CONNECTED) {
			call.setStatus(CallSessionManager.CALL_STATUS_QUEUED);
			call.setLastReason("Interpreter sent call back to agent from web page.");
			call = TLVXManager.callDAO.save(call);
			saveCallEvent(CallEvent.CUSTOMER_QUEUED, "Interpreter Web Agent Request");
		}
	}

	public void setPreviousVideo(Boolean video) {
		if (LOG.isDebugEnabled())
			LOG.debug("setPreviousVideo called with video = " + video);
		if (video != null)
			previousInterpreterVideo = video;
	}

	public void interpreterWebPlayCustomerVideoRequest(InterpreterInfo interpreter2) {
		if (LOG.isDebugEnabled()) LOG.debug("interpreterWebPlayCustomerVideoRequest called with video = " + interpreter2);
		if (videoInterpreterRequest != null && videoInterpreterRequest.videoCustomerInfo.videoServer != null) {
			try {
				TLVXManager.customerUserManager.getVideoAPIClient(videoInterpreterRequest.videoCustomerInfo.videoServer).playVideoRequest(getVideoCallInfo());
			} catch (Exception e) {
				LOG.error("Unable to send message to video call server, ending call",e);
				hangupConnection(customerConnection);
			}
		}
	}

	public void interpreterWebPauseCustomerVideoRequest(InterpreterInfo interpreter2) {
		if (LOG.isDebugEnabled()) LOG.debug("interpreterWebPauseCustomerVideoRequest called with video = " + interpreter2);
		if (videoInterpreterRequest != null && videoInterpreterRequest.videoCustomerInfo.videoServer != null) {
			try {
				TLVXManager.customerUserManager.getVideoAPIClient(videoInterpreterRequest.videoCustomerInfo.videoServer).pauseVideoRequest(getVideoCallInfo());
			} catch (Exception e) {
				LOG.error("Unable to send message to video call server, ending call",e);
				hangupConnection(customerConnection);
			}
		}
	}
	
	public void interpreterVideoSessionStarted(InterpreterInfo interpreter2) {
		if (LOG.isDebugEnabled()) LOG.debug("interpreterVideoSessionStarted called with video = " + interpreter2);
		if (videoInterpreterRequest != null && videoInterpreterRequest.videoCustomerInfo.videoServer != null) {
			interpreterConnection.setVideo(true);
			saveCallEvent(CallEvent.INTERPRETER_VIDEO_SESSION_STARTED, null);
		}
	}

	public void customerVideoSessionStarted() {
		if (LOG.isDebugEnabled()) LOG.debug("customerVideoSessionStarted");
		customerConnection.setVideo(true);
		saveCallEvent(CallEvent.CUSTOMER_VIDEO_SESSION_STARTED, null);
	}

	public void bluestreamInitiateCall(Call call2, BlueStreamRequest request) {
		LOG.debug("bluestreamInitiateCall");
		VideoCallInfo vci = getVideoCallInfo();
		vci.customerId = request.customerId;
		vci.customerName = request.customerName;
		vci.event = request.event;
		
		bluestream = true;

		if (videoInterpreterRequest != null && videoInterpreterRequest.videoCustomerInfo.videoServer != null) {
			try {
				TLVXManager.customerUserManager.getVideoAPIClient(videoInterpreterRequest.videoCustomerInfo.videoServer).bluestreamInitiateCall(vci);
			} catch (Exception e) {
				LOG.error("Unable to send message to video call server, ending call",e);
				hangupConnection(customerConnection);
			}
		}
		
		hangupConnection(agentConnection);
	}

	public void blueStreamConnected(String bsCallId, String bsInterpreterId, String bsInterpreterName) {
		LOG.debug("blueStreamConnected bsCallId: "+bsCallId+" bsInterpreterId: "+bsInterpreterId+" bsInterpreterName: "+bsInterpreterName); 
		this.bsCallId = bsCallId;
		this.bsInterpreterId = bsInterpreterId;
		callerOffHold();
		saveCallEvent(CallEvent.INTERPRETER_CONNECT, "bluestream:"+bsInterpreterId, bsCallId);
		saveCallEvent(CallEvent.INTERPRETER_ON_CALL, "bluestream:"+bsInterpreterId, bsInterpreterName);
		saveCallEvent(CallEvent.CUSTOMER_INTERPRETER_CONNECT, null);
	}
}