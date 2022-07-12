<?xml version="1.0"?>
<vxml version="2.1"  xmlns:voxeo="http://community.voxeo.com/xmlns/vxml" application="approot.jsp">
	<meta http-equiv="Cache-Control" content="no-cache"/>
	
	<form>
		<var name="accessNumber"/>
		
		<block>
			<voxeo:recordcall value="100" info="INTER-LOGIN" />
		</block>
		
		<field name="accessNum">
		    <grammar src="builtin:dtmf/number?length=5" />
			<prompt>
				<break time="1000"/> <break time="1000"/>
				Welcome to the Telelanguage Interpreter Access Number.
			</prompt>
		
			<prompt>
				Please enter your five digit Access Code.
			</prompt>
			
			<filled>
				<goto nextitem="confirm"/>
			</filled>
			
			<catch event="nomatch noinput" count="1">
				<prompt>
						Sorry, I didn't get that.
				</prompt>
				
				<reprompt/>
			</catch>
			<catch event="nomatch noinput" count="2">
				<prompt>
						Sorry, I still didn't get that.
				</prompt>
				
				<reprompt/>
			</catch>
			<catch event="nomatch noinput" count="3">
				<prompt>
						Sorry, I couldn't get that.  Please try
						calling again later.
				</prompt>
				
				<disconnect/>
			</catch>
		</field>
		
		<field name="confirm" type="boolean">
			<prompt>
				I got access code
				<say-as interpret-as="vxml:digits">
					<value expr="accessNum"/>
				</say-as>
				Is that correct?
				<break time="1000"/> <break time="1000"/>
				Please say yes or no.
			</prompt>
			
			<filled>
				<if cond="confirm">
				    <assign name="accessNumber" expr="'777' + accessNum"/>
					<goto nextitem="check"/>
				<else/>
					<prompt>
						Okay.
					</prompt>
					<clear namelist="accessNum accessNumber confirm"/>
					<goto nextitem="accessNum"/>
				</if>
			</filled>
			
			<catch event="noinput nomatch" count="1">
				<prompt>
					I am sorry I didn't get that. Please press
					1 for yes or 2 for no.
				</prompt>
			</catch>
			<catch event="noinput nomatch" count="2">
				<prompt>
					I am sorry I still didn't get that. Please
					press 1 for yes or 2 for no.
				</prompt>
			</catch>
			<catch event="nomatch noinput" count="3">
				<prompt>
					Sorry, I couldn't get that.  Please try
					calling again later.
				</prompt>
				
				<disconnect/>
			</catch>
		</field>
		
		<subdialog name="check" src="checkAccessCode.jsp" method="POST"
				namelist="accessNumber">
			<filled>
				<if cond="check.result == 'loggedIn'">
					<!-- see if they want to log out -->
					<goto next="checkLogOut.jsp"/>
				<elseif cond="check.result == 'loggedOut'"/>
					<!-- see if they want to log in -->
					<goto next="checkLogIn.jsp"/>
				<else/>
					<!-- not found, try again -->
					<prompt>
						Sorry, I don't have an interpreter registered with
						that access code.
					</prompt>
					<clear namelist="check accessNum accessNumber"/>
					<goto nextitem="accessNum"/>
				</if>
			</filled>
		</subdialog>
	</form>
</vxml>
