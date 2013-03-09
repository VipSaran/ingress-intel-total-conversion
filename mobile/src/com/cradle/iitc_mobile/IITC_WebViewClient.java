package com.cradle.iitc_mobile;

import java.io.IOException;

import android.content.Context;
import android.net.http.SslError;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class IITC_WebViewClient extends WebViewClient {

	private Context ctx;

	public IITC_WebViewClient(Context ctx) {
		super();
		this.ctx = ctx;
	}

	// enable https
	@Override
	public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
		handler.proceed() ;
	};

	// injecting IITC when page is loaded
	@Override
	public void onPageFinished(WebView web, String Url) {
		Log.d("loading finish", web.getUrl());
		if (web.getUrl().contains("ingress.com/intel") && !web.getUrl().contains("accounts")) {
			// first check for cookies, than inject javascript
			// this enables the user to login if necessary
			CookieManager cm = CookieManager.getInstance();
			final String cookie = cm.getCookie("https://www.ingress.com/intel");

			if(cookie != null) {

				// add iitc
				addJs(web, "iitc-debug.user.js", 0);

				// plugins
				addJs(web, "show-portal-weakness.user.js", 100);
				addJs(web, "guess-player-levels.user.js", 100);
				addJs(web, "player-tracker.user.js", 100);
				addJs(web, "reso-energy-pct-in-portal-detail.user.js", 100);
				addJs(web, "show-address.user.js", 100);
				addJs(web, "compute-ap-stats.user.js", 100);

			}
		}
	}

	private void addJs(WebView web, String f, int t) {
		// load plugins using setTimeout to prevent weird timing issues that shouldn't actually exist
		web.loadUrl("javascript: setTimeout(function() { "
				+ "var script=document.createElement('script');"
				+ "script.type='text/javascript';"
				+ "script.src='iitc://"+f+"';"
				+ "document.getElementsByTagName('head').item(0).appendChild(script);"
				+ "}, "+String.valueOf(t)+")");
	}

	@Override
	public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
		// mean hack to make loading local js into the security context of the intel map possible
		// http://stackoverflow.com/a/5656971

		String scheme = "iitc://";
		if (url.startsWith(scheme)) {
			try {

				return new WebResourceResponse(
						url.endsWith("js") ? "text/javascript" : "text/css",
						"utf-8",
						ctx.getAssets().open(url.substring(scheme.length()))
					);
			} catch (IOException e) {
				Log.e(getClass().getSimpleName(), e.getMessage(), e);
			}
		}
		return null;
	}
}
