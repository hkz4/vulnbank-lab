package com.vulnbank.app.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * NetworkHelper - HTTP utility methods
 *
 * INTENTIONAL VULNERABILITIES:
 * 1. TrustManager that accepts ALL certificates (including self-signed/invalid)
 * 2. HostnameVerifier that accepts ALL hostnames
 * 3. No certificate pinning
 * 4. Allows cleartext HTTP traffic
 * 5. Logs full request/response data including tokens
 */
public class NetworkHelper {

    private static final String TAG = "NetworkHelper";

    static {
        // VULNERABILITY: Install a TrustManager that trusts everything
        trustAllCertificates();
    }

    /**
     * VULNERABILITY: Custom TrustManager that accepts ALL SSL certificates
     * This completely defeats the purpose of HTTPS
     */
    private static void trustAllCertificates() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        // VULNERABILITY: Does nothing - trusts all client certs
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        // VULNERABILITY: Does nothing - trusts all server certs
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // VULNERABILITY: Accept all hostnames
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;  // Accept everything
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up trust manager: " + e.getMessage());
        }
    }

    public static String get(String urlString, String token) throws Exception {
        // VULNERABILITY: Logs URL and token
        Log.d(TAG, "GET " + urlString + " token=" + token);

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");

        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        return readResponse(conn);
    }

    public static String post(String urlString, String body) throws Exception {
        return post(urlString, body, null);
    }

    public static String post(String urlString, String body, String token) throws Exception {
        // VULNERABILITY: Logs full request body (may contain passwords)
        Log.d(TAG, "POST " + urlString + " body=" + body + " token=" + token);

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        OutputStream os = conn.getOutputStream();
        os.write(body.getBytes("UTF-8"));
        os.flush();

        return readResponse(conn);
    }

    private static String readResponse(HttpURLConnection conn) throws Exception {
        int responseCode = conn.getResponseCode();
        BufferedReader reader;

        if (responseCode >= 200 && responseCode < 300) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // VULNERABILITY: Logs full response (may contain sensitive data)
        Log.d(TAG, "Response: " + response.toString());

        return response.toString();
    }
}
