<?xml version="1.0" encoding="UTF-8"?>
<%@ page import="com.telelanguage.tlvx.service.*" %>
<%
	boolean valid = TLVXManager.companyManager.checkDepartmentCode(request.getParameter("deptcode"),request.getParameter("accesscode"));
	System.out.println("got valid: " + valid + " for accesscode: " + request.getParameter("accesscode")+ " and deptcode: " + request.getParameter("deptcode"));
%>
<vxml xmlns="http://www.w3.org/2001/vxml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://www.w3.org/2001/vxml http://www.w3.org/TR/voicexml20/vxml.xsd" version="2.0">
    <var name="accesscode" expr="'<%= request.getParameter("accesscode") %>'"/>
    <var name="language" expr="'<%= request.getParameter("language") %>'"/>
    <var name="deptcode" expr="'<%= request.getParameter("deptcode") %>'"/>
    <var name="failures" expr="<%= request.getParameter("failures") %>"/>
    <var name="dtmfpin" expr="'<%= request.getParameter("dtmfpin") %>'"/>
    <form id="welcome">
    	<block cond="true == <%= valid %>">
			<goto expr="'welcome.jsp?accesscode=' + accesscode + '&amp;language=' + language + '&amp;dtmfpin=' + dtmfpin + '&amp;deptcode=' + deptcode + '&amp;codevalid=<%= valid %>&amp;failures=' + failures + '#deptcheckcode'"/>
    	</block>
    	<block cond="false == <%= valid %>">
            <prompt bargein="false">
				<audio src="prompts/dept_code_not_found.wav">
					I'm sorry, we don't have that department code in our system.
				</audio>
            </prompt>
            <assign name="failures" expr="failures + 1"/>
			<goto expr="'welcome.jsp?codevalid=false&amp;accesscode=' + accesscode + '&amp;language=' + language + '&amp;dtmfpin=' + dtmfpin + '&amp;failures=' + failures + '#deptcheckcode'"/>
        </block>
    </form>
</vxml>
