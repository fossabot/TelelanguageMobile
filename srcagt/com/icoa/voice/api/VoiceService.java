package com.icoa.voice.api;

import java.util.Map;

public interface VoiceService {
	public Call createSipCall(Map<String, String> params);
}