package com.telelanguage.interpreter.service;

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

import com.telelanguage.api.INTAPIServiceServlet;
import com.telelanguage.api.TLVXAPIClient;
import com.telelanguage.interpreter.util.PropertyLoader;

public class TLVXManager implements Filter {
	
	//private static ThreadLocal<HttpSession> servletSessions = new ThreadLocal<HttpSession>();
    private static Properties properties = PropertyLoader.loadProperties("interpreter.properties");
    
    public static InterpreterManager interpreterManager = new InterpreterManager();
	//public static InterpreterDAO interpreterDAO = new InterpreterDAO();
	public static TLVXAPIClient tlvxClient = new TLVXAPIClient(properties.getProperty("tlvxApiUrl"));
	public static String thisServerApiUrl = properties.getProperty("tlvxApiUrl");
	public static INTAPIServiceImpl intApiService = new INTAPIServiceImpl();
	
	static {
		INTAPIServiceServlet.setServerInstance(intApiService);
	}
	
	public static void cleanupSession() {
	}

	public static Properties getProperties() {
		return properties;
	}

	public static void contextDestoryed() {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
        	//servletSessions.set(((HttpServletRequest)request).getSession());
            chain.doFilter(request, response);
        } finally {
            try {
                cleanupSession();
                //servletSessions.remove();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
	}
}