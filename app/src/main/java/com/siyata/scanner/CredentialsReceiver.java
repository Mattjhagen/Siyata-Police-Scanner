package com.siyata.scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Receives broadcast to set Broadcastify credentials via ADB
 * Usage: adb shell am broadcast -a com.siyata.scanner.SET_CREDENTIALS --es username "USER" --es password "PASS"
 */
public class CredentialsReceiver extends BroadcastReceiver {
    private static final String TAG = "CredentialsReceiver";
    private static final String ACTION_SET_CREDENTIALS = "com.siyata.scanner.SET_CREDENTIALS";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_SET_CREDENTIALS.equals(intent.getAction())) {
            String username = intent.getStringExtra("username");
            String password = intent.getStringExtra("password");

            if (username != null && password != null) {
                BroadcastifyAuthManager authManager = new BroadcastifyAuthManager(context);
                authManager.saveCredentials(username, password);

                Log.d(TAG, "Credentials saved for user: " + username);
                Toast.makeText(context, "Broadcastify credentials saved", Toast.LENGTH_SHORT).show();

                // Try to authenticate immediately in background
                new Thread(() -> {
                    boolean success = authManager.authenticate();
                    Log.d(TAG, "Authentication " + (success ? "successful" : "failed"));
                }).start();
            } else {
                Log.w(TAG, "Invalid credentials received");
                Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
