<?xml version="1.0"?>
<vxml version="2.1" application="approot.jsp">
	<meta http-equiv="Cache-Control" content="no-cache"/>
	
	<jsp:useBean id="interpreter" class="com.telelanguage.api.InterpreterInfo" scope="session"/>
	
	<%
	String phone = interpreter.getAreaCode()+interpreter.getPhoneNumber();
	%>
	
	<form>
		<var name="originalPhone" expr="'<%=phone%>'"/>
		<var name="phone"/>
		<var name="dashed"/>
		
		<field name="logIn" type="boolean">
			<prompt>
				You are currently logged out. Would you like to log in?
				<break time="1000"/> <break time="1000"/>
				Please say yes or no.
			</prompt>
			
			<filled>
				<if cond="logIn">
					<if cond="originalPhone == 'not found'">
						<assign name="changePhone" expr="'filled'"/>
						<goto nextitem="getPhone"/>
					<else/>
						<goto nextitem="changePhone"/>
					</if>
				<else/>
					<prompt>
						Okay, I will leave your status as it is.
						Call in again to change it.  Good bye.
					</prompt>
					
					<disconnect/>
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
		
		<field name="changePhone" type="boolean">
			<prompt>
				Your phone number is currently set to
				<say-as interpret-as="telephone">
					<value expr="originalPhone"/>
				</say-as>
				Would you like to change it?
				<break time="1000"/> <break time="1000"/>
				Please say yes or no.
			</prompt>
			
			<filled>
				<if cond="changePhone">
					<goto nextitem="getPhone"/>
				<else/>
					<assign name="dashed" expr="originalPhone"/>
					<assign name="phone" expr="dashed.replace('-','').replace('-','')"/>
					<goto nextitem="logInPhone"/>
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
		
		<field name="getPhone" type="digits?length=10">
			<prompt>
				Please say or enter the phone number where you can be reached.
			</prompt>
			
			<filled>
				<assign name="phone" expr="getPhone"/>
				<assign name="dashed" expr="phone.substring(0,3) + '-'
					+ phone.substring(3,6) + '-' + phone.substring(6)"/>
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
				You are logged in at
				<say-as interpret-as="telephone">
					<value expr="dashed"/>
				</say-as>
				Is that right?
				<break time="1000"/> <break time="1000"/>
				Please say yes or no.
			</prompt>
			
			<filled>
				<if cond="confirm">
					<goto nextitem="logInPhone"/>
				<else/>
					<prompt>
						Okay.
					</prompt>
					<clear namelist="getPhone phone dashed confirm"/>
					<goto nextitem="getPhone"/>
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
		
		<subdialog name="logInPhone" src="logIn.jsp" namelist="phone">
			<filled>
				<!-- something?? -->
			</filled>
		</subdialog>
	</form>
</vxml>