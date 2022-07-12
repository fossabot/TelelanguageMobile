package com.telelanguage.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/v1")
public class INTAPIServiceServlet implements INTAPIService {
	
	static INTAPIService serverInstance;
	
	static public void setServerInstance(INTAPIService serverInstance) {
		INTAPIServiceServlet.serverInstance = serverInstance;
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("interpreterOnHold")
	public void interpreterOnHold(InterpreterCallInfo interpreterCallInfo) {
		serverInstance.interpreterOnHold(interpreterCallInfo);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("interpreterOffHold")
	public void interpreterOffHold(InterpreterCallInfo interpreterCallInfo) {
		serverInstance.interpreterOffHold(interpreterCallInfo);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("askToAcceptCall")
	public void askToAcceptCall(InterpreterCallInfo interpreterCallInfo) {
		serverInstance.askToAcceptCall(interpreterCallInfo);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("disconnect")
	public void disconnect(InterpreterCallInfo interpreterCallInfo) {
		serverInstance.disconnect(interpreterCallInfo);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("callIncoming")
	public void callIncoming(InterpreterCallInfo interpreterCallInfo) {
		serverInstance.callIncoming(interpreterCallInfo);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("agentDisconnected")
	public void agentDisconnected(InterpreterCallInfo interpreterCallInfo) {
		serverInstance.agentDisconnected(interpreterCallInfo);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("interpreterNotAcceptingCalls")
	public void interpreterNotAcceptingCalls(
			InterpreterCallInfo interpreterCallInfo) {
		serverInstance.agentDisconnected(interpreterCallInfo);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("interpreterLogout")
	public void interpreterLogout(InterpreterCallInfo interpreterCallInfo) {
		serverInstance.interpreterLogout(interpreterCallInfo);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("callStatusUpdate")
	public void callStatusUpdate(InterpreterCallInfo interpreterCallInfo) {
		serverInstance.callStatusUpdate(interpreterCallInfo);
	}
}
