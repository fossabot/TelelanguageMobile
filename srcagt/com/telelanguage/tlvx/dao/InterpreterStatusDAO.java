package com.telelanguage.tlvx.dao;

import com.telelanguage.tlvx.model.Interpreter;
import com.telelanguage.tlvx.model.InterpreterStatus;
import com.telelanguage.tlvx.service.TLVXManager;

/**
 * InterpreterStatusDAO
 */
public class InterpreterStatusDAO
{
//    public boolean isBusy(Interpreter interpreter)
//    {
//        InterpreterStatus status = (InterpreterStatus) TLVXManager.getSession()
//        		.createQuery("FROM InterpreterStatus s WHERE s.interpreterId = :interpreterId")
//        		.setParameter("interpreterId", interpreter.getInterpreterId())
//        		.uniqueResult();
//        
//        if (status != null && status.getStatus() == InterpreterStatus.INTERPRETER_STATUS_BUSY)
//        {
//            return true;
//        }
//        
//        return false;
//    }
//    
//    public void setInterpreterStatus(Interpreter interpreter, int status)
//    {
//        InterpreterStatus interpreterStatus = (InterpreterStatus) TLVXManager.getSession()
//        		.createQuery("FROM InterpreterStatus s WHERE s.interpreterId = :interpreterId")
//        		.setParameter("interpreterId", interpreter.getInterpreterId())
//        		.uniqueResult();
//        
//        if (interpreterStatus == null)
//        {
//            interpreterStatus = new InterpreterStatus();
//            interpreterStatus.setInterpreterId(interpreter.getInterpreterId());
//            interpreterStatus.setStatus(status);
//            save(interpreterStatus);
//        }
//        else
//        {
//            interpreterStatus.setStatus(status);       
//            save(interpreterStatus);
//        }
//    }
//
//	private void save(InterpreterStatus interpreterStatus) {
//		TLVXManager.getSession().saveOrUpdate(interpreterStatus);
//		TLVXManager.commit();
//	}
}
