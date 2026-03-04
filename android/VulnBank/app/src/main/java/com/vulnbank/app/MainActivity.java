package com.vulnbank.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity - Entry point of the app
 * Checks if user is already logged in (via SharedPreferences) and redirects accordingly
 */
public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "VulnBankPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String token = prefs.getString("auth_token", "");

        if (!token.isEmpty()) {
            // Auto-login using stored token - no validation of token expiry
            int userId = prefs.getInt("user_id", 0);
            String username = prefs.getString("logged_in_user", "");

            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("user_id", userId);
            intent.putExtra("token", token);
            intent.putExtra("username", username);
            startActivity(intent);
            finish();
            return;
        }

        Button btnLogin = findViewById(R.id.btn_go_to_login);
        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }
}
