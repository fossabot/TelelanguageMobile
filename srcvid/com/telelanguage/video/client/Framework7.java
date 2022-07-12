package com.telelanguage.video.client;

import com.google.gwt.core.client.JavaScriptObject;

public class Framework7 {
	
	private Framework7Listener listener;
	private JavaScriptObject toast;
	private JavaScriptObject thisApp;

	public Framework7(Framework7Listener listener) {
		this.listener = listener;
		initFramework7();
	}
	
	public void showToast(String message) {
		showNativeToast(message);
	}
	
	private native void showNativeToast(String message) /*-{
		var framework7instance = this;
		framework7instance.@com.telelanguage.video.client.Framework7::toast.show(message);
	}-*/;
	
	public void showPopup(String popup) {
		showNativePopup(popup);
	}
	
	private native void showNativePopup(String popup) /*-{
		var framework7instance = this;
		framework7instance.@com.telelanguage.video.client.Framework7::thisApp.popup(popup);
	}-*/;
	
	public native void initDropdown(String id, String values) /*-{
		var framework7instance = this;
		var values = (values).split(',');
		var autocompleteDropdownSimple = framework7instance.@com.telelanguage.video.client.Framework7::thisApp
				.autocomplete({
					input : '#'+id,
					openIn : 'dropdown',
					source : function(autocomplete, query, render) {
						var results = [];
						if (query.length === 0) {
							render(results);
							return;
						}
						// Find matched items
						for ( var i = 0; i < values.length; i++) {
							if (values[i].toLowerCase().indexOf(
									query.toLowerCase()) >= 0)
								results.push(values[i]);
						}
						// Render items by passing array with result items
						render(results);
					}
				});
	}-*/;

	private native void initFramework7() /*-{
		var framework7instance = this;
		// Initialize your app
		var myApp = new $wnd.Framework7();
		framework7instance.@com.telelanguage.video.client.Framework7::thisApp = myApp;
		framework7instance.@com.telelanguage.video.client.Framework7::toast = myApp.toast('Marked star', '', {});
		// Export selectors engine
		var $$ = $wnd.Dom7;
		
		$$($wnd.document).on('pageBeforeAnimation', function (e) {
			//alert(e);
			framework7instance.@com.telelanguage.video.client.Framework7::notifyNewPage(Ljava/lang/String;Ljava/lang/String;)(e.detail.page.url, e.detail.page.fromPage.url);
		});
		
		// Add view
		var mainView = myApp.addView('.view-main', {
		    // Because we use fixed-through navbar we can enable dynamic navbar
		    dynamicNavbar: true, domCache:true
		});
	}-*/;
	
	private void notifyNewPage(String toPage, String fromPage) {
		listener.pageChange(toPage, fromPage);
	}
	
	public native void back() /*-{
		var framework7instance = this;
		var thisView = framework7instance.@com.telelanguage.video.client.Framework7::thisApp.getCurrentView();
		thisView.router.back({});
	}-*/;
	
	public native void open(String pageNameVal) /*-{
		var framework7instance = this;
		var thisView = framework7instance.@com.telelanguage.video.client.Framework7::thisApp.getCurrentView();
		thisView.router.load({
			pageName: pageNameVal
		});
	}-*/;
}
