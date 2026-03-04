package com.vulnbank.app;

import android.app.Application;
import android.util.Log;

/**
 * VulnBank Application class
 * VULNERABILITY: Logs sensitive startup information
 */
public class VulnBankApp extends Application {

    private static final String TAG = "VulnBankApp";

    @Override
    public void onCreate() {
        super.onCreate();
        // VULNERABILITY: Sensitive data in logs
        Log.d(TAG, "VulnBank starting up...");
        Log.d(TAG, "API Key: " + Constants.API_KEY);
        Log.d(TAG, "Backend URL: " + Constants.BASE_URL);
        Log.d(TAG, "Encryption Key: " + Constants.ENCRYPTION_KEY);
    }
}
