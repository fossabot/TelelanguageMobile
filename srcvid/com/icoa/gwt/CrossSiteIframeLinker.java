package com.icoa.gwt;

import com.google.gwt.core.ext.LinkerContext;

public class CrossSiteIframeLinker extends com.google.gwt.core.linker.CrossSiteIframeLinker {
	@Override
	protected String getJsDevModeRedirectHookPermitted(LinkerContext context) {
		return "$wnd.location.protocol == \"https:\" || $wnd.location.protocol == \"http:\" || $wnd.location.protocol == \"file:\"";
	}
}
