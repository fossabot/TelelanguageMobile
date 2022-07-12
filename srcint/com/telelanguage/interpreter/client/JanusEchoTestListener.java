package com.telelanguage.interpreter.client;

public interface JanusEchoTestListener {
	void echoPluginSuccess();
	void echoPluginError(String error);
	void echoPluginConsentDialog(Boolean on);
	void echoPluginDone();
}
