package com.telelanguage.tlvx.dao;

import org.apache.log4j.Logger;

import com.telelanguage.tlvx.model.User;
import com.telelanguage.tlvx.model.VriLinks;
import com.telelanguage.tlvx.model.VriUser;
import com.telelanguage.tlvx.service.TLVXManager;

public class UserDAO
{
    protected final Logger LOG = Logger.getLogger(UserDAO.class);
    public User findUser(String emailAddress)
    {
        User user = (User) TLVXManager.getSession()
        		.createQuery("from User u where u.email = :email")
        		.setParameter("email", emailAddress)
        		.uniqueResult();
        if (null == user)
        {
            if (LOG.isInfoEnabled()) LOG.info("user does not exist -> emailaddress = " + emailAddress);
        }
        return user;
    }
    
    public VriUser findVriUser(String email) {
    	VriUser user = (VriUser) TLVXManager.getSession()
        		.createQuery("from VriUser u where u.email = :email and deleted = false and active = true")
        		.setParameter("email", email)
        		.uniqueResult();
        if (null == user)
        {
            if (LOG.isInfoEnabled()) LOG.info("vriuser does not exist or is disabled -> email = " + email);
        }
        return user;
    }

	public void save(VriUser vriUser) {
		TLVXManager.getSession().saveOrUpdate(vriUser);
		TLVXManager.commit();
	}

	public VriUser findVriUserByToken(String loginToken) {
    	VriUser user = (VriUser) TLVXManager.getSession()
        		.createQuery("from VriUser u where u.login_token = :loginToken and deleted = false and active = true")
        		.setParameter("loginToken", loginToken)
        		.uniqueResult();
        if (null == user)
        {
            if (LOG.isInfoEnabled()) LOG.info("vriuser does not exist or is disabled -> token = " + loginToken);
        }
        return user;
	}

	public VriLinks getVriLinks() {
    	VriLinks links = (VriLinks) TLVXManager.getSession()
        		.createQuery("from VriLinks")
        		.setMaxResults(1)
        		.uniqueResult();
		return links;
	}
}
