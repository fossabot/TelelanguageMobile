package com.telelanguage.tlvx.ivr;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.telelanguage.tlvx.model.Agent;
import com.telelanguage.tlvx.service.AgentManager;
import com.telelanguage.tlvx.service.TLVXManager;

/**
 * OutboundCallSession
 */
public class OutboundCallSession implements Serializable
{
	private static final long serialVersionUID = -6982530963677539750L;

	private static final Logger LOG = Logger.getLogger(OutboundCallSession.class);
    
    private String ccxmlServer;
    private String sessionId;
    private Agent agent;
    private boolean dialingAgent;
    private boolean dialingRemote;
    private String destination;
    private Connection agentConnection;
    private Connection remoteConnection;
    private Conference conference;
    private boolean destroyed;
    
    public OutboundCallSession(String ccxmlServer, String sessionId, Long agentId, String destination)
    {
        this.ccxmlServer = ccxmlServer;
        this.sessionId = sessionId;
        this.destination = destination;
        
        agent = TLVXManager.agentDAO.findById(agentId);
    }
    
    public String getId()
    {
        return sessionId;
    }
    
    public void startSession()
    {
        Map parameters = new HashMap();
        parameters.put("confname", "CONF-" +sessionId);
        
        try
        {
        	TLVXManager.ccxmlManager.createConference(parameters);
        }
        catch (Exception e)
        {
            LOG.warn("Exception caught trying to create conference", e);
        }
    }
    
    private Map buildSessionDataObject()
    {
        Map sessionDataObject = new HashMap();
        sessionDataObject.put("sessionid", sessionId);
        sessionDataObject.put("ccxml_server", ccxmlServer);

        return sessionDataObject;
    }
    
    public void onConnectionProgressing(Map request)
    {
        if (LOG.isDebugEnabled()) LOG.debug("onConnectionProgressing received");
        
        if (true == dialingAgent)
        {
            String connectionId = (String) request.get("connectionid");
            String origination = (String) request.get("connectionLocal");
            String destination = (String) request.get("connectionRemote");
            
            agentConnection = new Connection(connectionId, destination, origination);
            agentConnection.setState(Connection.ConnectionState.CONNECTING);
        }
        else if (true == dialingRemote)
        {
            String connectionId = (String) request.get("connectionid");
            String origination = (String) request.get("connectionLocal");
            String destination = (String) request.get("connectionRemote");
            
            remoteConnection = new Connection(connectionId, destination, origination);
            remoteConnection.setState(Connection.ConnectionState.CONNECTING);
        }        
    }
    
    public void onConnectionConnected(Map request)
    {
        if (LOG.isDebugEnabled()) LOG.debug("onConnectionConnected received");

        String connectionId = (String) request.get("connectionid");
        
        if (null != agentConnection && connectionId.equals(agentConnection.getConnectionId()))
        {
            agentConnection.setState(Connection.ConnectionState.CONNECTED);
            dialingAgent = false;

            Map parameters = buildSessionDataObject();
            
            String convertedDestination = destination.replace(" ", "").replace("(", "").replace(")","").replace("-", "");
            
            parameters.put("dest", "sip:" + convertedDestination + "@" + TLVXManager.callSessionManager.getProxyAddress());

            try
            {
                dialingRemote = true;
                parameters.put("timeout", "30000ms");
                parameters.put("remoteAddress", ccxmlServer);
                TLVXManager.ccxmlManager.createCall(parameters);
            }
            catch (Exception e)
            {
                LOG.warn("Exception caught trying to create call", e);
            }           
        }
        else if (null != remoteConnection && connectionId.equals(remoteConnection.getConnectionId()))
        {
            remoteConnection.setState(Connection.ConnectionState.CONNECTED);
            dialingRemote = false;
            
            conferenceConnection(agentConnection, "full", null);
            conferenceConnection(remoteConnection, "full", null);
        }
    }

    private void conferenceConnection(Connection connection, String duplex, String termDigits)
    {
        if (false == connection.isConferenced())
        {
            Map parameters = new HashMap();
            parameters.put("id1", connection.getConnectionId());
            parameters.put("id2", conference.getConferenceId());
            parameters.put("duplex", duplex);
            
            
            if (null != termDigits)
            {
                parameters.put("termdigits", termDigits);
            }
            
            try
            {
            	TLVXManager.ccxmlManager.join(parameters);
            }
            catch (Exception e)
            {
                LOG.warn("Exception caught while trying to join", e);
            }
            
            connection.setConferenced(true);
        }
        else
        {
            if (LOG.isDebugEnabled()) LOG.debug("Ignoring conference connection request, already conferenced");
        }
    }
    
    public void onConnectionDisconnected(Map request)
    {
        if (LOG.isDebugEnabled()) LOG.debug("onConnectionDisconnected received");       
        
        String connectionId = (String) request.get("connectionid");
        
        if (null != remoteConnection && connectionId.equals(remoteConnection.getConnectionId()))
        {
            processRemoteDisconnect();   
        }
        else if (null != agentConnection && connectionId.equals(agentConnection.getConnectionId()))
        {
            processAgentDisconnect();
        }        
        
        if (false == isAnyConnected())
        {
            cleanUpSession();
        }
    }
    
    public void onConnectionFailed(Map request)
    {
        String reason = (String) request.get("reason");
        if (LOG.isDebugEnabled()) LOG.debug("onConnectionFailed received for "+this+", reason = " + reason);
        
        String connectionId = (String) request.get("connectionid");
        
        if (agentConnection != null && connectionId.equals(agentConnection.getConnectionId()))
        {
            processAgentDisconnect();
        }
        else if (remoteConnection != null && connectionId.equals(remoteConnection.getConnectionId()))
        {
            processRemoteDisconnect();
        }
        else if (true == dialingAgent || true == dialingRemote)
        {
            onConnectionProgressing(request);
            onConnectionFailed(request);
        }
        else
        {
            LOG.warn("Unable to handle onConnectionFailed request, ignoring");
        }
        
        if (false == isAnyConnected())
        {
            cleanUpSession();
        }        
    }
    
    private void processAgentDisconnect()
    {
        if (LOG.isDebugEnabled()) LOG.debug("processAgentDisconnect received");
        
        dialingAgent = false;
        agentConnection.setState(Connection.ConnectionState.DISCONNECTED);
        
        if (remoteConnection != null && true == isConnected(remoteConnection))
        {
            hangupConnection(remoteConnection);
        }
    }
    
    private void processRemoteDisconnect()
    {
        if (LOG.isDebugEnabled()) LOG.debug("processRemoteDisconnect received");
        
        dialingRemote = false;
        remoteConnection.setState(Connection.ConnectionState.DISCONNECTED);
        
        if (agentConnection != null && true == isConnected(agentConnection))
        {
            hangupConnection(agentConnection);
        }
    }
    
    private void dispatchAgentMessage(Map data, String type)
    {
        if (null != agent)
        {
        	TLVXManager.agentManager.dispatchAgentMessage(agent, data, type);
        }
        else
        {
            LOG.warn("dispatchAgentMessage called with data="+data+", type="+type+" -- however, no agent is connected for "+this);
        }
    }
    
    public void onConferenceCreated(Map request)
    {
        if (LOG.isDebugEnabled()) LOG.debug("onConferenceCreated received for "+this);
        
        conference = new Conference((String) request.get("conferenceid"));
        
        Map data = new HashMap();
        data.put("destination", destination);
        dispatchAgentMessage(data, AgentManager.TL_CALL_OUTBOUND_START);
        
        Map parameters = buildSessionDataObject();
        parameters.put("dest", agent.getSipUri());
        
        try
        {
            dialingAgent = true;
            parameters.put("timeout", "15000ms");
            parameters.put("remoteAddress", ccxmlServer);
            parameters.put("connectionid", "agent");
            TLVXManager.ccxmlManager.createCall(parameters);
        }
        catch (Exception e)
        {
            LOG.warn("Exception caught trying to create call", e);
        }
    }
    
    public void onConferenceDestroyed(Map request)
    {
        if (LOG.isDebugEnabled()) LOG.debug("onConferenceDestroyed received for "+this);
    }
    
    public void onConnectionWrongState(Map request)
    {
        if (LOG.isDebugEnabled()) LOG.debug("onConnectionWrongState received for "+this);
    }
    
    public void onConferenceJoined(Map request)
    {
        if (LOG.isDebugEnabled()) LOG.debug("onConferenceJoined received for "+this);
    }

    public void onConferenceUnjoined(Map request)
    {
        if (LOG.isDebugEnabled()) LOG.debug("onConferenceUnjoined received for "+this);
    }
    
    public void onDialogStarted(Map request)
    {
        
    }

    public void onDialogExit(Map request)
    {
        
    }
    
    private boolean isConnected(Connection connection)
    {
        return (null != connection && connection.getState() != Connection.ConnectionState.DISCONNECTED);
    }
    
    private boolean isAnyConnected()
    {
        boolean connectionConnected = (isConnected(agentConnection) || isConnected(remoteConnection));
        
        boolean dialing = (dialingAgent || dialingRemote);
        
        return (connectionConnected || dialing);
    }
    
    private void hangupConnection(Connection connection)
    {
        if (true == destroyed)
        {
            return;
        }

        Map parameters = buildSessionDataObject();
        parameters.put("connectionid", connection.getConnectionId());
        
        try
        {
        	TLVXManager.ccxmlManager.disconnect(parameters);
            connection.setState(Connection.ConnectionState.DISCONNECTING);
        }
        catch (Exception e)
        {
            LOG.warn("Exception caught while trying to disconnect call", e);                    
        }       
    }    
    
    private void cleanUpSession()
    {
        if (LOG.isDebugEnabled()) LOG.debug("cleanUpSession, destroyed = " + destroyed);
                
        if (false == destroyed)
        {
            Map data = new HashMap();
            dispatchAgentMessage(data, AgentManager.TL_CALL_OUTBOUND_STOP);
            
            TLVXManager.agentManager.agentCallDisconnected(agent, null);
            
            try
            {
                data = buildSessionDataObject();
                data.put("ccxmlsession", sessionId);
                TLVXManager.ccxmlManager.destroySession(data);
            }
            catch (Exception e)
            {
                LOG.warn("Exception caught trying to destroy session", e);
            }
            
            TLVXManager.callSessionManager.endOutboundCallSession(this);
            
            destroyed = true;
        }
    }
}
