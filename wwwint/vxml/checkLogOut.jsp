<?xml version="1.0"?>
<vxml version="2.1" application="approot.jsp">
	<meta http-equiv="Cache-Control" content="no-cache"/>
	
	<form>
		<field name="logOut" type="boolean">
			<prompt>
				You are currently logged in. Would you like to log out?
				<break time="1000"/> <break time="1000"/>
				Please say yes or no.
			</prompt>
			
			<filled>
				<if cond="logOut">
					<goto next="logOut.jsp"/>
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
	</form>
</vxml>