<?xml version="1.0"?>
<vxml version="2.1" application="approot.jsp">
	<meta http-equiv="Cache-Control" content="no-cache"/>
	
	<form>
		<%
			String access = request.getParameter("accessNumber");
				com.telelanguage.api.InterpreterInfo interpreter = 
				com.telelanguage.interpreter.service.TLVXManager.interpreterDAO.findInterpreterByAccess(access);
				
				session.setAttribute("interpreter", interpreter);
				
				if (interpreter == null) {
		%>
			<var name="result" expr="'notFound'"/>
			<%
		} else if (interpreter.getActiveSession()) {
			%>
			<var name="result" expr="'loggedIn'"/>
			<%
		} else {
			%>
			<var name="result" expr="'loggedOut'"/>
			<%
		}
		%>
		<block>
			<return namelist="result"/>
		</block>
	</form>
</vxml>