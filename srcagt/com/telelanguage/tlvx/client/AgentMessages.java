package com.telelanguage.tlvx.client;

public enum AgentMessages {
	TlAgentStatusResponse,
	TlAgentStatus,
	TlCallsessionAgentSet,
	TlCallInit, 
	TlCallStart,
	TlCallStop,
	
	TlCallInterpreterHistory,
	TlCallIncomingNoagents,
	TlCallPriorityIncoming,
	
	TlCustomerStatusChange, 
	TlInterpreterStatusChange,
	TlThirdpartyStatusChange,
	
	SaveCallInformation,
	
	Logout, 
	ping, ErrorMessage
}
