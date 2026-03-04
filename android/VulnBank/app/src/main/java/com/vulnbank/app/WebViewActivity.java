package com.vulnbank.app;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

/**
 * WebViewActivity - Displays web content
 *
 * INTENTIONAL VULNERABILITIES:
 * 1. JavaScript enabled
 * 2. File access allowed
 * 3. JavaScript interface exposes sensitive methods
 * 4. No URL validation - loads any URL from intent
 * 5. setAllowFileAccessFromFileURLs(true)
 * 6. setAllowUniversalAccessFromFileURLs(true)
 */
public class WebViewActivity extends AppCompatActivity {

    private static final String TAG = "WebViewActivity";
    private WebView webView;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        webView = findViewById(R.id.webview);

        // VULNERABILITY: Get URL directly from intent without validation
        String url = getIntent().getStringExtra("url");
        if (url == null) {
            // VULNERABILITY: scheme://host - can load local files
            url = getIntent().getDataString();
        }
        if (url == null) {
            url = Constants.BASE_URL;
        }

        Log.d(TAG, "Loading URL: " + url);

        WebSettings settings = webView.getSettings();

        // VULNERABILITY: Enable JavaScript
        settings.setJavaScriptEnabled(true);

        // VULNERABILITY: Allow file access
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        // VULNERABILITY: Allow file URLs to access other files
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);

        // VULNERABILITY: Enable DOM storage (can be used to store sensitive data)
        settings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient());

        // VULNERABILITY: JavaScript interface exposes sensitive app methods
        webView.addJavascriptInterface(new VulnBankJSInterface(), "VulnBankApp");

        webView.loadUrl(url);
    }

    /**
     * VULNERABILITY: Exposed JavaScript interface with sensitive methods
     * JS can call: VulnBankApp.getToken(), VulnBankApp.getCredentials(), etc.
     */
    public class VulnBankJSInterface {

        @android.webkit.JavascriptInterface
        public String getToken() {
            SharedPreferences prefs = getSharedPreferences("VulnBankPrefs", MODE_PRIVATE);
            String token = prefs.getString("auth_token", "");
            Log.d(TAG, "JS requested token: " + token);
            return token;
        }

        @android.webkit.JavascriptInterface
        public String getCredentials() {
            SharedPreferences prefs = getSharedPreferences("VulnBankPrefs", MODE_PRIVATE);
            String username = prefs.getString("username", "");
            String password = prefs.getString("password", "");
            Log.d(TAG, "JS requested credentials: " + username + ":" + password);
            return username + ":" + password;
        }

        @android.webkit.JavascriptInterface
        public String getApiKey() {
            return Constants.API_KEY;
        }

        @android.webkit.JavascriptInterface
        public void sendMessage(String message) {
            Log.d(TAG, "JS message: " + message);
        }
    }
}
