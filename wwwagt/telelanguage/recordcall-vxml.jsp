<?xml version="1.0" encoding="UTF-8" ?> 

    <!-- declaring the 'xmlns:voxeo' attribute is required --> 

    <vxml version="2.1" xmlns="http://www.w3.org/2001/vxml" 
      xmlns:voxeo="http://community.voxeo.com/xmlns/vxml"> 

    <form id="F1"> 
        <block> 
            <!-- the value of 100 will start recording --> 
            <voxeo:recordcall value="100" info="CONF-CallId-<%=request.getParameter("CallId") %>" /> 
            <prompt> 
                <break time="14400000"/><!-- wait up to 100 minutes --> 
            </prompt> 
        </block> 
    </form> 

  <catch event="connection.disconnect"> 
          <!-- do our submit here --> 
          <log expr="'***** SUBMITTING TO OUR FILE RETRIEVER *****'"/> 
  </catch> 
</vxml> 
