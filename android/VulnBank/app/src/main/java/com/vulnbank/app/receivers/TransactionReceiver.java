package com.vulnbank.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * TransactionReceiver - BroadcastReceiver for transaction events
 *
 * INTENTIONAL VULNERABILITIES:
 * 1. Exported without any permission (android:exported="true")
 * 2. Any app or ADB can send broadcasts to trigger actions
 * 3. Logs sensitive transaction data
 * 4. Accepts amount and account data from untrusted intents
 *
 * Exploit via ADB:
 * adb shell am broadcast -a com.vulnbank.TRANSACTION_COMPLETE --es account "ACC-001" --es amount "9999"
 * adb shell am broadcast -a com.vulnbank.SEND_MONEY --es to "attacker_account" --es amount "9999"
 */
public class TransactionReceiver extends BroadcastReceiver {

    private static final String TAG = "TransactionReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Received broadcast: " + action);

        if ("com.vulnbank.TRANSACTION_COMPLETE".equals(action)) {
            // VULNERABILITY: Logs data from untrusted intent
            String account = intent.getStringExtra("account");
            String amount = intent.getStringExtra("amount");
            String description = intent.getStringExtra("description");

            Log.d(TAG, "Transaction complete - Account: " + account
                + " Amount: " + amount + " Description: " + description);

            Toast.makeText(context,
                "Transaction: " + amount + " to " + account,
                Toast.LENGTH_SHORT).show();

        } else if ("com.vulnbank.SEND_MONEY".equals(action)) {
            // VULNERABILITY: Processes money transfer from untrusted broadcast
            String fromAccount = intent.getStringExtra("from");
            String toAccount = intent.getStringExtra("to");
            String amount = intent.getStringExtra("amount");

            Log.d(TAG, "SEND_MONEY received - From: " + fromAccount
                + " To: " + toAccount + " Amount: " + amount);

            // VULNERABILITY: Would trigger actual transfer if implemented
            Toast.makeText(context,
                "Money transfer requested: " + amount,
                Toast.LENGTH_SHORT).show();
        }
    }
}
