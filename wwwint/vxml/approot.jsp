<?xml version="1.0"?>
<vxml version="2.1">
	<meta http-equiv="Cache-Control" content="no-cache"/>
	
	<var name="DNIS" expr="session.connection.local.uri"/>
	<var name="ANI" expr="session.connection.remote.uri"/>
	
	<property name="termtimeout" value="1s"/>
	<property name="sensitivity" value="0.30"/>
	
	<catch event="connection.disconnect.hangup">
		<log>
			caught disconnect!
		</log>
		<submit next="finish.jsp?reason=hangup"/>
	</catch>
	
	<catch event="error">
		<log>
			caught error!
		</log>
		<prompt>
			I'm sorry.  There has been a technical problem.  Please
			try again later.
		</prompt>
		<submit next="finish.jsp?reason=error"/>
	</catch>
</vxml>