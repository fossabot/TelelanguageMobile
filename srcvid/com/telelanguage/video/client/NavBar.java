package com.telelanguage.video.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.UListElement;

public class NavBar {
	static UListElement loginInfo = Document.get().getElementById("login-info").cast();
	static NavBarListener listener;
	
	public static void showLogins() {
		loginInfo.removeAllChildren();
		loginInfo.setInnerHTML(
				"<li id=\"login-prompt\" class=\"dropdown\">\r\n" + 
				"	<a href=\"#\" class=\"dropdown-toggle\" data-toggle=\"dropdown\" role=\"button\" aria-haspopup=\"true\" aria-expanded=\"false\">Login<span class=\"caret\"></span></a>\r\n" + 
				"	<ul class=\"dropdown-menu\">\r\n" + 
				"		<li><a onclick=\"OggFlowAPI.loginFacebook(false); return false;\"><span class=\"fa fa-facebook\"></span> &nbsp;Facebook </a></li>\r\n" + 
				"		<li><a onclick=\"OggFlowAPI.loginGoogle(false); return false;\"><span class=\"fa fa-google\"></span> &nbsp;Google </a></li>\r\n" + 
				"		<li><a onclick=\"OggFlowAPI.loginLinkedin(false); return false;\"><span class=\"fa fa-linkedin\"></span> &nbsp;Linkedin </a></li>\r\n" + 
				"	</ul>\r\n" + 
				"</li>");
	}

	public static void showUserInfo() {
		loginInfo.removeAllChildren();
		loginInfo.setInnerHTML("<li class=\"dropdown\">\r\n" + 
				"    <a href=\"#\" class=\"dropdown-toggle\" data-toggle=\"dropdown\">\r\n" + 
				"    <img src=\""+getProfilePic()+"\" class=\"profile-image img-circle\" style=\"width: 24px\"> &nbsp; "+getName()+" <b class=\"caret\"></b></a>\r\n" + 
				"    <ul class=\"dropdown-menu\">\r\n" + 
				"        <li><a href=\"#Account\"><i class=\"fa fa-cog\"></i> Account</a></li>\r\n" + 
				"        <li class=\"divider\"></li>\r\n" + 
				"        <li><a onclick=\"OggFlowAPI.showChatWidget(); return false;\"><i class=\"fa fa-question\"></i> Request Help </a></li>\r\n" + 
				"        <li class=\"divider\"></li>\r\n" + 
				"        <li><a onclick=\"OggFlowAPI.logout(); return false;\"><i class=\"fa fa-sign-out\"></i> Logout </a></li>\r\n" + 
				"    </ul>\r\n" + 
				"</li>");
	}
	
	private static native boolean isLoggedIn() /*-{
		return $wnd.OggFlowAPI.loggedin;
	}-*/;
	
	private static native String getName() /*-{
		return $wnd.OggFlowAPI.name;
	}-*/;

	private static native String getEmail() /*-{
		return $wnd.OggFlowAPI.email;
	}-*/;

	private static native String getProfilePic() /*-{
		return $wnd.OggFlowAPI.profile_pic;
	}-*/;

	private static native String getSessionId() /*-{
		return $wnd.OggFlowAPI.session_id;
	}-*/;

	public static void contextLoaded() {
		if (!isLoggedIn()) {
			NavBar.showLogins();
			listener.navBarLoaded(false);
		} else {
			NavBar.showUserInfo();
			listener.navBarLoaded(true);
		}
	}
	
	public static void updated() {
		contextLoaded();
	}

	public static void initUserContext(NavBarListener listener) {
		NavBar.listener = listener;
		initUserContextNative();
	}
	
	private static native void initUserContextNative() /*-{
		if (typeof $wnd.OggFlowAPI === "undefined") {
		   $wnd.onOggFlowReady = function() {
		   	  $wnd.OggFlowAPI.updated = function() {
		   	  	 @com.telelanguage.video.client.NavBar::updated()();
		   	  }
		      @com.telelanguage.video.client.NavBar::contextLoaded()();
		   }
		} else {
		   $wnd.OggFlowAPI.updated = function() {
		   	  @com.telelanguage.video.client.NavBar::updated()();
		   }
		   @com.telelanguage.video.client.NavBar::contextLoaded()();
		}
	}-*/;
}
