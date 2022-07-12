package com.telelanguage.video.client;

public interface JanusEchoTestListener {
	void echoPluginSuccess();
	void echoPluginError(String error);
	void echoPluginConsentDialog(Boolean on);
	void echoPluginDone();
}
