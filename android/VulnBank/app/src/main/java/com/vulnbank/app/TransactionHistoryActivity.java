package com.vulnbank.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.vulnbank.app.utils.NetworkHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * TransactionHistoryActivity - Shows transaction history
 *
 * VULNERABILITY: Uses account_id from intent without validation (IDOR)
 * VULNERABILITY: Logs transaction data
 */
public class TransactionHistoryActivity extends AppCompatActivity {

    private static final String TAG = "TransactionHistory";

    private ListView lvTransactions;
    private TextView tvTitle;
    private String token;
    private int accountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        token = getIntent().getStringExtra("token");
        // VULNERABILITY: accountId from intent can be any value - IDOR
        accountId = getIntent().getIntExtra("account_id", 1);

        if (token == null) {
            SharedPreferences prefs = getSharedPreferences("VulnBankPrefs", MODE_PRIVATE);
            token = prefs.getString("auth_token", "");
        }

        lvTransactions = findViewById(R.id.lv_transactions);
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("Transactions for Account #" + accountId);

        // VULNERABILITY: Log account ID being queried
        Log.d(TAG, "Loading transactions for account: " + accountId + " token: " + token);

        loadTransactions();
    }

    private void loadTransactions() {
        new Thread(() -> {
            try {
                // VULNERABILITY: IDOR - can query any account_id
                String response = NetworkHelper.get(
                    Constants.BASE_URL + "/api/transactions/" + accountId, token);
                JSONObject json = new JSONObject(response);
                JSONArray transactions = json.optJSONArray("transactions");

                List<String> items = new ArrayList<>();
                if (transactions != null) {
                    for (int i = 0; i < transactions.length(); i++) {
                        JSONObject t = transactions.getJSONObject(i);
                        items.add(t.optString("from_account") + " -> " +
                                  t.optString("to_account") + ": $" +
                                  t.optDouble("amount") + " (" +
                                  t.optString("description") + ")");
                    }
                }

                runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_list_item_1, items);
                    lvTransactions.setAdapter(adapter);
                    Log.d(TAG, "Loaded " + items.size() + " transactions");
                });
            } catch (Exception e) {
                runOnUiThread(() -> Log.e(TAG, "Error: " + e.getMessage()));
            }
        }).start();
    }
}
