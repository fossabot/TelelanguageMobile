package com.telelanguage.tlvx.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.telelanguage.tlvx.model.CallInformation;
import com.telelanguage.tlvx.model.Department;
import com.telelanguage.tlvx.model.InterpreterLine;
import com.telelanguage.tlvx.model.Language;
import com.telelanguage.tlvx.model.QuestionItem;

public interface AgentServiceAsync {
	void logon(String sessionId, String input, String password, AsyncCallback<Integer> callback);
	void startTakingCalls(String sessionId, AsyncCallback<Void> callback);
	void stopTakingCalls(String sessionId, AsyncCallback<Void> callback);
	void logout(String sessionId, AsyncCallback<Void> callback);
	void callSessionCustomerConnect(String sessionId, String callSessionId, String dest, AsyncCallback<Void> callback);
	void callerOnHold(String sessionId, String callSessionId, Boolean completeCall, AsyncCallback<Void> callback);
	void callerOffHold(String sessionId, String callSessionId, AsyncCallback<Void> callback);
	void updateStatus(String sessionId, AsyncCallback<Void> callback);
	void completeCall(String sessionId, CallInformation callInformation, Boolean hangupCaller,
			AsyncCallback<Boolean> callback);
	void getLanguages(AsyncCallback<List<Language>> callback);
	void getInterpreters(String sessionId, String callSessionId,
			String Language, String interpreterGender, Boolean video,
			AsyncCallback<List<InterpreterLine>> callback);
	void requeueRequest(String sessionId, CallInformation callInformation, AsyncCallback<Void> callback);
	void personalHoldRequest(String sessionId, CallInformation callInformation, AsyncCallback<Void> callback);
	void getAdditionalQuestions(String sessionId, String callId, String customerId, AsyncCallback<List<QuestionItem>> callback);
	void serializeQuestionItem(String sessionId, QuestionItem item, AsyncCallback<QuestionItem> callback);
	void transfer(String sessionId, String callId, String dest, AsyncCallback<Void> callback);
	void updateNumber(String sessionId, String phoneNumber, AsyncCallback<Void> callback);
	void dialThirdParty(String sessionId, String callId, String dest, AsyncCallback<Void> callback);
	void getInterpreterHistory(String sessionId, String callId, String Language, AsyncCallback<List<InterpreterLine>> callback);
	void serializeInterpreterLine(String sessionId, InterpreterLine item, AsyncCallback<InterpreterLine> callback);
	void callInterpreter(String sessionId, String callId, String language, String interpreterId,
			AsyncCallback<Void> callback);
	void hangupThirdParty(String sessionId, String callId, String connectionId, AsyncCallback<Void> callback);
	void hangupInterpreter(String sessionId, String callId, AsyncCallback<Void> callback);
	void interpreterOnHold(String sessionId, String callId, AsyncCallback<Void> callback);
	void thirdPartyOnHold(String sessionId, String callId, String connectionId, AsyncCallback<Void> callback);
	void interpreterOffHold(String sessionId, String callId, AsyncCallback<Void> callback);
	void thirdPartyOffHold(String sessionId, String callId, String connectionId, AsyncCallback<Void> callback);
	void callLanguage(String sessionId, String callId, String language,
			String interpreterGender, Boolean interpreterVideo,
			AsyncCallback<Boolean> callback);
	void getRelease(AsyncCallback<String> callback);
	void getMessages(String sessionId, AsyncCallback<List<String>> callback);
	void serializeDepartment(Department d, AsyncCallback<Department> callback);
	void getCallInformationByCallId(String sessionId, String callId, AsyncCallback<CallInformation> callback);
	void getCallInformationByCallIdAccessCode(String sessionId, String callId, String accessCode, AsyncCallback<CallInformation> callback);
	void saveCallInformation(String sessionId, CallInformation callInformation, AsyncCallback<Void> callback);
}
