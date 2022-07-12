<?xml version="1.0" encoding="UTF-8"?>
<%
	String accesscode = request.getParameter("accesscode");
	if (null == accesscode)
	{
		accesscode = "9999";
	}
	
	String accesscodeshort = request.getParameter("accesscodeshort");
	if (null == accesscodeshort)
	{
		accesscodeshort = "9999";
	}	
%>
<vxml xmlns="http://www.w3.org/2001/vxml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://www.w3.org/2001/vxml http://www.w3.org/TR/voicexml20/vxml.xsd" version="2.0">
    <var name="language" expr="'<%= request.getParameter("language") %>'"/>
    <var name="promptURL" expr="'<%= request.getParameter("promptURL") %>'"/>
    <var name="expectedid" expr="'<%= accesscode %>'"/>
    <var name="expectedid_short" expr="'<%= accesscodeshort %>'"/>
    <var name="validated" expr="false"/>
    <var name="failures" expr="0"/>
    <var name="dialogreason" expr="'no reason'"/>
    <property name="inputmodes" value="dtmf"/>
    <form>
        <field name="interpreterid" type="digits?length=1">
            <prompt bargein="true" bargeintype="hotword" timeout="8s">
                <audio expr="'prompts/interpreteraccept'+language+'.wav'">
                   This is a Telah-language <value expr="language"/>  call.
                   	Press one to accept, two to decline.
                </audio>
            </prompt>
            <noinput>
                <assign name="failures" expr="failures + 1"/>
                <if cond="failures >= 1">
                    <prompt>
	                    <audio src="prompts/we_could_not_hear_you.wav">
	                        We could not hear you.  Goodbye.
                        </audio>
                    </prompt>
                    <assign name="dialogreason" expr="'too many failed access codes - did not hear'"/>
                    <exit namelist="validated dialogreason"/>
                <else/>
                    <reprompt/>
                </if>
            </noinput>
            <nomatch>
            	<assign name="failures" expr="failures + 1"/>
                <prompt>
	                <audio src="prompts/sorry_i_did_not_understand.wav">
	                    Sorry, I did not understand.
                    </audio>
                </prompt>
                <assign name="failures" expr="failures + 1"/>
                <if cond="failures >= 1">
                    <prompt>
	                    <audio src="prompts/goodbye.wav">
	                        Goodbye.
                        </audio>
                    </prompt>
                    <assign name="dialogreason" expr="'too many failed access codes - did not understand'"/>
                    <exit namelist="validated dialogreason"/>
                <else/>
                    <reprompt/>
                </if>
            </nomatch>
            <filled>
                <if cond="interpreterid != 1">
                    <assign name="failures" expr="failures + 1"/>
                    <prompt>
	                    <audio src="prompts/you_have_declined_this_call.wav">
	                        You have declined this call and are now logged out.
                        </audio>
                    </prompt>
                    <assign name="dialogreason" expr="'pressed '+interpreterid"/>
                    <exit namelist="validated dialogreason"/>
                <else/>
		        	<if cond="promptURL != ''">
		        		<prompt><audio expr="promptURL"></audio></prompt>
		        	</if>
		            <assign name="validated" expr="true"/>
		            <exit namelist="validated"/>
                </if>
            </filled>
        </field>
    </form>
</vxml>