<?xml version="1.0" encoding="UTF-8"?>
<vxml xmlns="http://www.w3.org/2001/vxml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://www.w3.org/2001/vxml http://www.w3.org/TR/voicexml20/vxml.xsd" version="2.0">
    <var name="promptURL" expr="'<%= request.getParameter("promptURL") %>'"/>
    <block id="instructions">
        <audio expr="promptURL"></audio>
        <exit/>
    </block>
</vxml>
