package com.telelanguage.videoapi;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/customer/v1")
public class TLVXCustomerAPIServiceServlet implements TLVXCustomerAPIService {

	static TLVXCustomerAPIService serverInstance;
	
	static public void setServerInstance(TLVXCustomerAPIService serverInstance) {
		TLVXCustomerAPIServiceServlet.serverInstance = serverInstance;
	}
	
	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("registerCustomer")
	public CustomerLoginResponse registerCustomer(CustomerRegistrationRequest request) {
		return serverInstance.registerCustomer(request);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("loginCustomer")
	public CustomerLoginResponse loginCustomer(CustomerLoginRequest request) {
		return serverInstance.loginCustomer(request);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("requestInterpreter")
	public void requestInterpreter(InterpreterRequest request) {
		serverInstance.requestInterpreter(request);
	}
	
	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("logoutCustomer")
	public CustomerLogoutResponse logoutCustomer(CustomerLogoutRequest request) {
		return serverInstance.logoutCustomer(request);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("hangupCustomer")
	public CustomerHangupResponse hangupCustomer(CustomerHangupRequest request) {
		return serverInstance.hangupCustomer(request);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("videoSessionStarted")
	public void videoSessionStarted(CustomerVideoEstablished request) {
		serverInstance.videoSessionStarted(request);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("requestCredentials")
	public void requestCredentials(CredentialRequest request) {
		serverInstance.requestCredentials(request);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("scheduleInterpreter")
	public void scheduleInterpreter(ScheduleInterpreterRequest request) {
		serverInstance.scheduleInterpreter(request);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("bluestreamConnected")
	public void bluestreamConnected(BlueStreamConnected request) {
		serverInstance.bluestreamConnected(request);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("bluestreamAgentRequest")
	public void bluestreamAgentRequest(BlueStreamConnected request) {
		serverInstance.bluestreamAgentRequest(request);
	}

	@Override
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("bluestreamDisconnected")
	public void bluestreamDisconnected(BlueStreamConnected request) {
		serverInstance.bluestreamDisconnected(request);
	}
}
