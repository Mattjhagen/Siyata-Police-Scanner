package com.siyata.scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

public class RotaryReceiver extends BroadcastReceiver {
    private static final String TAG = "RotaryReceiver";
    public static MainActivity mainActivity = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        Log.d(TAG, "Broadcast received: " + intentAction);

        if (!"com.br.intent.action.ROTARY_KNOB".equals(intentAction)) {
            return;
        }

        KeyEvent keyEvent = intent.getParcelableExtra("android.intent.extra.KEY_EVENT");
        if (keyEvent == null) {
            Log.w(TAG, "KeyEvent is null");
            return;
        }

        int keyCode = keyEvent.getKeyCode();
        int action = keyEvent.getAction();

        Log.d(TAG, "KeyEvent action: " + action + ", keyCode: " + keyCode + " (" + KeyEvent.keyCodeToString(keyCode) + ")");

        // Only process the events we care about
        if (keyCode != KeyEvent.KEYCODE_F4 && keyCode != KeyEvent.KEYCODE_F5 && keyCode != KeyEvent.KEYCODE_F8) {
            Log.d(TAG, "Ignoring non-rotary keyCode: " + keyCode);
            return;
        }

        // F4 sends ACTION_UP only, F5/F8 send ACTION_DOWN
        // Process ACTION_UP for F4, ACTION_DOWN for others
        if (keyCode == KeyEvent.KEYCODE_F4 && action != KeyEvent.ACTION_UP) {
            Log.d(TAG, "Ignoring F4 ACTION_DOWN (only process UP)");
            return;
        }
        if ((keyCode == KeyEvent.KEYCODE_F5 || keyCode == KeyEvent.KEYCODE_F8) && action != KeyEvent.ACTION_DOWN) {
            Log.d(TAG, "Ignoring F5/F8 ACTION_UP (only process DOWN)");
            return;
        }

        Log.d(TAG, "Processing rotary event: " + KeyEvent.keyCodeToString(keyCode));

        if (mainActivity == null) {
            Log.w(TAG, "MainActivity not available, starting activity");
            Intent launchIntent = new Intent(context, MainActivity.class);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(launchIntent);
            return;
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_F5: // Clockwise
                Log.d(TAG, "Handling clockwise (F5) -> Previous");
                mainActivity.handlePreviousFeed();
                break;
            case KeyEvent.KEYCODE_F4: // Counter-clockwise
                Log.d(TAG, "Handling counter-clockwise (F4) -> Next");
                mainActivity.handleNextFeed();
                break;
            case KeyEvent.KEYCODE_F8: // Press
                Log.d(TAG, "Handling press (F8) -> Toggle");
                mainActivity.handleTogglePlayStop();
                break;
            default:
                Log.w(TAG, "Unknown keyCode: " + keyCode);
        }
    }
}
