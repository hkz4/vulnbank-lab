package com.vulnbank.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.vulnbank.app.utils.NetworkHelper;

import org.json.JSONObject;

/**
 * DashboardActivity - Main dashboard after login
 * Shows account balance and navigation buttons
 *
 * VULNERABILITY: Can be launched directly without authentication (exported)
 * VULNERABILITY: Logs sensitive account data
 */
public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";
    private static final String PREFS_NAME = "VulnBankPrefs";

    private TextView tvWelcome;
    private TextView tvBalance;
    private TextView tvAccountNumber;

    private String token;
    private int userId;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // VULNERABILITY: No proper auth check - if launched from ADB, no token
        Intent intent = getIntent();
        token = intent.getStringExtra("token");
        userId = intent.getIntExtra("user_id", 1);
        username = intent.getStringExtra("username");

        if (token == null || token.isEmpty()) {
            // VULNERABILITY: Falls back to SharedPreferences instead of enforcing auth
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            token = prefs.getString("auth_token", "");
            userId = prefs.getInt("user_id", 1);
            username = prefs.getString("logged_in_user", "unknown");
        }

        tvWelcome = findViewById(R.id.tv_welcome);
        tvBalance = findViewById(R.id.tv_balance);
        tvAccountNumber = findViewById(R.id.tv_account_number);

        tvWelcome.setText("Welcome, " + username + "!");

        // VULNERABILITY: Logs token
        Log.d(TAG, "Dashboard loaded for user: " + username);
        Log.d(TAG, "Auth token: " + token);
        Log.d(TAG, "User ID: " + userId);

        loadAccountInfo();

        Button btnTransfer = findViewById(R.id.btn_transfer);
        btnTransfer.setOnClickListener(v -> {
            Intent i = new Intent(this, TransferActivity.class);
            i.putExtra("token", token);
            i.putExtra("user_id", userId);
            startActivity(i);
        });

        Button btnHistory = findViewById(R.id.btn_history);
        btnHistory.setOnClickListener(v -> {
            Intent i = new Intent(this, TransactionHistoryActivity.class);
            i.putExtra("token", token);
            i.putExtra("account_id", userId);  // Using user_id as account_id - IDOR
            startActivity(i);
        });

        Button btnWebView = findViewById(R.id.btn_webview);
        btnWebView.setOnClickListener(v -> {
            Intent i = new Intent(this, WebViewActivity.class);
            i.putExtra("url", Constants.BASE_URL + "/api/debug");
            startActivity(i);
        });

        Button btnPin = findViewById(R.id.btn_pin);
        btnPin.setOnClickListener(v -> {
            startActivity(new Intent(this, PinActivity.class));
        });

        Button btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void loadAccountInfo() {
        new Thread(() -> {
            try {
                // VULNERABILITY: account_id is user_id - IDOR vulnerability
                String response = NetworkHelper.get(
                    Constants.BASE_URL + "/api/account/" + userId, token);
                JSONObject json = new JSONObject(response);

                runOnUiThread(() -> {
                    try {
                        tvBalance.setText("Balance: $" + json.optDouble("balance", 0));
                        tvAccountNumber.setText("Account: " + json.optString("account_number"));

                        // VULNERABILITY: Logs sensitive account data
                        Log.d(TAG, "Account loaded: " + json.toString());
                        Log.d(TAG, "PIN hash: " + json.optString("pin_hash"));
                    } catch (Exception e) {
                        Log.e(TAG, "Error updating UI: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> tvBalance.setText("Error loading account: " + e.getMessage()));
                Log.e(TAG, "Error loading account: " + e.getMessage());
            }
        }).start();
    }
}
