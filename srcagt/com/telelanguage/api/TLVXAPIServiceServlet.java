package com.telelanguage.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/v1")
public class TLVXAPIServiceServlet implements TLVXAPIService {
	
	static TLVXAPIService serverInstance;
	
	static public void setServerInstance(TLVXAPIService serverInstance) {
		TLVXAPIServiceServlet.serverInstance = serverInstance;
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("findInterpreterByEmail")
	public InterpreterInfo findInterpreterByEmail(String email) {
		return serverInstance.findInterpreterByEmail(email);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("saveInterpreter")
	public void saveInterpreter(InterpreterInfo interpreter) {
		serverInstance.saveInterpreter(interpreter);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("markInterpreterOffCall")
	public void markInterpreterOffCall(InterpreterInfo interpreter) {
		serverInstance.markInterpreterOffCall(interpreter);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("checkCommunicationsOk")
	public Boolean checkCommunicationsOk(String thisServerUrl) {
		return serverInstance.checkCommunicationsOk(thisServerUrl);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("hangupCall")
	public void hangupCall(InterpreterInfo interpreter) {
		serverInstance.hangupCall(interpreter);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("acceptCall")
	public void acceptCall(InterpreterInfo interpreter) {
		serverInstance.acceptCall(interpreter);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("rejectCall")
	public void rejectCall(InterpreterInfo interpreter) {
		serverInstance.rejectCall(interpreter);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("requestAgent")
	public void requestAgent(InterpreterInfo interpreter) {
		serverInstance.requestAgent(interpreter);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("acceptVideo")
	public void acceptVideo(InterpreterInfo interpreter) {
		serverInstance.acceptVideo(interpreter);
	}
	
	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("acceptVideoOnly")
	public void acceptVideoOnly(InterpreterInfo interpreter) {
		serverInstance.acceptVideoOnly(interpreter);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("dontAcceptVideo")
	public void dontAcceptVideo(InterpreterInfo interpreter) {
		serverInstance.dontAcceptVideo(interpreter);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("playCustomerVideo")
	public void playCustomerVideo(InterpreterInfo interpreter) {
		serverInstance.playCustomerVideo(interpreter);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("pauseCustomerVideo")
	public void pauseCustomerVideo(InterpreterInfo interpreter) {
		serverInstance.pauseCustomerVideo(interpreter);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("videoSessionStarted")
	public void videoSessionStarted(InterpreterInfo interpreter) {
		serverInstance.videoSessionStarted(interpreter);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("dialThirdParty")
	public void dialThirdParty(InterpreterInfo interpreter) {
		serverInstance.dialThirdParty(interpreter);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("hangupThirdParty")
	public void hangupThirdParty(InterpreterInfo interpreter) {
		serverInstance.hangupThirdParty(interpreter);
	}
}
