package com.telelanguage.tlvx.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.telelanguage.tlvx.model.CallInformation;
import com.telelanguage.tlvx.model.Department;
import com.telelanguage.tlvx.model.InterpreterLine;
import com.telelanguage.tlvx.model.Language;
import com.telelanguage.tlvx.model.QuestionItem;

@RemoteServiceRelativePath("agent")
public interface AgentService extends RemoteService {
	void updateStatus(String sessionId);
	Integer logon(String sessionId, String name, String password);
	void logout(String sessionId);
	void startTakingCalls(String sessionId);
	void stopTakingCalls(String sessionId);
	void callSessionCustomerConnect(String sessionId, String callSessionId, String dest);
	void callerOnHold(String sessionId, String callId, Boolean completeCall);
	void interpreterOnHold(String sessionId, String callId);
	void thirdPartyOnHold(String sessionId, String callId, String connectionId);
	void callerOffHold(String sessionId, String callId);
	void interpreterOffHold(String sessionId, String callId);
	void thirdPartyOffHold(String sessionId, String callId, String connectionId);
	Boolean completeCall(String sessionId, CallInformation callInformation, Boolean hangupCaller);
	void requeueRequest(String sessionId, CallInformation callInformation);
	void personalHoldRequest(String sessionId, CallInformation callInformation);
	void saveCallInformation(String sessionId, CallInformation callInformation);
	List<InterpreterLine> getInterpreters(String sessionId, String callSessionId,
			String Language, String interpreterGender, Boolean video);
	List<InterpreterLine> getInterpreterHistory(String sessionId, String callId, String Language);
	List<Language> getLanguages();
	List<QuestionItem> getAdditionalQuestions(String sessionId, String callId, String customerId);
	QuestionItem serializeQuestionItem(String sessionId, QuestionItem item);
	InterpreterLine serializeInterpreterLine(String sessionId, InterpreterLine item);
	void transfer(String sessionId, String callId, String dest);
	void updateNumber(String sessionId, String phoneNumber);
	void dialThirdParty(String sessionId, String callId, String dest) throws RuntimeException;
	void callInterpreter(String sessionId, String callId, String language, String interpreterId);
	Boolean callLanguage(String sessionId, String callId, String language, String interpreterGender, Boolean interpreterVideo);
	void hangupThirdParty(String sessionId, String callId, String connectionId);
	void hangupInterpreter(String sessionId, String callId) throws RuntimeException;
	String getRelease();
	List<String> getMessages(String sessionId);
	Department serializeDepartment(Department d);
	CallInformation getCallInformationByCallId(String sessionId, String callId);
	CallInformation getCallInformationByCallIdAccessCode(String sessionId, String callId, String accessCode);
}
