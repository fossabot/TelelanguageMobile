<?xml version="1.0"?>
<vxml version="2.1" application="approot.jsp">
	<meta http-equiv="Cache-Control" content="no-cache"/>
	
	<jsp:useBean id="interpreter" class="com.telelanguage.api.InterpreterInfo" scope="session"/>
	
	<%
	interpreter = com.telelanguage.interpreter.service.TLVXManager.interpreterDAO.findInterpreterByAccess(interpreter.getAccessCode());

	String phone = request.getParameter("phone");
	String areaCode = phone.substring(0, 3);
	String phoneNumber = phone.substring(3);
	interpreter.setAreaCode(areaCode);
	interpreter.setPhoneNumber(phoneNumber);
	interpreter.setActiveSession(true);
	interpreter.setNumMissedCalls(0);
	%>
	
	<form>
		<block>
			<prompt>
					Thank you for calling.
			</prompt>
			
			<disconnect/>
		</block>
	</form>
</vxml>