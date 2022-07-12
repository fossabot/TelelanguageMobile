package com.telelanguage.videoapi;

import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.telelanguage.tlvx.service.TLVXManager;

@Path("/video/v1")
public class VideoAPIServiceServlet implements VideoAPIService {
	
	Properties properties = TLVXManager.getProperties();
	static VideoAPIService serverInstance;
	
	static public void setServerInstance(VideoAPIService serverInstance) {
		VideoAPIServiceServlet.serverInstance = serverInstance;
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("videoCallOnHold")
	public void videoCallOnHold(VideoCallInfo interpreterCallInfo) {
		serverInstance.videoCallOnHold(interpreterCallInfo);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("videoCallOffHold")
	public void videoCallOffHold(VideoCallInfo interpreterCallInfo) {
		serverInstance.videoCallOffHold(interpreterCallInfo);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("disconnect")
	public void disconnect(VideoCallInfo interpreterCallInfo) {
		serverInstance.disconnect(interpreterCallInfo);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("callIncoming")
	public void callIncoming(VideoCallInfo interpreterCallInfo) {
		serverInstance.callIncoming(interpreterCallInfo);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("statusChanged")
	public void statusChanged(VideoCallInfo interpreterCallInfo) {
		serverInstance.statusChanged(interpreterCallInfo);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("playVideoRequest")
	public void playVideoRequest(VideoCallInfo videoCallInfo) {
		serverInstance.playVideoRequest(videoCallInfo);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("pauseVideoRequest")
	public void pauseVideoRequest(VideoCallInfo videoCallInfo) {
		serverInstance.pauseVideoRequest(videoCallInfo);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("bluestreamInitiateCall")
	public void bluestreamInitiateCall(VideoCallInfo videoCallInfo) {
		serverInstance.bluestreamInitiateCall(videoCallInfo);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("bluestreamOnHold")
	public void bluestreamOnHold(VideoCallInfo videoCallInfo) {
		serverInstance.bluestreamOnHold(videoCallInfo);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("bluestreamOffHold")
	public void bluestreamOffHold(VideoCallInfo videoCallInfo) {
		serverInstance.bluestreamOffHold(videoCallInfo);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("bluestreamHangup")
	public void bluestreamHangup(VideoCallInfo videoCallInfo) {
		serverInstance.bluestreamHangup(videoCallInfo);
	}
}
