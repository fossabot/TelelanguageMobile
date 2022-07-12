package com.telelanguage.interpreter.client;

public interface JanusSipListener {
	void sipPluginSuccess();
	void sipPluginError(String error);
	void sipPluginConsentDialog(Boolean on);
	void sipIncomingCall(String username, Boolean audio, Boolean video);
	void sipRegistered(String username);
	void sipHangup(String username, String reason);
	void sipAccepted(String username, String reason);
	void sipDetached();
	void sipRegistrationFailed(String reason);
}
