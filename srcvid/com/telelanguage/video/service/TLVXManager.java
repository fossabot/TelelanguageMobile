package com.telelanguage.video.service;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.RandomBasedGenerator;
import com.telelanguage.video.util.PropertyLoader;
import com.telelanguage.videoapi.TLVXCustomerAPIClient;
import com.telelanguage.videoapi.VideoAPIServiceServlet;

public class TLVXManager implements Filter {
	
    private static Properties properties = PropertyLoader.loadProperties("video.properties");
    
    public static VideoCustomerManager videoCustomerManager = new VideoCustomerManager();
	public static TLVXCustomerAPIClient tlvxClient = new TLVXCustomerAPIClient(properties.getProperty("tlvxApiUrl"));
	public static String thisServerApiUrl = properties.getProperty("tlvxApiUrl");
	public static VideoAPIServiceImpl videoApiService = new VideoAPIServiceImpl();
	public static BalancerService balancerService = new BalancerService(properties.getProperty("routingApiUrl"));
	private static RandomBasedGenerator uuidGenerator = Generators.randomBasedGenerator();
	
	static {
		VideoAPIServiceServlet.setServerInstance(videoApiService);
	}
	
	public static String grabNewUuid() {
		return uuidGenerator.generate().toString();
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
    		((HttpServletResponse) response).setHeader("Access-Control-Allow-Origin",((HttpServletRequest) request).getHeader("origin"));
    		((HttpServletResponse) response).setHeader("Access-Control-Allow-Credentials","true");
        	((HttpServletResponse)response).setHeader("Access-Control-Allow-Headers","X-GWT-Module-Base, X-GWT-Permutation, Content-Type");

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