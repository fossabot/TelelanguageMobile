package com.telelanguage.api;

public interface INTAPIService {
	void callIncoming(InterpreterCallInfo interpreterCallInfo);
	void askToAcceptCall(InterpreterCallInfo interpreterCallInfo);
	void interpreterOnHold(InterpreterCallInfo interpreterCallInfo);
	void interpreterOffHold(InterpreterCallInfo interpreterCallInfo);
	void disconnect(InterpreterCallInfo interpreterCallInfo);
	void agentDisconnected(InterpreterCallInfo interpreterCallInfo);
	void interpreterNotAcceptingCalls(InterpreterCallInfo interpreterCallInfo);
	void interpreterLogout(InterpreterCallInfo interpreterCallInfo);
	void callStatusUpdate(InterpreterCallInfo interpreterCallInfo);
}
