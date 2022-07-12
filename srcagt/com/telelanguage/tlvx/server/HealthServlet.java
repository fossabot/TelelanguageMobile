package com.telelanguage.tlvx.server;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.telelanguage.tlvx.service.TLVXManager;

public class HealthServlet extends HttpServlet {
	private static final long serialVersionUID = 115838615617510838L;
	OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
	static int mb = 1024*1024;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		boolean dbOk = TLVXManager.userDAO.findUser("justin@telelanguage.net") != null;
		TLVXManager.cleanupSession();
		Runtime runtime = Runtime.getRuntime();
		ServletOutputStream out = resp.getOutputStream();
		out.println("<html>");
		out.println("<head>");
		out.println("</head>");
		out.println("<body>");
		out.println("<table border=\"1\">");
		String status = dbOk /*&& os.getSystemLoadAverage()<2*/ ? "OK" : "FAIL" ;
		out.println("<tr><td>Status:</td><td>"+status+"</td></tr>");
		out.println("<tr><td>DB:</td><td>"+dbOk+"</td></tr>");
		out.println("<tr><td>Load:</td><td>"+os.getSystemLoadAverage()+"</td></tr>");
		out.println("<tr><td>Agents:</td><td>"+AgentServiceImpl.sessionIdToMessageList.size()+"</td></tr>");
		out.println("<tr><td>Threads:</td><td>"+Thread.activeCount()+"</td></tr>");
		out.println("<tr><td>Used RAM in MB:</td><td>"+((runtime.totalMemory() - runtime.freeMemory()) / mb)+"</td></tr>");
		out.println("<tr><td>Free RAM in MB:</td><td>"+(runtime.freeMemory() / mb)+"</td></tr>");
		out.println("</table>");
		out.println("</body>");
		out.println("</html>");
	}
}