package com.telelanguage.videoapi;

public interface TLVXCustomerAPIService {
	CustomerLoginResponse registerCustomer(CustomerRegistrationRequest request);
	CustomerLoginResponse loginCustomer(CustomerLoginRequest request);
	void requestInterpreter(InterpreterRequest request);
	CustomerLogoutResponse logoutCustomer(CustomerLogoutRequest request);
	CustomerHangupResponse hangupCustomer(CustomerHangupRequest request);
	void videoSessionStarted(CustomerVideoEstablished request);
	void requestCredentials(CredentialRequest request);
	void scheduleInterpreter(ScheduleInterpreterRequest request);
	void bluestreamConnected(BlueStreamConnected request);
	void bluestreamAgentRequest(BlueStreamConnected request);
	void bluestreamDisconnected(BlueStreamConnected request);
}
