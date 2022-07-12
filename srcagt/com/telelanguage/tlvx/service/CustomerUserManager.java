package com.telelanguage.tlvx.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.telelanguage.videoapi.VideoAPIClient;

public class CustomerUserManager {
	private static final Logger LOG = Logger.getLogger(CustomerUserManager.class);
	private static Map<String, VideoAPIClient> videoAPIClients = new HashMap<String, VideoAPIClient>();
	
	synchronized public VideoAPIClient getVideoAPIClient(String onWebSite) {
		if (videoAPIClients.containsKey(onWebSite)) return videoAPIClients.get(onWebSite);
		VideoAPIClient newClient = new VideoAPIClient(onWebSite);
		LOG.info("VideoAPIClient created for "+onWebSite);
		videoAPIClients.put(onWebSite, newClient);
		return newClient;
	}
}
