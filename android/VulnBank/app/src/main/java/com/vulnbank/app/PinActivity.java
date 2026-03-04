package com.vulnbank.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * PinActivity - PIN verification screen
 *
 * INTENTIONAL VULNERABILITIES:
 * 1. PIN check done entirely client-side
 * 2. Hardcoded PIN in source code
 * 3. PIN logged to logcat
 * 4. No server-side verification
 */
public class PinActivity extends AppCompatActivity {

    private static final String TAG = "PinActivity";

    // VULNERABILITY: Hardcoded PIN in source code - easily found via reverse engineering
    private static final String CORRECT_PIN = "1234";

    private EditText etPin;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        etPin = findViewById(R.id.et_pin);
        tvResult = findViewById(R.id.tv_result);

        // VULNERABILITY: Log the PIN on activity creation
        Log.d(TAG, "PIN screen loaded. Expected PIN: " + CORRECT_PIN);

        Button btnVerify = findViewById(R.id.btn_verify_pin);
        btnVerify.setOnClickListener(v -> verifyPin());
    }

    private void verifyPin() {
        String enteredPin = etPin.getText().toString();

        // VULNERABILITY: Logs entered PIN
        Log.d(TAG, "PIN entered: " + enteredPin + " | Correct PIN: " + CORRECT_PIN);

        // VULNERABILITY: Client-side only PIN check
        if (enteredPin.equals(CORRECT_PIN)) {
            tvResult.setText("✓ PIN Verified! Access Granted.");

            // Store that PIN was verified - easily bypassed
            SharedPreferences prefs = getSharedPreferences("VulnBankPrefs", MODE_PRIVATE);
            prefs.edit().putBoolean("pin_verified", true).apply();

            Toast.makeText(this, "Access granted!", Toast.LENGTH_SHORT).show();
        } else {
            tvResult.setText("✗ Incorrect PIN. Try again.");
            Log.d(TAG, "Wrong PIN entered: " + enteredPin);
        }
    }
}
