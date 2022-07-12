<?xml version="1.0"?>
<vxml version="2.1" application="approot.jsp">
	<meta http-equiv="Cache-Control" content="no-cache"/>
	
	<jsp:useBean id="interpreter" class="com.telelanguage.api.InterpreterInfo" scope="session"/>
	
	<%
	interpreter = com.telelanguage.interpreter.service.TLVXManager.interpreterDAO.findInterpreterByAccess(interpreter.getAccessCode());
	interpreter.setActiveSession(false);
	%>
	
	<form>
		<block>
			<prompt>
				You are logged out.
				Thank you for calling.
			</prompt>
			
			<disconnect/>
		</block>
	</form>
</vxml>