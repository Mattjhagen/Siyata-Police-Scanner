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
        String action = intent.getAction();

        if (!"com.br.intent.action.ROTARY_KNOB".equals(action)) {
            return;
        }

        KeyEvent keyEvent = intent.getParcelableExtra("android.intent.extra.KEY_EVENT");
        if (keyEvent == null || keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
            return;
        }

        int keyCode = keyEvent.getKeyCode();
        Log.d(TAG, "Rotary event: " + KeyEvent.keyCodeToString(keyCode));

        if (mainActivity == null) {
            Log.w(TAG, "MainActivity not available");
            return;
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_F5: // Clockwise
                mainActivity.handlePreviousFeed();
                break;
            case KeyEvent.KEYCODE_F4: // Counter-clockwise
                mainActivity.handleNextFeed();
                break;
            case KeyEvent.KEYCODE_F8: // Press
                mainActivity.handleTogglePlayStop();
                break;
        }
    }
}
