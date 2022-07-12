package com.telelanguage.tlvx.service;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.RandomBasedGenerator;
import com.telelanguage.api.TLVXAPIServiceServlet;
import com.telelanguage.tlvx.dao.AgentDAO;
import com.telelanguage.tlvx.dao.CallDAO;
import com.telelanguage.tlvx.dao.CallDetailRecordDAO;
import com.telelanguage.tlvx.dao.CallEventDAO;
import com.telelanguage.tlvx.dao.CallStatDAO;
import com.telelanguage.tlvx.dao.CustomerDAO;
import com.telelanguage.tlvx.dao.CustomerDNISDAO;
import com.telelanguage.tlvx.dao.CustomerDepartmentDAO;
import com.telelanguage.tlvx.dao.CustomerOptionContentDAO;
import com.telelanguage.tlvx.dao.CustomerOptionDAO;
import com.telelanguage.tlvx.dao.CustomerOptionDataDAO;
import com.telelanguage.tlvx.dao.EventDAO;
import com.telelanguage.tlvx.dao.InterpreterBlackListDAO;
import com.telelanguage.tlvx.dao.InterpreterDAO;
import com.telelanguage.tlvx.dao.InterpreterStatusDAO;
import com.telelanguage.tlvx.dao.LanguageDAO;
import com.telelanguage.tlvx.dao.SessionHistoryDAO;
import com.telelanguage.tlvx.dao.SubscribedClientsDIDDAO;
import com.telelanguage.tlvx.dao.UserDAO;
import com.telelanguage.tlvx.ivr.CallSessionManager;
import com.telelanguage.tlvx.ivr.TLVX;
import com.telelanguage.tlvx.util.PropertyLoader;
import com.telelanguage.videoapi.TLVXCustomerAPIServiceServlet;

public class TLVXManager implements Filter {
	
	private static Configuration hibernateConfiguration;
	private static SessionFactory sessionFactory;
	private static ThreadLocal<Session> hibernateSessions = new ThreadLocal<Session>();
	private static ThreadLocal<HttpSession> servletSessions = new ThreadLocal<HttpSession>();
    private static Properties properties = PropertyLoader.loadProperties("tlvx.properties");
    private static RandomBasedGenerator uuidGenerator = Generators.randomBasedGenerator();
	
	public static AgentManager agentManager = new AgentManager();
	public static InterpreterManager interpreterManager = new InterpreterManager();
	public static CustomerUserManager customerUserManager = new CustomerUserManager();
	public static InterpreterStatusDAO interpreterStatusDAO = new InterpreterStatusDAO();
	public static CustomerDNISDAO customerDNISDAO = new CustomerDNISDAO();
	public static CustomerDAO customerDAO = new CustomerDAO();
	public static CustomerOptionDAO customerOptionDAO = new CustomerOptionDAO();
	public static CustomerOptionDataDAO customerOptionDataDAO = new CustomerOptionDataDAO();
	public static CustomerOptionContentDAO customerOptionContentDAO = new CustomerOptionContentDAO();
	public static CustomerDepartmentDAO customerDepartmentDAO = new CustomerDepartmentDAO();
	public static CallDAO callDAO = new CallDAO();
	public static CallEventDAO callEventDAO = new CallEventDAO();
	public static AgentDAO agentDAO = new AgentDAO();
	public static InterpreterDAO interpreterDAO = new InterpreterDAO();
	public static InterpreterBlackListDAO interpreterBlackListDAO = new InterpreterBlackListDAO();
	public static SubscribedClientsDIDDAO subscribedClientsDIDDAO = new SubscribedClientsDIDDAO();
	public static LanguageDAO languageDAO = new LanguageDAO();
	public static CallDetailRecordDAO callDetailRecordDAO = new CallDetailRecordDAO();
	public static SessionHistoryDAO sessionHistoryDAO = new SessionHistoryDAO();
	public static UserDAO userDAO = new UserDAO();
	public static CallSessionManager callSessionManager = new CallSessionManager();
	public static LanguageManager languageManager = new LanguageManager();
	public static CompanyManager companyManager = new CompanyManager();
	public static TLVX ccxmlManager;
	public static EventDAO eventDAO = new EventDAO();
	public static CallStatDAO callStatDAO = new CallStatDAO();
	public static TLVXAPIServiceImpl apiService = new TLVXAPIServiceImpl();
	public static TLVXCustomerAPIServiceImpl customerService = new TLVXCustomerAPIServiceImpl();
	
	
	static {
		TLVXAPIServiceServlet.setServerInstance(apiService);
		TLVXCustomerAPIServiceServlet.setServerInstance(customerService);
	}
	
	public static Session getSession() {
		if (hibernateSessions.get() == null) {
			Session session = sessionFactory.openSession();
			hibernateSessions.set(session);
			session.beginTransaction();
			return session;
		} else {
			Session session = hibernateSessions.get();
			if (!session.getTransaction().isActive())
				session.beginTransaction();
			return session;
		}
	}
	
	public static void cleanupSession() {
		Session currentSession = hibernateSessions.get();
		if (currentSession != null) {
			if (currentSession.getTransaction().isActive()) {
				//System.out.println("transaction active!");
				try {
					currentSession.getTransaction().commit();
				} catch (Exception e) {
					try {
						currentSession.getTransaction().rollback();
					} catch (Exception ee) {
						ee.printStackTrace();
					}
				}
			}
			try {
				currentSession.close();
			} catch (Exception eee) {
				eee.printStackTrace();
			}
			hibernateSessions.remove();
		}
	}
	
	public static void commit() {
		if (hibernateSessions.get() != null) {
			Session session = hibernateSessions.get();
			if (!session.getTransaction().wasCommitted()) {
				session.getTransaction().commit();
			}
		}
	}
    
	public static void setTlvxIvr(TLVX tlvxIvr) {
		TLVXManager.ccxmlManager = tlvxIvr;
	}

	public static Properties getProperties() {
		return properties;
	}

	public static void contextDestoryed() {
	}
	
	public static String grabNewUuid() {
		return uuidGenerator.generate().toString().replaceAll("-", "");
	}

	@Override
	public void destroy() {
		agentManager.destroy();
		sessionFactory.close();
//		try {
//			FileOutputStream fos = new FileOutputStream(properties.getProperty("serializedSessionsOnRestart"));
//			ObjectOutputStream oos = new ObjectOutputStream(fos);
//	        oos.writeObject(callSessionManager.sessions);
//	        oos.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
        	servletSessions.set(((HttpServletRequest)request).getSession());
            chain.doFilter(request, response);
        } finally {
            try {
                cleanupSession();
                servletSessions.remove();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		hibernateConfiguration = new Configuration()
			.addProperties(properties)
			.addAnnotatedClass(com.telelanguage.tlvx.model.Agent.class)
            .addAnnotatedClass(com.telelanguage.tlvx.model.Call.class)
            .addAnnotatedClass(com.telelanguage.tlvx.model.CallDetailRecord.class)                
            .addAnnotatedClass(com.telelanguage.tlvx.model.CallEvent.class)                
            .addAnnotatedClass(com.telelanguage.tlvx.model.CallStat.class)
            .addAnnotatedClass(com.telelanguage.tlvx.model.CredentialsRequest.class)
            .addAnnotatedClass(com.telelanguage.tlvx.model.Event.class)
            .addAnnotatedClass(com.telelanguage.tlvx.model.Dnis.class)
            .addAnnotatedClass(com.telelanguage.tlvx.model.DnisPrompt.class)
            .addAnnotatedClass(com.telelanguage.tlvx.model.InterpreterActivity.class)
            .addAnnotatedClass(com.telelanguage.tlvx.model.InterpreterLanguageList.class)
            .addAnnotatedClass(com.telelanguage.tlvx.model.InterpreterStatus.class)
            .addAnnotatedClass(com.telelanguage.tlvx.model.Prompt.class)
            .addAnnotatedClass(com.telelanguage.tlvx.model.Customer.class)
            .addAnnotatedClass(com.telelanguage.tlvx.model.CustomerDepartment.class)
            .addAnnotatedClass(com.telelanguage.tlvx.model.CustomerDNIS.class)
            .addAnnotatedClass(com.telelanguage.tlvx.model.CustomerOption.class)
            .addAnnotatedClass(com.telelanguage.tlvx.model.CustomerOptionContent.class)
            .addAnnotatedClass(com.telelanguage.tlvx.model.CustomerOptionData.class)
            .addAnnotatedClass(com.telelanguage.tlvx.model.Language.class)
            .addAnnotatedClass(com.telelanguage.tlvx.model.Interpreter.class)
            .addAnnotatedClass(com.telelanguage.tlvx.model.InterpreterBlackList.class)
            .addAnnotatedClass(com.telelanguage.tlvx.model.InterpreterMissedCall.class)
            .addAnnotatedClass(com.telelanguage.tlvx.model.SessionHistory.class)
            .addAnnotatedClass(com.telelanguage.tlvx.model.ScheduleInterpreterRequest.class)
            .addAnnotatedClass(com.telelanguage.tlvx.model.SubscribedClientsDID.class)
            .addAnnotatedClass(com.telelanguage.tlvx.model.ThirdPartyActivity.class)
            .addAnnotatedClass(com.telelanguage.tlvx.model.User.class)
        	.addAnnotatedClass(com.telelanguage.tlvx.model.VriLinks.class)
    		.addAnnotatedClass(com.telelanguage.tlvx.model.VriUser.class);
		sessionFactory = hibernateConfiguration.buildSessionFactory();
//		try {
//			FileInputStream fis = new FileInputStream(properties.getProperty("serializedSessionsOnRestart"));
//	        ObjectInputStream ois = new ObjectInputStream(fis);
//	        callSessionManager.sessions = (Map<String, CallSession>) ois.readObject();
//	        ois.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		agentManager.init();
	}
}