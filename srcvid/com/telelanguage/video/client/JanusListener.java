package com.telelanguage.video.client;

public interface JanusListener {
	void initSuccess();
	void error(String reason);
	void destroyed();
}
