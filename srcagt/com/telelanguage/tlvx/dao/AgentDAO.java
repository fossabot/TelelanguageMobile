package com.telelanguage.tlvx.dao;

import java.util.Date;
import java.util.List;

import com.telelanguage.tlvx.model.Agent;
import com.telelanguage.tlvx.model.User;
import com.telelanguage.tlvx.service.TLVXManager;

/**
 * AgentDAO
 */
public class AgentDAO
{
    public Agent findByUser(User user)
    {
        return (Agent) TLVXManager.getSession()
        		.createQuery("from Agent a where a.user = :user")
        		.setParameter("user", user)
        		.uniqueResult();
    }

    public Agent findBySipUri(String sipUri)
    {
        return (Agent) TLVXManager.getSession()
        		.createQuery("FROM Agent a WHERE a.sipUri = :sipUri")
        		.setParameter("sipUri", sipUri)
        		.uniqueResult();
    }

    /**
     * find the available agent (not on a call) where they're online (logged in and available to take a call)
     * ordered by the agent that has been idle the longest
     */
    @SuppressWarnings("unchecked")
    public List<Agent> findOnlineAndAvailableAgentsLongestIdleFirst()
    {
        return TLVXManager.getSession()
        		.createQuery("FROM Agent a WHERE a.online = true AND a.available = true AND a.busy = false ORDER BY a.lastAttemptDate ASC")
        		.list();
    }
    
    @SuppressWarnings("unchecked")
    public List<Agent> findOnlineAgentsLongestIdleFirst()
    {
        return TLVXManager.getSession()
        		.createQuery("FROM Agent a WHERE a.online = true ORDER BY a.lastAttemptDate ASC")
        		.list();
    }
    
    @SuppressWarnings("unchecked")
    public int findAvailableAgentsCount(Agent exclude)
    {
        Long count = null;
        
        if (exclude != null)
        {
        	count = (Long) TLVXManager.getSession()
            		.createQuery("select count(*) FROM Agent a WHERE a.online = true and a.available = true and a.busy = false and a.agentId NOT EQUAL :exclude")
            		.setParameter("exclude", exclude.getId())
            		.uniqueResult();
        }
        else
        {
        	count = (Long) TLVXManager.getSession()
            		.createQuery("select count(*) FROM Agent a WHERE a.online = true and a.available = true and a.busy = false")
            		.uniqueResult();
        }
        return (count == null) ? 0 : count.intValue();
    }
    
    public int findOnlineAgentsCount()
    {
    	
        Long count = (Long) TLVXManager.getSession()
        		.createQuery("select count(*) FROM Agent a WHERE a.online = true and a.available = true")
        		.uniqueResult();
        return (count == null) ? 0 : count.intValue();
    }
    
    public void updateAgentLastCall(Long agentId)
    {
    	Agent agent = (Agent) TLVXManager.getSession()
        		.createQuery("FROM Agent a WHERE a.id = :agentId")
        		.setParameter("agentId", agentId)
        		.uniqueResult();
    	if (agent != null)
    	{
        	agent.setLastCallDate(new Date());
        	save(agent);
    	}
    }

	public Agent findById(Long agentId) {
		if (agentId == null) return null;
		return (Agent) TLVXManager.getSession().get(Agent.class, agentId);
	}

	public Agent save(Agent agent) {
		TLVXManager.getSession().saveOrUpdate(agent);
		TLVXManager.commit();
		return agent;
	}
}
