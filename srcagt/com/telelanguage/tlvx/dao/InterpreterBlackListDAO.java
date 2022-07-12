package com.telelanguage.tlvx.dao;

import java.util.LinkedList;
import java.util.List;

import com.telelanguage.tlvx.model.Interpreter;
import com.telelanguage.tlvx.model.InterpreterBlackList;
import com.telelanguage.tlvx.service.TLVXManager;

public class InterpreterBlackListDAO 
{
    public List<Object[]> removeBlackListedInterpreters(String accessCode, List<Object[]> interpreters)
    {
    	if (accessCode == null)
    	{
    		return interpreters;
    	}

    	List<Object[]> results = new LinkedList<Object[]>();
    	@SuppressWarnings("unchecked")
		List<InterpreterBlackList> blackList = TLVXManager.getSession()
        		.createQuery("FROM InterpreterBlackList i WHERE i.accessCode = :accessCode")
        		.setParameter("accessCode",accessCode)
        		.list();

    	for (Object[] result : interpreters)
    	{
    		Interpreter interpreter = (Interpreter)result[0];
    		boolean found = false;
    		for (InterpreterBlackList bl : blackList)
    		{
    			if (interpreter.getInterpreterId().equals(bl.getInterpreterId()))
    			{
    				found = true;
    				break;
    			}
    		}
    		if (false == found)
    		{
    			results.add(result);
    		}
    	}
    	
    	return results;
    }
}
