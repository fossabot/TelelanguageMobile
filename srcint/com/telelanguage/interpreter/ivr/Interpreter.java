package com.telelanguage.interpreter.ivr;

import java.util.Properties;

import com.icoa.voice.api.Call;
import com.icoa.voice.api.VoiceApplication;
import com.telelanguage.interpreter.service.TLVXManager;

public class Interpreter extends VoiceApplication {

	@Override
	public void incomingCall(Call call) {
		System.out.println("incomingCall "+call);
		call.answer();
	}

	@Override
	public void outgoingCall(Call call) {
		System.out.println("outgoingCall "+call);
	}

	@Override
	public void connectedCall(Call call) {
		System.out.println("connectedCall "+call);
		call.customDialog("vxml/getAccessCode.jsp");
	}

	@Override
	public void disconnectedCall(Call call) {
		System.out.println("disconnectedCall "+call);
	}

	@Override
	public void exitedDialog(Call call) {
		System.out.println("exitedDialog "+call);
	}

	@Override
	public void conferenceUnJoined(Call call) {
		System.out.println("conferenceUnJoined "+call);
	}

	@Override
	public void sessionDestroyed(Call call) {
		System.out.println("sessionDestroyed "+call);
	}

	@Override
	public void conferenceDestroyed(Call call) {
		System.out.println("conferenceDestroyed "+call);
	}

	@Override
	public void conferenceUnjoined(Call call) {
		System.out.println("conferenceUnjoined "+call);
	}

	@Override
	public Properties getAppProps() {
		return TLVXManager.getProperties();
	}

	@Override
	public void createdConference(Call call) {
		System.out.println("createdConference "+call);
	}

	@Override
	public void dialogStarted(Call call) {
		System.out.println("dialogStarted "+call);
	}

	@Override
	public void dialogExit(Call call) {
		System.out.println("dialogExit "+call);
		call.hangup();
	}

	@Override
	public void conferenceJoined(Call call) {
		System.out.println("conferenceJoined "+call);
	}

	@Override
	public void conferenceErrorJoin(Call call) {
		System.out.println("conferenceErrorJoin "+call);
	}

	@Override
	public void callConnectionFailed(Call call) {
		System.out.println("callConnectionFailed "+call);
	}

	@Override
	public void cleanupSession() {
		System.out.println("cleanupSession ");
	}

	@Override
	public void callConnectionErrorWrongstate(Call call) {
		System.out.println("callConnectionErrorWrongstate "+call);
	}

	@Override
	public void errorSemantic(Call call) {
		System.out.println("errorSemantic "+call);
	}

	@Override
	public void errorDialogNotStarted(Call call) {
		System.out.println("errorDialogNotStarted "+call);
	}

}
