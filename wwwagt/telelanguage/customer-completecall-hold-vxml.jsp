<?xml version="1.0" encoding="UTF-8"?>
<vxml xmlns="http://www.w3.org/2001/vxml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://www.w3.org/2001/vxml http://www.w3.org/TR/voicexml20/vxml.xsd" version="2.0">
    <var name="promptURL" expr="'<%= request.getParameter("promptURL") %>'"/>
    <form id="hold">
		<block>
        	<if cond="promptURL != ''">
        		<prompt timeout="0s" bargein="false">
					<audio expr="promptURL"></audio>
				</prompt>
			<else/>
				<prompt timeout="0s" bargein="false">
	                <audio src="prompts/customer-completecall-hold.wav"/>
	                <audio src="prompts/default-hold.wav">
	                    Please continue to hold the line. We will be with you shortly.
	                </audio>
				</prompt>
        	</if>
		</block>
        <field type="digits">
			<prompt timeout="0s" bargein="false">
			</prompt>
        	<noinput>
	            <goto next="#hold"/>
        	</noinput>
        	<nomatch>
	            <goto next="#hold"/>
        	</nomatch>
        	<filled>
	            <goto next="#hold"/>
        	</filled>
        </field>
    </form>
</vxml>
