package com.vulnlab.vulnbank;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.*;
import android.util.Log;

public class WebViewActivity extends Activity {
    @SuppressLint({"SetJavaScriptEnabled","AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView webView = new WebView(this);
        setContentView(webView);
        WebSettings settings = webView.getSettings();
        // VULN: All dangerous WebView flags enabled
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setDomStorageEnabled(true);
        // VULN: JavascriptInterface exposed - RCE via JS bridge
        webView.addJavascriptInterface(new VulnBridge(this), "VulnBridge");
        String url = getIntent().getStringExtra("url");
        if (url == null || url.isEmpty()) {
            url = "https://vulnbank.lab/dashboard";
        }
        // VULN: load arbitrary URL from intent
        webView.loadUrl(url);
    }
}