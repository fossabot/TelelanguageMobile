<?xml version="1.0" encoding="UTF-8"?>
<%@ page import="com.telelanguage.tlvx.service.*" %>
<%
	boolean valid = TLVXManager.companyManager.checkAccessCode(request.getParameter("accesscode"), request.getParameter("subscriptionCode"));
	boolean askDeptCode = TLVXManager.companyManager.shouldAskDepartmentCode(request.getParameter("accesscode"));
	boolean sendToAgent = TLVXManager.companyManager.shouldSendToAgentQueue(request.getParameter("accesscode"));
	boolean askDtmfPin = TLVXManager.companyManager.shouldAskDtmf(request.getParameter("accesscode"));
	String dtmfpin = request.getParameter("dtmfpin");
	String deptCode = request.getParameter("deptcode");
	if (askDtmfPin == false) {
		dtmfpin = "dontask";
	}
	if (askDeptCode == false)
	{
	    deptCode = "dontask";
	}
	String failures = request.getParameter("failures");
	//System.out.println("**** Inside of check_code.jsp - failures: " + failures + " ****");
	if(null == failures)
	{
		failures = "0";
	}
%>
<vxml xmlns="http://www.w3.org/2001/vxml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://www.w3.org/2001/vxml http://www.w3.org/TR/voicexml20/vxml.xsd" version="2.0">
    <var name="accesscode" expr="'<%= request.getParameter("accesscode") %>'"/>
    <var name="language" expr="'<%= request.getParameter("language") %>'"/>
    <var name="deptcode" expr="'<%= deptCode %>'"/>
    <var name="dtmfpin" expr="'<%= dtmfpin %>'"/>
    <var name="failures" expr="<%=  failures %>"/>
    <form id="welcome">
        <block cond="true == <%= sendToAgent %>">
			<exit namelist="accesscode"/>
    	</block>
    	<block cond="true == <%= valid %>">
			<goto expr="'welcome.jsp?accesscode=' + accesscode + '&amp;dtmfpin=' + dtmfpin +  '&amp;language=' + language + '&amp;deptcode=' + deptcode + '&amp;codevalid=<%= valid %>&amp;failures=' + failures + '#checkcode'"/>
    	</block>
    	<block cond="false == <%= valid %>">
            <audio src="prompts/access_code_not_found.wav">
			<prompt bargein="false">
				I'm sorry, we don't have that access code in our system.
            </prompt>
			</audio>
            <assign name="failures" expr="failures + 1"/>
			<goto expr="'welcome.jsp?codevalid=false&amp;failures=' + failures + '#checkcode'"/>
        </block>
    </form>
</vxml>
