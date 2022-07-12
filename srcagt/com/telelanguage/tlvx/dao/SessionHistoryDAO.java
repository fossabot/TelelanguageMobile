package com.telelanguage.tlvx.dao;

import java.util.Collection;

import com.telelanguage.tlvx.model.SessionHistory;
import com.telelanguage.tlvx.model.User;
import com.telelanguage.tlvx.service.TLVXManager;

/**
 * SessionHistoryDAO
 */
public class SessionHistoryDAO
{
    /**
     * find a currently active session, if available, for a user identified by uuid
     *
     * @param uuid user uuid
     * @return active session for user identified by uuid or <code>null</code> if no active session is found
     */
    public SessionHistory findActiveSessionForUUID(String uuid)
    {
        return (SessionHistory) TLVXManager.getSession()
        		.createQuery("FROM SessionHistory s WHERE s.uuid = :uuid AND s.endDate IS NULL")
        		.setParameter("uuid", uuid)
        		.uniqueResult();
    }

    /**
     * find all the user's current active session(s)
     *
     * @param user user to find active sessions for.
     * @return collection of user's active sessions.
     */
    public Collection<SessionHistory> findActiveSessionsForUser(User user)
    {
        return findActiveSessionsForUser(user, null);
    }

    /**
     * find the user's current active session(s) or null if not logged in for a specific serverId
     * or null for all
     *
     * @param user     user to find active sessions for.
     * @param serverId server ID to look for active sessions on or <code>null</code> for all servers.
     * @return currently active sessions on specified server or all servers
     */
    @SuppressWarnings("unchecked")
    public Collection<SessionHistory> findActiveSessionsForUser(User user, String serverId)
    {
        StringBuilder sqlBuilder = new StringBuilder("FROM SessionHistory s WHERE s.endDate IS NULL AND s.user = :user ");
        if (serverId != null)
        {
            sqlBuilder.append(" and s.serverid = '").append(serverId).append("'");
        }
        sqlBuilder.append(" order by s.startDate");

        return TLVXManager.getSession()
        		.createQuery(sqlBuilder.toString())
        		.setParameter("user", user)
        		.list();
    }

	public SessionHistory save(SessionHistory sessionHistory) {
		TLVXManager.getSession().saveOrUpdate(sessionHistory);
		TLVXManager.commit();
		return sessionHistory;
	}
}
