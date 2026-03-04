package com.vulnbank.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.vulnbank.app.utils.NetworkHelper;

import org.json.JSONObject;

/**
 * TransferActivity - Transfer money between accounts
 *
 * VULNERABILITY: No server-side ownership check (relies on vulnerable backend)
 * VULNERABILITY: Logs transfer details
 */
public class TransferActivity extends AppCompatActivity {

    private static final String TAG = "TransferActivity";

    private EditText etFromAccount;
    private EditText etToAccount;
    private EditText etAmount;
    private EditText etDescription;
    private TextView tvStatus;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        token = getIntent().getStringExtra("token");
        if (token == null) {
            SharedPreferences prefs = getSharedPreferences("VulnBankPrefs", MODE_PRIVATE);
            token = prefs.getString("auth_token", "");
        }

        etFromAccount = findViewById(R.id.et_from_account);
        etToAccount = findViewById(R.id.et_to_account);
        etAmount = findViewById(R.id.et_amount);
        etDescription = findViewById(R.id.et_description);
        tvStatus = findViewById(R.id.tv_status);

        Button btnTransfer = findViewById(R.id.btn_transfer);
        btnTransfer.setOnClickListener(v -> performTransfer());
    }

    private void performTransfer() {
        String fromAccount = etFromAccount.getText().toString();
        String toAccount = etToAccount.getText().toString();
        String amount = etAmount.getText().toString();
        String description = etDescription.getText().toString();

        // VULNERABILITY: Logs transfer details including amounts
        Log.d(TAG, "Transfer initiated: " + fromAccount + " -> " + toAccount + " amount: " + amount);

        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("from_account", fromAccount);
                body.put("to_account", toAccount);
                body.put("amount", Double.parseDouble(amount));
                body.put("description", description);

                String response = NetworkHelper.post(
                    Constants.BASE_URL + "/api/transfer", body.toString(), token);
                JSONObject json = new JSONObject(response);

                runOnUiThread(() -> {
                    try {
                        if (json.optBoolean("success", false)) {
                            tvStatus.setText("Transfer successful: " + json.optString("message"));
                            Log.d(TAG, "Transfer complete: " + json.toString());
                        } else {
                            tvStatus.setText("Transfer failed: " + json.optString("error"));
                        }
                    } catch (Exception e) {
                        tvStatus.setText("Error: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> tvStatus.setText("Network error: " + e.getMessage()));
                Log.e(TAG, "Transfer error: " + e.getMessage());
            }
        }).start();
    }
}
