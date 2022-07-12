<?xml version="1.0" encoding="UTF-8"?>
<%
	String accesscode = request.getParameter("accesscode");
	System.out.println("**** Inside of welcome.jsp - accesscode: " + accesscode + " ****");
	if (null == accesscode || ("").equals(accesscode) || ("undefined").equals(accesscode))
	{
		accesscode = "";
	}
	else
	{
		accesscode = "'" + accesscode + "'";
	}

	String language = request.getParameter("language");
	System.out.println("**** Inside of welcome.jsp - language: " + language + " ****");
	if (null == language || ("").equals(language) || ("undefined").equals(language))
	{
		language = "";
	}
	else
	{
		language = "'" + language + "'";
	}

	String deptcode = request.getParameter("deptcode");
	System.out.println("**** Inside of welcome.jsp - deptcode: " + deptcode + " ****");
	if (null == deptcode || ("").equals(deptcode) || ("undefined").equals(deptcode))
	{
		deptcode = "";
	}
	else
	{
		deptcode = "'" + deptcode + "'";
	}
	
	String dtmfpin = request.getParameter("dtmfpin");
	System.out.println("**** Inside of welcome.jsp - dtmfpin: " + dtmfpin + " ****");
	if (null == dtmfpin || ("").equals(dtmfpin) || ("undefined").equals(dtmfpin))
	{
		dtmfpin = "";
	}
	else
	{
		dtmfpin = "'" + dtmfpin + "'";
	}

	String codevalid = request.getParameter("codevalid");
	if (null == codevalid)
	{
		codevalid = "false";
	}

	String failures = request.getParameter("failures");
	System.out.println("**** Inside of welcome.jsp - failures: " + failures + " ****");
	if (null == failures)
	{
		failures = "0";
	}
	
	String promptURL = request.getParameter("promptURL");
	if (null == promptURL || ("").equals(promptURL) || ("undefined").equals(promptURL))
	{
		promptURL = "''";
	}
	else
	{
		promptURL = "'" + promptURL + "'";
	}
	
	String promptText = request.getParameter("promptText");
	if (null == promptText || ("").equals(promptText) || ("undefined").equals(promptText))
	{
		promptText = "''";
	}
	else
	{
		promptText = "'" + promptText + "'";
	}

	String subscriptionCode = request.getParameter("subscriptionCode");
	if (null == subscriptionCode || ("").equals(subscriptionCode) || ("undefined").equals(subscriptionCode))
	{
		subscriptionCode = "''";
	}
	else
	{
		subscriptionCode = "'" + subscriptionCode + "'";
	}
	
	String customDeptPromptURL = request.getParameter("customDeptPromptURL");
	if (null == customDeptPromptURL || ("").equals(customDeptPromptURL) || ("undefined").equals(customDeptPromptURL))
	{
		customDeptPromptURL = "''";
	}
	else
	{
		customDeptPromptURL = "'" + customDeptPromptURL + "'";
	}
%>
<vxml xmlns="http://www.w3.org/2001/vxml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://www.w3.org/2001/vxml http://www.w3.org/TR/voicexml20/vxml.xsd" version="2.0">
	<!-- newfound added (nfa) -->
	<meta http-equiv="Cache-Control" content="no-cache"/>
	<!-- end nfa -->
    <var name="failures" expr="<%= failures %>"/>
    <var name="promptURL" expr="<%= promptURL %>"/>
    <var name="promptText" expr="<%= promptText %>"/>
    <var name="customDeptPromptURL" expr="<%= customDeptPromptURL %>"/>
    <var name="subscriptionCode" expr="<%= subscriptionCode %>"/>
    <var name="noCount" expr="0"/>
    <var name="confirm" expr="'filled'"/>
    <var name="languages" expr="new Array()"/>
    <var name="skips" expr="new Array()"/>
    <var name="index" expr="0"/>
	<var name="promptFilename" expr="''"/>
	<var name="accesscode" expr="<%= accesscode %>"/>
	<var name="deptcode" expr="<%= deptcode %>" />
	<var name="dtmfpin" expr="<%= dtmfpin %>" />
	<var name="language" expr="<%= language %>"/>
    <var name="accessCodeArray" />
	<var name="confirmFailures" expr="0" />
    <var name="confirmLanguageFailures" expr="0" />
    <var name="chineseLanguageFailures" expr="0" />
	
    <property name="sensitivity" value="0.20"/>
	
	<link next="#exit" dtmf="0">
		<grammar root="oper" type="application/grammar-xml"
				xml:lang="en-US" xmlns="http://www.w3.org/2001/06/grammar">
			<rule id="oper" scope="public">
				<one-of>
					<item>
						operator
					</item>
					<item>
						agent
					</item>
					<item>
						representative
					</item>
				</one-of>
			</rule>
		</grammar>
	</link>
	
	<link next="#exit">
		<grammar src="forgot.grxml" type="application/srgs+xml"/>
	</link>
	
    <form id="getcalltype">
		<block>
			<if cond="promptURL != ''">
				<prompt timeout="0s" bargein="false">
					<audio expr="promptURL"></audio>
                </prompt>
			<elseif cond="promptText != ''"/>
				<prompt timeout="0s" bargein="false">
					<value expr="promptText"/>
				</prompt>
			</if>
		</block>
		<block>
			<goto next="#getcallinfo"/>
                </block>

		<field name="calltype" type="digits">
			<prompt>
				<audio src="prompts/main_menu.wav">
				For telephonic interpretation press one. For face two face interpretation press two.
				For all other inquiries press three.
				</audio>
			</prompt>
			<noinput>
				<prompt>
					<audio src="prompts/no_input1.wav">
                                        Sorry, I did not get that.
                                        </audio>
				</prompt>
				<assign name="failures" expr="failures + 1"/>
				<if cond="failures >= 2">
					<exit namelist="calltype"/>
				</if>
			</noinput>
			<nomatch>
				<prompt>
					<audio src="prompts/no_match1.wav">
                                        Sorry, I did not understand.
                                        </audio>
				</prompt>
				<assign name="failures" expr="failures + 1"/>
				<if cond="failures >= 2">
					<exit namelist="calltype"/>
				<else/>
					<reprompt/>
				</if>
			</nomatch>
			<filled>
				<if cond="calltype == 1">
					<assign name="failures" expr="0"/>
					<goto next="#getcallinfo"/>
				<elseif cond="calltype == 2"/>
					<exit namelist="calltype"/>
				<elseif cond="calltype == 3"/>
					<exit namelist="calltype"/>
				</if>
			</filled>
		</field>
	</form>

	<form id="getcallinfo">
		<field name="accesscode" type="digits" expr="<%= accesscode %>">
			<property name="sensitivity" value="0.20"/>
			<property name="confidencelevel" value="0.35"/>
			
			<prompt>
				<audio src="prompts/get_access_code.wav">
					What is your access code?
				</audio>
			</prompt>
			<noinput>
				<prompt bargein="false">
					<audio src="prompts/no_input1.wav">
						Sorry, I did not get that.
					</audio>
				</prompt>
				<assign name="failures" expr="failures + 1"/>
				<if cond="failures >= 2">
					<prompt bargein="false">
						<audio src="prompts/agent_connect1.wav">
						 	One moment while I connect you with an agent who can assist you.
						</audio>
					</prompt>
					<exit namelist="language accesscode deptcode dtmfpin"/>
				</if>
				<reprompt/>
			</noinput>
			<nomatch>
				<prompt bargein="false">
					<audio src="prompts/no_match1.wav">
						Sorry, I did not understand.
					</audio>
				</prompt>
				<assign name="failures" expr="failures + 1"/>
				<if cond="failures >= 2">
					<prompt bargein="false">
						<audio src="prompts/agent_connect1.wav">
						 	One moment while I connect you with an agent who can assist you.
						</audio>
					</prompt>
					<exit namelist="language accesscode deptcode dtmfpin"/>					
				</if>
				<reprompt/>
			</nomatch>
			<filled>
				<!-- <prompt>Oh kay. Access code, <value expr="accesscode$.interpretation"/>.</prompt> -->
				<!-- assign name="failures" expr="0"/ -->
				<log expr="'**********************************************'" label="trace:?level=ERROR"/>
				<log expr="'CONFIDENCE = ' +lastresult$.confidence" label="trace:?level=ERROR"/>
				<log expr="'UTTERANCE = ' + lastresult$.utterance" label="trace:?level=ERROR"/>
				<log expr="'INPUTMODE = ' + lastresult$.inputmode" label="trace:?level=ERROR"/>
				<log expr="'INTERPRETATION = ' + lastresult$.interpretation" label="trace:?level=ERROR"/>
				<log expr="'ACCESSCODE = ' + accesscode" label="trace:?level=ERROR"/>
				<log expr="'**********************************************'"/>
				<clear namelist="confirm"/>
				<assign name="accessCodeArray" expr="accesscode.split('')" />
				<goto nextitem="confirmAccessCode"/>
			</filled>
		</field>
		
		<field name="confirmAccessCode" type="boolean" expr="confirm">
			<property name="sensitivity" value="0.20"/>
			<property name="confidencelevel" value="0.35"/>
			
			<prompt>
				<audio src="prompts/access_code_confirm.wav">
						I got access code
				</audio>
				<foreach item="digit" array="accessCodeArray">
					<audio expr="'prompts/numbers/' + digit + '.wav'"/>
				</foreach>
				<audio src="prompts/confirm_q.wav">
						Is that correct?
				</audio>
			</prompt>
			
			<noinput>
				<prompt bargein="false">
					<audio src="prompts/no_input1.wav">
						Sorry, I did not get that.
					</audio>
				</prompt>
				<assign name="confirmFailures" expr="confirmFailures + 1"/>
				<if cond="confirmFailures >= 2">
					<prompt bargein="false">
						<audio src="prompts/agent_connect1.wav">
						 	One moment while I connect you with an agent who can assist you.
						</audio>
					</prompt>
					<exit namelist="language accesscode deptcode dtmfpin"/>
				</if>
				<clear namelist="confirmAccessCode"/>
				<reprompt/>
			</noinput>
			<nomatch>
				<prompt bargein="false">
					<audio src="prompts/no_match1.wav">
						Sorry, I did not understand.
					</audio>
				</prompt>
				<assign name="confirmFailures" expr="confirmFailures + 1"/>
				<if cond="confirmFailures >= 2">
					<prompt bargein="false">
						<audio src="prompts/agent_connect1.wav">
						 	One moment while I connect you with an agent who can assist you.
						</audio>
					</prompt>
					<exit namelist="language accesscode deptcode dtmfpin"/>					
				</if>
				<clear namelist="confirmAccessCode"/>
				<reprompt/>
			</nomatch>
			
			<filled>
				<assign name="confirm" expr="'filled'"/>
					<if cond="confirmAccessCode">
						<assign name="noCount" expr="0"/>
		
						<goto expr="'check_code.jsp?failures=' + failures + '&amp;accesscode=' + accesscode + '&amp;language=' + language + '&amp;dtmfpin=' + dtmfpin + '&amp;deptcode=' + deptcode + '&amp;subscriptionCode=' + subscriptionCode"/>
					<else/>
						<assign name="noCount" expr="noCount + 1"/>
						<if cond="noCount &lt; 2">
						<prompt>
							<audio src="prompts/retry_access_code.wav">
								Oh kay, let's try that again.
							</audio>
						</prompt>
						<clear namelist="confirmAccessCode accesscode"/>
						<goto nextitem="accesscode"/>
						<else/>
							<prompt bargein="false">
								<audio src="prompts/agent_connect1.wav">
									One moment while I connect you with an agent who can assist you.
								</audio>
							</prompt>
							<exit namelist="language accesscode deptcode dtmfpin"/>
						 </if>
					</if>
			</filled>
		</field>

		<field name="language" expr="<%= language %>">
			<property name="sensitivity" value="0.17"/>
			<property name="confidencelevel" value="0.05"/>
			<property name="maxnbest" value="3"/>
			
			<grammar src="languages.grxml" type="application/srgs+xml"/>
			
			<prompt>
				<audio src="prompts/get_language.wav">
					What language do you require?
				</audio>
			</prompt>
            <noinput>
               <prompt bargein="false">
            	   <audio src="prompts/no_input1.wav">
                	    I'm sorry. I did not get that.
                   </audio>
				</prompt>
				<assign name="failures" expr="failures + 1"/>
				<if cond="failures >= 2">
					<log>
						<value expr="'******** Inside of welcome.jsp#language maxnoinput got: accesscode='+accesscode+' language='+language+' deptcode='+deptcode" />
					</log>
					<prompt bargein="false">
						<audio src="prompts/agent_connect2.wav">
							Please hold while I connect you with an agent.
						</audio>
                    </prompt>
					<exit namelist="language accesscode deptcode dtmfpin"/>
				<else/>
					<reprompt/>
                 </if>
             </noinput>
                        
             <nomatch>
				<prompt bargein="false">
					<audio src="prompts/no_match1.wav">
						Sorry, I did not understand.
					</audio>
				</prompt>
				<assign name="failures" expr="failures + 1"/>
				<if cond="failures >= 2">
					<log>
						<value expr="'******** Inside of welcome.jsp#language maxnomatch got: accesscode='+accesscode+' language='+language+' deptcode='+deptcode" />
					</log>
					<prompt bargein="false">
						<audio src="prompts/agent_connect2.wav">
							Please hold while I connect you with an agent.
						</audio>
					</prompt>
					<exit namelist="language accesscode deptcode dtmfpin"/>
				<else/>
					<reprompt/>
				</if>
			</nomatch>
			
			<filled>
				<!-- <prompt>OK. <value expr="language$.interpretation.language"/>.</prompt> -->
				<assign name="failures" expr="0"/>
				<log expr="'**********************************************'" label="trace:?level=ERROR"/>
				<log expr="'CONFIDENCE = ' +lastresult$.confidence" label="trace:?level=ERROR"/>
				<log expr="'UTTERANCE = ' + lastresult$.utterance" label="trace:?level=ERROR"/>
				<log expr="'INPUTMODE = ' + lastresult$.inputmode" label="trace:?level=ERROR"/>
				<log expr="'INTERPRETATION = ' + lastresult$.interpretation.language" label="trace:?level=ERROR"/>
				<log expr="'**********************************************'"/>
				<!-- may want to change above to reflect the chosen one -->
				
				<!-- put the n-best in our array -->
				<assign name="languages" expr="application.lastresult$"/>
				<assign name="promptFilename" expr="lastresult$.interpretation.wavefilename"/>
				<assign name="language" expr="lastresult$.interpretation.language"/>
				<var name="lanLen" expr="languages.length"/>
				<!-- now return highest confidence item not in the skiplist -->
				<script>
					<![CDATA[
						var i;
						var j;
						index = -1;
						for (i=0; i<lanLen; i++) {
							var found = false;
							var lang = languages[i].interpretation.language;
							for (j=0; j<skips.length; j++) {
								if (lang == skips[j]) {
									found = true;
									j = skips.length;
								}
							}
							
							if (!found) {
								index = i;
								// language = languages[i].interpretation.language;
								i = languages.length;
							}
						}
					]]>
				</script>
				<if cond="index == -1">
					<prompt bargein="false">
                        <audio src="prompts/agent_connect1.wav">
                            One moment while I connect you with an agent who can assist you.
                        </audio>
                    </prompt>
                    <assign name="language" expr="''"/>
                    <exit namelist="language accesscode deptcode dtmfpin"/>
				</if>
				<assign name="language" expr="languages[index].interpretation.language"/>
				<clear namelist="confirm"/>
				<goto nextitem="confirmLanguage"/>
			</filled>
		</field>
		
		<field name="confirmLanguage" type="boolean" expr="confirm">
			<prompt>
				<audio src="prompts/ok_i_heard.wav">
					Oh kay. I heard
				</audio>
				<audio expr="'prompts/languages/' + promptFilename + '.wav'">
					<value expr="language"/>
				</audio>			
				<audio src="prompts/confirm_q.wav">
					Is that correct?
				</audio>
			</prompt>    
			
            <noinput>
				<prompt bargein="false">
					<audio src="prompts/no_input1.wav">
						Sorry, I did not get that.
					</audio>
				</prompt>
				<assign name="confirmLanguageFailures" expr="confirmLanguageFailures + 1"/>
				<if cond="confirmLanguageFailures >= 2">
					<prompt bargein="false">
                        <audio src="prompts/agent_connect1.wav">
                            One moment while I connect you with an agent who can assist you.
                        </audio>
                    </prompt>
                    <assign name="language" expr="''"/>
                    <exit namelist="language accesscode deptcode dtmfpin"/>
				</if>
				<clear namelist="confirmLanguage"/>
				<reprompt/>
			</noinput>
			<nomatch>
				<prompt bargein="false">
					<audio src="prompts/no_match1.wav">
						Sorry, I did not understand.
					</audio>
				</prompt>
				<assign name="confirmLanguageFailures" expr="confirmLanguageFailures + 1"/>
				<if cond="confirmLanguageFailures >= 2">
					<prompt bargein="false">
						<audio src="prompts/agent_connect1.wav">
						 	One moment while I connect you with an agent who can assist you.
						</audio>
					</prompt>
                    <assign name="language" expr="''"/>
					<exit namelist="language accesscode deptcode dtmfpin"/>					
				</if>
				<clear namelist="confirmLanguage"/>
				<reprompt/>
			</nomatch>
			
			<filled>
				<assign name="confirm" expr="'filled'"/>
				<if cond="confirmLanguage">
					<assign name="noCount" expr="0"/>
					<if cond="language == 'Chinese'">
                    	<clear namelist="chineseLanguage"/>
					</if>
				<else/>
					<!-- add this language to the skiplist -->
					<script>
						<![CDATA[
							var sLen = skips.length;
							skips.length++;
							skips[sLen] = language;
						]]>
					</script>
					<assign name="index" expr="0"/>
					<assign name="noCount" expr="noCount + 1"/>
					
					<if cond="noCount &lt; 2">
						<prompt>
							<audio src="prompts/retry_access_code.wav">
								Oh kay, let's try that again.
							</audio>
						</prompt>
						<clear namelist="confirmLanguage language"/>
						<goto nextitem="language"/>
					<else/>
						<prompt>
							<audio src="prompts/agent_connect1.wav">
								One moment while I connect you with an agent who can assist you.
							</audio>
						</prompt>
						<assign name="language" expr="''" />
						<exit namelist="language accesscode deptcode dtmfpin"/>
					</if>
				</if>
			</filled>
		</field>
		
		<field name="chineseLanguage" type="digits" expr="0">
			<prompt>
				<audio src="prompts/chinese_language_disambig.wav">
					Which Chinese language do you require? 
							Press 1 for Chinese Cantonese, 
							press 2 for Chinese Mandarin, 
							press 3 for Chinese Toisanese, 
							press 4 for Chinese Taiwanese,
							press 5 for Chinese Chow-saneez, 
							press 6 for Chinese Shanghaieez,
							press 7 for Chinese Siz-wahn,
							or press 8 for Foo-kye-neez Chinese.
										
				</audio>
			</prompt>
		    <filled>
		        <if cond="chineseLanguage == 1">
    				<assign name="language" expr="'Cantonese'"/>
					<assign name="promptFilename" expr="'Cantonese'"/>
		        <elseif cond="chineseLanguage == 2"/>
					<assign name="language" expr="'Mandarin'"/>
					<assign name="promptFilename" expr="'Mandarin'"/>
		        <elseif cond="chineseLanguage == 3"/>
		            <assign name="language" expr="'Chinese Toisanese'"/>
					<assign name="promptFilename" expr="'ChineseToishanese'"/>
		        <elseif cond="chineseLanguage == 4"/>
		            <assign name="language" expr="'Chinese Taiwanese'"/>
					<assign name="promptFilename" expr="'ChineseTaiwanese'"/>
		        <elseif cond="chineseLanguage == 5"/>
		            <assign name="language" expr="'Chinese (Chaosanese)'"/>
					<assign name="promptFilename" expr="'ChineseChaosanese'"/>
		        <elseif cond="chineseLanguage == 6"/>
		            <assign name="language" expr="'Chinese (Shanghaiese)'"/>
					<assign name="promptFilename" expr="'ChineseShanghai'"/>
				<elseif cond="chineseLanguage == 7"/>
					<assign name="language" expr="'Chinese Sichuan'"/>
					<assign name="promptFilename" expr="'ChineseSzechuan'"/>
				<elseif cond="chineseLanguage == 8"/>
					<assign name="language" expr="'Fukienes (Chinese)"/><else/>
					<assign name="promptFilename" expr="'ChineseFukienese'"/>
				<else />
					<throw event="nomatch"/> 
	            </if>
			</filled>
			
            <noinput>
				<prompt bargein="false">
					<audio src="prompts/no_input1.wav">
						Sorry, I did not get that.
					</audio>
				</prompt>
				<assign name="chineseLanguageFailures" expr="chineseLanguageFailures + 1"/>
				<if cond="chineseLanguageFailures >= 2">
					<prompt bargein="false">
                        <audio src="prompts/agent_connect1.wav">
                            One moment while I connect you with an agent who can assist you.
                        </audio>
                    </prompt>
                    <assign name="language" expr="''"/>
                    <exit namelist="language accesscode deptcode dtmfpin"/>
				</if>
				<clear namelist="chineseLanguage"/>
				<reprompt/>
			</noinput>
			<nomatch>
				<prompt bargein="false">
					<audio src="prompts/no_match1.wav">
						Sorry, I did not understand.
					</audio>
				</prompt>
				<assign name="chineseLanguageFailures" expr="chineseLanguageFailures + 1"/>
				<if cond="chineseLanguageFailures >= 2">
					<prompt bargein="false">
						<audio src="prompts/agent_connect1.wav">
						 	One moment while I connect you with an agent who can assist you.
						</audio>
					</prompt>
                    <assign name="language" expr="''"/>
					<exit namelist="language accesscode deptcode dtmfpin"/>					
				</if>
				<clear namelist="chineseLanguage"/>
				<reprompt/>
			</nomatch>
		</field>
		
		<field name="deptcode" type="digits" expr="<%= deptcode %>">
			<property name="sensitivity" value="0.17"/>
			<%
			if (null == customDeptPromptURL || ("").equals(customDeptPromptURL) || ("undefined").equals(customDeptPromptURL)) {
			%>
				<prompt timeout="0s" bargein="false">
					<audio expr="customDeptPromptURL"></audio>
                </prompt>
			<% } else { %>
				<prompt>
					<audio src="prompts/get_dept_code.wav">
						Please enter or say your department code.
					</audio>
				</prompt>
			<% } %>
			<noinput>
				<log>
					<value expr="'******** Inside of welcome.jsp#deptcode maxnoinput got: accesscode='+accesscode+' language='+language+' deptcode='+deptcode" />
				</log>
				<prompt bargein="false">
					<audio src="prompts/no_input1.wav">
                        Sorry, I did not get that.
                    </audio>
				</prompt>
				<assign name="failures	" expr="failures + 1"/>
				<if cond="failures >= 2">
					<prompt bargein="false">
						<audio src="prompts/agent_connect2.wav">
                            Please hold while I connect you with an agent.
                        </audio>
					</prompt>
					<exit namelist="language accesscode deptcode dtmfpin"/>
				</if>
			</noinput>
			<nomatch>
				<prompt bargein="false">
					<audio src="prompts/no_match1.wav">
                        Sorry, I did not understand.
                    </audio>
				</prompt>
				<assign name="failures" expr="failures + 1"/>
				<if cond="failures >= 2">
					<log>
						<value expr="'******** Inside of welcome.jsp#deptcode maxnomatch got: accesscode='+accesscode+' language='+language+' deptcode='+deptcode" />
					</log>
					<prompt bargein="false">
                        <audio src="prompts/agent_connect2.wav">
                            Please hold while I connect you with an agent.
                        </audio>
					</prompt>
					<exit namelist="language accesscode deptcode dtmfpin"/>
				<else/>
					<reprompt/>
				</if>
			</nomatch>
			<filled>
				<log>
					<value expr="'******** Inside of welcome.jsp#deptcode filled got: accesscode='+accesscode+' language='+language+' deptcode='+deptcode" />
				</log>
				<goto expr="'check_deptcode.jsp?failures=' + failures + '&amp;accesscode=' + accesscode + '&amp;language=' + language + '&amp;dtmfpin=' + dtmfpin + '&amp;deptcode=' + deptcode"/>
			</filled>
		</field>

		<field name="dtmfpin" type="digits" expr="<%= dtmfpin %>">
			<property name="sensitivity" value="0.17"/>
			<prompt>
				<audio src="prompts/get_call_pin.wav">
					What is your numeric call I D ?
				</audio>
			</prompt>
			<noinput>
				<log>
					<value expr="'******** Inside of welcome.jsp#dtmfpin maxnoinput got: dtmfpin='+dtmfpin+' language='+language+' deptcode='+deptcode" />
				</log>
				<prompt bargein="false">
					<audio src="prompts/no_input1.wav">
                        Sorry, I did not get that.
                    </audio>
				</prompt>
				<assign name="failures	" expr="failures + 1"/>
				<if cond="failures >= 2">
					<prompt bargein="false">
						<audio src="prompts/agent_connect2.wav">
                            Please hold while I connect you with an agent.
                        </audio>
					</prompt>
					<exit namelist="language accesscode deptcode dtmfpin"/>
				</if>
			</noinput>
			<nomatch>
				<prompt bargein="false">
					<audio src="prompts/no_match1.wav">
                        Sorry, I did not understand.
                    </audio>
				</prompt>
				<assign name="failures" expr="failures + 1"/>
				<if cond="failures >= 2">
					<log>
						<value expr="'******** Inside of welcome.jsp#dtmfpin maxnomatch got: accesscode='+accesscode+' language='+language+' deptcode='+deptcode" />
					</log>
					<prompt bargein="false">
                        <audio src="prompts/agent_connect2.wav">
                            Please hold while I connect you with an agent.
                        </audio>
					</prompt>
					<exit namelist="language accesscode deptcode dtmfpin"/>
				<else/>
					<reprompt/>
				</if>
			</nomatch>
			<filled>
				<log>
					<value expr="'******** Inside of welcome.jsp#dtmfpin filled got: accesscode='+accesscode+' language='+language+' deptcode='+deptcode+' dtmfpin='+dtmfpin" />
				</log>
			</filled>
		</field>

		<block>
			<if cond="accesscode=='undefined'||language=='undefined'">
				<prompt>
					<audio src="prompts/interpreter_connect1.wav">
                        Please hold while I connect you with an Interpreter.
                     </audio>
				</prompt>
			<else/>
				<assign name="accessCodeArray" expr="accesscode.split('')" />
				<prompt>
					<audio src="prompts/interpreter_connect2-1.wav">
                        Please hold while I connect you with a
                     </audio>
				</prompt>
				<prompt>
					<audio expr="'prompts/languages/' + promptFilename + '.wav'">
						<value expr="language"/>
					</audio>
				</prompt>
                <prompt>
					    <audio src="prompts/interpreter_connect2-2.wav">
							Interpreter
						</audio>
				</prompt>
			</if>
			<exit namelist="language accesscode deptcode dtmfpin"/>
		</block>
		
	</form>
	
	<form id="checkcode">
		<log expr="'**** Inside of welcome.jsp#checkcode ****'"/>
		<block cond="true == <%= codevalid %>">
			<goto next="#getcallinfo"/>
		</block>
		<block cond="false == <%= codevalid %>">
			<if cond="failures >= 2">
					<log>
						<value expr="'******** Inside of welcome.jsp#checkcode got: accesscode='+accesscode+' language='+language+' deptcode='+deptcode" />
					</log>
				<prompt>
					<audio src="prompts/agent_connect2.wav">
                        Please hold while I connect you with an agent.
                    </audio>
                </prompt>
				<!-- assign name="accesscode" expr="undefined"/ -->
				<exit namelist="accesscode language deptcode dtmfpin"/>
			<else/>
				<assign name="accesscode" expr="undefined"/>
				<goto next="#getcallinfo"/>
			</if>
		</block>
	</form>
	
	<form id="deptcheckcode">
		<log expr="'**** Inside of welcome.jsp#deptcheckcode ****'"/>
		<block cond="true == <%= codevalid %>">
			<goto next="#getcallinfo"/>
		</block>
		<block cond="false == <%= codevalid %>">
			<if cond="failures >= 2">
					<log>
						<value expr="'******** Inside of welcome.jsp#deptcheckcode got: accesscode='+accesscode+' language='+language+' deptcode='+deptcode" />
					</log>
				<prompt>
					<audio src="prompts/agent_connect2.wav">
                        Please hold while I connect you with an agent.
                    </audio>
                </prompt>
				<assign name="accesscode" expr="<%= accesscode %>"/>
				<assign name="language" expr="<%= language %>"/>
				<exit namelist="accesscode language deptcode dtmfpin"/>
			<else/>
				<goto next="#getcallinfo"/>
			</if>
		</block>
	</form>
	
	<form id ="exit">
		<block>
			<prompt bargein="false">
				<audio src="prompts/agent_connect2.wav">
					Please hold while I connect you with an agent.
				</audio>
			</prompt>
			<exit namelist="accesscode language deptcode dtmfpin" />
		</block>
	</form>
</vxml>