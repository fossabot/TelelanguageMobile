package com.telelanguage.api;

public interface TLVXAPIService {
	InterpreterInfo findInterpreterByEmail(String email);
	void saveInterpreter(InterpreterInfo interpreter);
	void markInterpreterOffCall(InterpreterInfo interpreter);
	Boolean checkCommunicationsOk(String thisServerUrl);
	void hangupCall(InterpreterInfo interpreter);
	void acceptCall(InterpreterInfo interpreter);
	void rejectCall(InterpreterInfo interpreter);
	void requestAgent(InterpreterInfo interpreter);
	void acceptVideo(InterpreterInfo interpreter);
	void acceptVideoOnly(InterpreterInfo interpreter);
	void dontAcceptVideo(InterpreterInfo interpreter);
	void playCustomerVideo(InterpreterInfo interpreter);
	void pauseCustomerVideo(InterpreterInfo interpreter);
	void videoSessionStarted(InterpreterInfo interpreter);
	void dialThirdParty(InterpreterInfo interpreter);
	void hangupThirdParty(InterpreterInfo interpreter);
}
