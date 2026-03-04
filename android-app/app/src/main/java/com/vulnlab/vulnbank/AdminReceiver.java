package com.vulnlab.vulnbank;
import android.content.*;
import android.util.Log;
// VULN: BroadcastReceiver exported - bat ky ai cung gui duoc command
public class AdminReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        String cmd = intent.getStringExtra("command");
        Log.w("VulnBank", "AdminReceiver cmd=" + cmd);
        if ("WIPE_DATA".equals(cmd)) {
            ctx.getSharedPreferences("session", 0).edit().clear().apply();
            Log.w("VulnBank", "Data wiped!");
        } else if ("GRANT_ADMIN".equals(cmd)) {
            String user = intent.getStringExtra("username");
            ctx.getSharedPreferences("session", 0).edit()
                .putBoolean("isAdmin", true)
                .putString("username", user != null ? user : "hacker")
                .apply();
            Log.w("VulnBank", "Admin granted to: " + user);
        } else if ("GET_TOKEN".equals(cmd)) {
            String token = ctx.getSharedPreferences("session", 0).getString("token", "no_token");
            Log.w("VulnBank", "TOKEN=" + token);
        }
    }
}