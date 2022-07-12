package com.telelanguage.tlvx.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.telelanguage.tlvx.model.Customer;
import com.telelanguage.tlvx.model.CustomerDepartment;
import com.telelanguage.tlvx.model.CustomerOptionContent;
import com.telelanguage.tlvx.model.Department;
import com.telelanguage.tlvx.model.QuestionItem;

/**
 * CompanyManager
 */
public class CompanyManager
{
    private static final Logger LOG = Logger.getLogger(CompanyManager.class);
    
    public boolean checkAccessCode(String code, String subscription)
    {
        Customer customer = TLVXManager.customerDAO.findByCode(code);
        if (customer != null)
        {
        	if (customer.getSubscriptionCode() != null && customer.getSubscriptionCode().equals(subscription))
        	{
        		return true;
        	}
        }
    	return false;
    }
    
    public boolean shouldSendToAgentQueue(String code)
    {
        Customer customer = TLVXManager.customerDAO.findByCode(code);
        if (customer != null)
        {
        	return (customer.getSendToAgent() != null && customer.getSendToAgent());
        }
    	return false;
    }
    
    public boolean shouldAskDepartmentCode(String code)
    {
        Customer customer = TLVXManager.customerDAO.findByCode(code);
        if (customer != null)
        {
        	List<CustomerDepartment> departments = TLVXManager.customerDepartmentDAO.findCustomerDepartments(customer.getCustomerId());
        	if (departments.size() > 0)
        	{
        		return true;
        	}
        }
    	return false;
    }
    
    public boolean shouldAskDtmf(String code)
    {
    	Customer customer = TLVXManager.customerDAO.findByCode(code);
    	if (customer != null && customer.getCustomerDNIS() != null) {
    		return customer.getCustomerDNIS().isAskDtmfPin();
    	}
    	return false;
    }
    
    public boolean checkDepartmentCode(String deptCode, String accessCode)
    {
        LOG.debug("checkDepartmentCode, deptCode = " + deptCode + ", accessCode = " + accessCode);
        Customer customer = TLVXManager.customerDAO.findByCode(accessCode);
        if (customer != null)
        {
            List<CustomerDepartment> departments = TLVXManager.customerDepartmentDAO.findCustomerDepartments(customer.getCustomerId());
            for (CustomerDepartment department : departments)
            {
                if (department.getCode().equals(deptCode))
                {
                    return true;
                }
            }
        }
        return false;
    }

	public List<QuestionItem> getAdditionalQuestions(String callId, String customerId) {
		List<QuestionItem> additionalQuestions = new ArrayList<QuestionItem>();
        List<CustomerOptionContent> options = TLVXManager.customerOptionContentDAO.findByCustomerId(customerId);
        LOG.info("Options = "+options.toString());
        for (CustomerOptionContent option : options) {
        	QuestionItem item = new QuestionItem(option.getId(), option.getOptionLabel(), option.getOptionContent());
        	item.setValue(TLVXManager.customerOptionDataDAO.getValueForCustOptionCall(callId, option.getId()));
        	additionalQuestions.add(item);
        }
		return additionalQuestions;
	}

	public List<Department> getDepartments(String customerId) {
		List<CustomerDepartment> custDepts = TLVXManager.customerDepartmentDAO.findCustomerDepartments(customerId);
		List<Department> departments = new ArrayList<Department>();
		for(CustomerDepartment custDept : custDepts) {
			Department department = new Department();
			department.departmentName = ""+custDept.getCode()+" "+custDept.getName();
			department.departmentValue = custDept.getCode();
			departments.add(department);
		}
		return departments;
	}

	public boolean validateDepartmentCode(Customer customer, String departmentCode) {
		return TLVXManager.customerDepartmentDAO.isDeptCodeValidForCustomer(customer, departmentCode);
	}

}
