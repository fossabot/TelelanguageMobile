package com.telelanguage.interpreter.client;

public interface JanusListener {
	void initSuccess();
	void error(String reason);
	void destroyed();
}
