package com.icoa.voice.api.voxeo;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequests;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.utils.URIUtils;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.log4j.Logger;

import com.icoa.voice.api.Call;
import com.icoa.voice.api.VoiceApplication;
import com.icoa.voice.api.VoiceService;
import com.telelanguage.tlvx.ivr.CallSession;
import com.telelanguage.tlvx.service.TLVXManager;

public class VoxeoService extends HttpServlet implements VoiceService, HttpSessionListener {
	private static final Logger LOG = Logger.getLogger(CallSession.class);
	
	private static final long serialVersionUID = -3290749744395826697L;
	private VoiceApplication voiceApplication = null;
	private static ThreadLocal<HttpSession> sessionInfo = new ThreadLocal<HttpSession>();
	private static String ccxmlHost;
	private static int ccxmlPort;
	private static boolean hostedVoxeo;
	
	private static IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
			.setIoThreadCount(100)
			.setSoTimeout(15, TimeUnit.SECONDS)
			.build();
//	private static RequestConfig requestConfig = RequestConfig.custom()
//			.setConnectionRequestTimeout(3, TimeUnit.SECONDS)
//			.build();
	private static CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
			.setIOReactorConfig(ioReactorConfig)
			.build();
	
	static {
		try {
			ccxmlHost = (String) TLVXManager.getProperties().get("CCXMLHost");
			ccxmlPort = Integer.parseInt((String) TLVXManager.getProperties().get("CCXMLPort"));
			hostedVoxeo = Boolean.parseBoolean((String) TLVXManager.getProperties().get("hostedVoxeo"));
			httpclient.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void init() throws ServletException {
		super.init();
		
		String voiceApplicationClassName = getInitParameter("VoiceApplication");
		try {
			voiceApplication = (VoiceApplication) Class.forName(voiceApplicationClassName).newInstance();
			voiceApplication.setVoiceService(this);
			voiceApplication.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IllegalArgumentException {
		String threadName = Thread.currentThread().getName();
		sessionInfo.set(req.getSession());
		String sessionid = req.getParameter("sessionid");
		req.getSession().setAttribute("callSessionId", sessionid);
		//sessions.put(sessionid, req.getSession());
		
		try {
			VoxeoCallEvents event = VoxeoCallEvents.Unknown;
			try {
				event = VoxeoCallEvents.valueOf(req.getParameter("eventname"));
			} catch(Exception e) { e.printStackTrace(); }
			String eventString = "recvEvent: [";
			for (Object key : req.getParameterMap().keySet()) {
				eventString += " "+key+": "+req.getParameter((String)key);
			}
			eventString +="]";
			LOG.info(eventString);
			synchronized(req.getSession()) {
				switch(event) {
				case CCXMLLoaded: voiceApplication.ccxmlLoaded(getCall(req)); break;
				case CallIncoming: voiceApplication.incomingCall(getCall(req)); break;
				case CallOutgoing: voiceApplication.outgoingCall(getCall(req)); break;
				case CallConnected: Thread.sleep(500); voiceApplication.connectedCall(getCall(req)); break;
				case CallDisconnected: voiceApplication.disconnectedCall(getCall(req)); break;
				case ConferenceCreated: voiceApplication.createdConference(getCall(req)); break;
				case ConferenceDestroyed: voiceApplication.conferenceDestroyed(getCall(req)); break;
				case DialogStarted: voiceApplication.dialogStarted(getCall(req)); break;
				case DialogExit: voiceApplication.dialogExit(getCall(req)); break;
				case ConferenceJoined: voiceApplication.conferenceJoined(getCall(req)); break;
				case ConferenceErrorJoin: voiceApplication.conferenceErrorJoin(getCall(req)); break;
				case ConferenceUnjoined: voiceApplication.conferenceUnjoined(getCall(req)); break;
				case SessionDestroyed: voiceApplication.sessionDestroyed(getCall(req)); break;
				case CallConnectionFailed: voiceApplication.callConnectionFailed(getCall(req)); break;
				case CallConnectionErrorWrongstate: voiceApplication.callConnectionErrorWrongstate(getCall(req)); break;
				case ErrorSemantic: voiceApplication.errorSemantic(getCall(req)); break;
				case DialogErrorNotstarted: voiceApplication.errorDialogNotStarted(getCall(req)); break;
				default:
					System.out.println("UNHANDLED EVENT................");
					Enumeration<String> headernames = req.getHeaderNames();
					while (headernames.hasMoreElements()) {
						String headername = headernames.nextElement();
						System.out.println("Header "+headername+": "+req.getHeader(headername));
					}
					for (Object key : req.getParameterMap().keySet()) {
						System.out.println("Parameter "+key+": "+req.getParameter((String)key));
					}
					System.out.println("......................................");
					break;
				}
			}
		} catch(Exception e) {
			LOG.error("*ERROR with event "+req.getParameter("eventname"),e);
			e.printStackTrace();
		} finally {
			voiceApplication.cleanupSession();
			sessionInfo.remove();
		}
		Thread.currentThread().setName(threadName);
	}

	public static void answer(Call call) {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("eventname", "CallAnswer"));
		qparams.add(new BasicNameValuePair("sessionid", ((VoxeoCall)call).get("sessionid")));
		qparams.add(new BasicNameValuePair("connectionid", ((VoxeoCall)call).get("connectionid")));
		sendEvent((VoxeoCall)call, qparams);
	}

	public Call getCall(HttpServletRequest req) {
		Map<String, String[]> callInfo = req.getParameterMap();
		VoxeoCall call = new VoxeoCall();
		call.put("http.session.id", sessionInfo.get().getId());
		call.put("remoteAddress", getClientIpAddr(req));
		//System.out.println(">>>>>  "+call.get("remoteAddress"));
		call.putAll(callInfo);
		return call;
	}
	
	public static String getClientIpAddr(HttpServletRequest request) {  
//		Enumeration<String> headers = request.getHeaderNames();
//		while(headers.hasMoreElements()) {
//			String name = headers.nextElement();
//			System.out.println(">> "+name+": "+request.getHeader(name));
//		}
        String ip = request.getHeader("X-Forwarded-For");  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("Proxy-Client-IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("WL-Proxy-Client-IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("HTTP_CLIENT_IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getRemoteAddr();  
        }  
        return ip;  
    }
	
    public static void sendEvent(VoxeoCall call, List<NameValuePair> qparams)
    {
		try {
			String sendHost = call.get("remoteAddress");
			//System.out.println("call.remoteAddress="+sendHost );
			if (sendHost == null) { sendHost = ccxmlHost; /*System.out.println("property.ccxmlHost="+sendHost ); */ }
			int sendPort = 9999;
			String sendUrl = "/CCXML.send";
			if (hostedVoxeo) {
				sendHost = ccxmlHost;
				sendPort = ccxmlPort;
				sendUrl = "/SessionControl/CCXML.send";
			}
			HttpHost host = new HttpHost("http", sendHost, sendPort);
			URI uri = URIUtils.create(host, sendUrl + "?" + URLEncodedUtils.format(qparams, Charset.forName("UTF-8")));
			//System.out.println("sendEvent: "+uri);
			SimpleHttpRequest request1 = SimpleHttpRequests.get(uri);
			httpclient.execute(request1, new FutureCallback<SimpleHttpResponse>() {
				@Override
				public void cancelled() {
					
				}

				@Override
				public void completed(SimpleHttpResponse response) {
//					if (response!=null) {
//						try {
//							response.close();
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					}
				}

				@Override
				public void failed(Exception e) {
					e.printStackTrace();
				}
			});
    		//HttpGet httpget = new HttpGet(uri);
    		//httpclient.execute(httpget);
		} catch (RuntimeException e) {
			LOG.error(e);
			e.printStackTrace();
		} catch (Throwable e) {
			LOG.error(e);
			e.printStackTrace();
		} finally {
			
		}
    }

	public static void hangup(VoxeoCall voxeoCall) {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("eventname", "CallDisconnect"));
		qparams.add(new BasicNameValuePair("sessionid", ((VoxeoCall)voxeoCall).get("sessionid")));
		qparams.add(new BasicNameValuePair("connectionid", voxeoCall.get("connectionid")));
		sendEvent(voxeoCall, qparams);
	}

	public static void customDialog(VoxeoCall call, String url) {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("eventname", "DialogStart"));
		qparams.add(new BasicNameValuePair("sessionid", call.get("sessionid")));
		qparams.add(new BasicNameValuePair("src", url));
		qparams.add(new BasicNameValuePair("connectionid", ((VoxeoCall)call).get("connectionid")));
		sendEvent(call, qparams);
	}
	
	public static void conferenceDialog(VoxeoCall call, String conferenceId, String src) {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("eventname", "DialogStart"));
		qparams.add(new BasicNameValuePair("sessionid", call.get("sessionid")));
		qparams.add(new BasicNameValuePair("src", src));
		qparams.add(new BasicNameValuePair("conferenceid", conferenceId));
		sendEvent(call, qparams);
	}

	public static void createConference(VoxeoCall voxeoCall, String conferenceName) {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("sessionid", voxeoCall.get("sessionid")));
		qparams.add(new BasicNameValuePair("confname", conferenceName));
		qparams.add(new BasicNameValuePair("eventname", "ConferenceCreate"));
		sendEvent(voxeoCall, qparams);
	}

	public static void connectCall(Call call) {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("eventname", "CreateCall"));
		if (((VoxeoCall)call).get("connectionid") != null) {
			qparams.add(new BasicNameValuePair("connectionid", ((VoxeoCall)call).get("connectionid")));
		}
		if (((VoxeoCall)call).get("cpa") != null) {
			qparams.add(new BasicNameValuePair("cpa", ((VoxeoCall)call).get("cpa")));
		}
		qparams.add(new BasicNameValuePair("sessionid", ((VoxeoCall)call).get("sessionid")));
		qparams.add(new BasicNameValuePair("dest", ((VoxeoCall)call).get("dest")));
		qparams.add(new BasicNameValuePair("timeout", ((VoxeoCall)call).get("timeout")));
		qparams.add(new BasicNameValuePair("callerid", ((VoxeoCall)call).get("callerid")));
		sendEvent((VoxeoCall)call, qparams);
	}
	
	@Override
	public Call createSipCall(Map<String, String> params) {
		VoxeoCall call = new VoxeoCall();
		for(Object key : params.keySet()) 
			if (null != params.get(key))
			call.put((String)key, (String)params.get(key));
		return call;
	}

	public static void stopDialog(Call call, String dialogId) {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("eventname", "DialogTerminate"));
		qparams.add(new BasicNameValuePair("sessionid", ((VoxeoCall)call).get("sessionid")));
		qparams.add(new BasicNameValuePair("dialogid", dialogId));
		sendEvent((VoxeoCall)call, qparams);
	}

	public static void join(VoxeoCall call, String id1, String id2, String duplex, String termdigits, String entertone) {
		if (entertone == null) entertone = "true";
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("eventname", "ConferenceJoin"));
		qparams.add(new BasicNameValuePair("sessionid", call.get("sessionid")));
		qparams.add(new BasicNameValuePair("id1", id1));
		qparams.add(new BasicNameValuePair("id2", id2));
		qparams.add(new BasicNameValuePair("entertone", ""+entertone));
		if (termdigits != null && termdigits.length()>0) qparams.add(new BasicNameValuePair("termdigits", termdigits));
		qparams.add(new BasicNameValuePair("duplex", duplex));
		sendEvent(call, qparams);
	}
	
	public static void unjoin(VoxeoCall call, String id1, String id2/* SIMPLE, ActionCallback<Boolean> actionCallback */) {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("eventname", "ConferenceUnjoin"));
		qparams.add(new BasicNameValuePair("sessionid", call.get("sessionid")));
		qparams.add(new BasicNameValuePair("id1", id1));
		qparams.add(new BasicNameValuePair("id2", id2));
		sendEvent(call, qparams);
	}

	public static void stopConference(Call call, String confId) {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("eventname", "ConferenceDestroy"));
		qparams.add(new BasicNameValuePair("sessionid", ((VoxeoCall)call).get("sessionid")));
		qparams.add(new BasicNameValuePair("conferenceid", confId));
		sendEvent((VoxeoCall)call, qparams);
	}

	public static void destroySession(VoxeoCall call) {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("eventname", "SessionDestroy"));
		qparams.add(new BasicNameValuePair("sessionid", ((VoxeoCall)call).get("sessionid")));
		sendEvent(call, qparams);
	}

	@Override
	public void sessionCreated(HttpSessionEvent event) {
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		//String sessionid = (String)event.getSession().getAttribute("sessionid");
		//if (sessionid != null) sessions.remove(sessionid);
	}

	public static void recordCall(VoxeoCall voxeoCall, String connectionid, String recordingtag) {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("eventname", "RecordCall"));
		qparams.add(new BasicNameValuePair("sessionid", voxeoCall.get("sessionid")));
		qparams.add(new BasicNameValuePair("connectionid", connectionid));
		qparams.add(new BasicNameValuePair("recordingtag", recordingtag));
		sendEvent(voxeoCall, qparams);
	}

	public static void recordCallStop(VoxeoCall voxeoCall, String connectionid, String recordingtag) {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("eventname", "RecordCallStop"));
		qparams.add(new BasicNameValuePair("sessionid", voxeoCall.get("sessionid")));
		qparams.add(new BasicNameValuePair("connectionid", connectionid));
		qparams.add(new BasicNameValuePair("recordingtag", recordingtag));
		sendEvent(voxeoCall, qparams);
	}

	//"http://"+host+":9999/SessionControl/CCXML10.start"
	//http://api.voxeo.net/SessionControl/4.5.41/CCXML10.start
	public static String startNewSession(String ccxmlServer, String tempSessionId) {
//		CloseableHttpResponse response = null;
//		HttpGet httpget = null;
		try {
			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			String app = TLVXManager.getProperties().getProperty("voxeoApp");
			qparams.add(new BasicNameValuePair("tokenid", app));
			qparams.add(new BasicNameValuePair("tempsessionid", tempSessionId));
			if (ccxmlServer == null) { ccxmlServer = ccxmlHost; /*System.out.println("property.ccxmlHost="+sendHost ); */ }
			int sendPort = 9999;
			String sendUrl = "/SessionControl/CCXML10.start";
			if (hostedVoxeo) {
				sendPort = ccxmlPort;
				sendUrl = "/SessionControl/4.5.41/CCXML10.start";
			}
			//URI uri = URIUtils.createURI("http", ccxmlServer, sendPort, sendUrl, URLEncodedUtils.format(qparams, "UTF-8"), null);
			//LOG.info("sendEvent: "+ccxmlServer+":"+sendPort+" "+sendUrl+" "+qparams);
			HttpHost host = new HttpHost("http", ccxmlServer, sendPort);
			URI uri = URIUtils.create(host, sendUrl + "?" + URLEncodedUtils.format(qparams, Charset.forName("UTF-8")));
			//System.out.println("startNewSession: "+uri);
			SimpleHttpRequest request = SimpleHttpRequests.get(uri);
			Future<SimpleHttpResponse> future = httpclient.execute(request, null);
			SimpleHttpResponse response = future.get();
			String responseString = response.getBodyText();
			return responseString;
		} catch (RuntimeException e) {
			LOG.error(e);
			e.printStackTrace();
			return null;
		} catch (Throwable e) {
			LOG.error(e);
			e.printStackTrace();
			return null;
		} finally {
//			IOUtils.closeQuietly(response);
		}
	}
}
