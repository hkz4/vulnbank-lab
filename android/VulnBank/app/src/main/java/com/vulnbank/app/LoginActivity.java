package com.vulnbank.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.vulnbank.app.utils.NetworkHelper;

import org.json.JSONObject;

/**
 * LoginActivity - Handles user login
 *
 * INTENTIONAL VULNERABILITIES:
 * 1. Stores credentials in plaintext SharedPreferences
 * 2. Stores auth token in plaintext SharedPreferences
 * 3. Logs credentials with Log.d()
 * 4. No SSL certificate validation (via NetworkHelper)
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String PREFS_NAME = "VulnBankPrefs";

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvStatus = findViewById(R.id.tv_status);

        // VULNERABILITY: Auto-fill from stored plaintext credentials
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedUsername = prefs.getString("username", "");
        String savedPassword = prefs.getString("password", "");
        if (!savedUsername.isEmpty()) {
            etUsername.setText(savedUsername);
            etPassword.setText(savedPassword);
        }

        btnLogin.setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        // VULNERABILITY: Logging credentials in plaintext
        Log.d(TAG, "Login attempt - Username: " + username + " Password: " + password);
        Log.d(TAG, "Using API endpoint: " + Constants.BASE_URL + "/api/login");

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // VULNERABILITY: Store credentials in plaintext SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", username);
        editor.putString("password", password);  // VULNERABILITY: Storing password in plaintext
        editor.apply();

        tvStatus.setText("Logging in...");

        // Perform login request in background thread
        new Thread(() -> {
            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("username", username);
                requestBody.put("password", password);

                String response = NetworkHelper.post(Constants.BASE_URL + "/api/login", requestBody.toString());
                JSONObject jsonResponse = new JSONObject(response);

                runOnUiThread(() -> {
                    try {
                        if (jsonResponse.optBoolean("success", false)) {
                            String token = jsonResponse.optString("token");
                            int userId = jsonResponse.optInt("user_id");

                            // VULNERABILITY: Store token in plaintext SharedPreferences
                            SharedPreferences.Editor ed = prefs.edit();
                            ed.putString("auth_token", token);
                            ed.putInt("user_id", userId);
                            ed.putString("logged_in_user", username);
                            ed.apply();

                            // VULNERABILITY: Log the token
                            Log.d(TAG, "Login successful! Token: " + token);
                            Log.d(TAG, "User ID: " + userId);

                            // Navigate to dashboard
                            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                            intent.putExtra("user_id", userId);
                            intent.putExtra("token", token);
                            intent.putExtra("username", username);
                            startActivity(intent);
                            finish();
                        } else {
                            tvStatus.setText("Login failed: " + jsonResponse.optString("message"));
                            Log.d(TAG, "Login failed for user: " + username);
                        }
                    } catch (Exception e) {
                        tvStatus.setText("Error: " + e.getMessage());
                        Log.e(TAG, "Login error: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    tvStatus.setText("Network error: " + e.getMessage());
                    Log.e(TAG, "Network error: " + e.getMessage());
                });
            }
        }).start();
    }
}
