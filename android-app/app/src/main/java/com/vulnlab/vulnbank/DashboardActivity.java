package com.vulnlab.vulnbank;
import android.app.Activity;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import android.widget.*;

public class DashboardActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        String username = prefs.getString("username", "guest");
        boolean isAdmin = prefs.getBoolean("isAdmin", false);

        TextView tvWelcome = findViewById(R.id.tv_welcome);
        // VULN: XSS-like: username from prefs set directly into HTML if loaded in WebView elsewhere
        tvWelcome.setText("Welcome, " + username + (isAdmin ? " [ADMIN]" : ""));

        // VULN: query exported ContentProvider - leaks all user data
        try {
            Uri uri = Uri.parse("content://com.vulnlab.vulnbank.provider");
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Log.d("VulnBank", "User: " + cursor.getString(cursor.getColumnIndex("username"))
                        + " pass=" + cursor.getString(cursor.getColumnIndex("password"))
                        + " token=" + cursor.getString(cursor.getColumnIndex("token")));
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("VulnBank", "CP query failed: " + e.getMessage());
        }

        Button btnTransfer = findViewById(R.id.btn_transfer);
        btnTransfer.setOnClickListener(v -> {
            startActivity(new Intent(this, TransferActivity.class));
        });

        Button btnWebView = findViewById(R.id.btn_webview);
        btnWebView.setOnClickListener(v -> {
            Intent i = new Intent(this, WebViewActivity.class);
            // VULN: URL from prefs (attacker-controlled if prefs poisoned)
            i.putExtra("url", prefs.getString("redirect_url", "https://vulnbank.lab/dashboard"));
            startActivity(i);
        });
    }
}