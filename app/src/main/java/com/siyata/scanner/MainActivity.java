package com.siyata.scanner;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements PTTWebSocketClient.PTTConnectionListener {
    private static final String TAG = "SiyataScanner";
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1;

    private List<RadioFeed> feeds;
    private int selectedIndex = 0;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private TextToSpeech tts;
    private boolean ttsReady = false;

    // PTT walkie-talkie components
    private PTTWebSocketClient pttClient;
    private PTTAudioManager pttAudioManager;
    private boolean isPTTMode = false;
    private boolean isPTTConnected = false;
    private boolean isPTTTransmitting = false;
    private long pttModeStartTime = 0;

    private TextView statusText;
    private TextView feedNameText;
    private TextView feedListText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.status_text);
        feedNameText = findViewById(R.id.feed_name_text);
        feedListText = findViewById(R.id.feed_list_text);

        setupFeeds();
        setupMediaPlayer();
        setupTTS();
        updateDisplay();

        Log.d(TAG, "Voice Scanner initialized with " + feeds.size() + " feeds");
    }

    @Override
    protected void onResume() {
        super.onResume();
        RotaryReceiver.mainActivity = this;
        if (ttsReady) {
            RadioFeed feed = feeds.get(selectedIndex);
            speak(feed.getName());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        RotaryReceiver.mainActivity = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RotaryReceiver.mainActivity = null;

        // Cleanup media player
        if (mediaPlayer != null) {
            if (isPlaying) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        // Cleanup PTT
        if (pttClient != null) {
            pttClient.disconnect();
            pttClient = null;
        }
        if (pttAudioManager != null) {
            pttAudioManager.release();
            pttAudioManager = null;
        }

        // Cleanup TTS
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
    
    private void setupTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "TTS language not supported");
                    ttsReady = false;
                } else {
                    ttsReady = true;
                    tts.setSpeechRate(1.1f);
                    speak("Scanner ready. " + feeds.size() + " feeds available. Use rotary knob to navigate.");
                }
            } else {
                Log.e(TAG, "TTS initialization failed");
                ttsReady = false;
            }
        });
    }
    
    private void speak(String text) {
        if (ttsReady && tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            Log.d(TAG, "Speaking: " + text);
        }
    }
    
    private void setupFeeds() {
        feeds = new ArrayList<>();

        // Try to load feeds from external file first
        if (!loadFeedsFromFile()) {
            // Fall back to default feeds
            Log.d(TAG, "Using default feeds");
            addDefaultFeeds();
        }
    }

    private boolean loadFeedsFromFile() {
        try {
            // Try app-specific directory first (no permissions needed)
            java.io.File feedsFile = new java.io.File(getExternalFilesDir(null), "scanner_feeds.txt");

            // Fall back to /sdcard root (requires permissions)
            if (!feedsFile.exists()) {
                feedsFile = new java.io.File("/sdcard/scanner_feeds.txt");
            }

            if (!feedsFile.exists()) {
                Log.d(TAG, "No custom feeds file found");
                return false;
            }

            Log.d(TAG, "Loading feeds from: " + feedsFile.getAbsolutePath());

            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.FileReader(feedsFile));
            String line;
            int count = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    String name = parts[0].trim();
                    String desc = parts[1].trim();
                    String url = parts[2].trim();
                    feeds.add(new RadioFeed(name, desc, url, count < 3));
                    count++;
                }
            }
            reader.close();

            if (feeds.size() > 0) {
                Log.d(TAG, "Loaded " + feeds.size() + " feeds from file");
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading feeds from file: " + e.getMessage());
        }
        return false;
    }

    private void addDefaultFeeds() {
        // DEFAULT FEEDS - UPDATE URLS HERE
        // Stream URL format: https://broadcastify.cdnstream1.com/[FEED_ID]
        // To find feed IDs:
        // 1. Go to broadcastify.com/listen/feed/[ID]
        // 2. Feed ID is in the URL
        // 3. Stream URL is: https://broadcastify.cdnstream1.com/[ID]
        //
        // NOTE: Free Broadcastify plays ads before streams
        // Premium removes ads: $6.99/month

        // Test feed (known working)
        feeds.add(new RadioFeed("Test Feed", "Working test stream",
            "https://broadcastify.cdnstream1.com/41192", true));

        // Omaha area feeds (URLs may need updating)
        feeds.add(new RadioFeed("Omaha Police Northeast", "Omaha PD Northeast Precinct",
            "https://broadcastify.cdnstream1.com/8466", true));
        feeds.add(new RadioFeed("Douglas County Sheriff", "Douglas County Sheriff Office",
            "https://broadcastify.cdnstream1.com/26983", true));
        feeds.add(new RadioFeed("Omaha Fire Department", "Omaha Fire and Rescue",
            "https://broadcastify.cdnstream1.com/3887", true));
        feeds.add(new RadioFeed("Council Bluffs Public Safety", "Council Bluffs Police and Fire",
            "https://broadcastify.cdnstream1.com/9228", false));
        feeds.add(new RadioFeed("Sarpy County Sheriff", "Sarpy County Sheriff Department",
            "https://broadcastify.cdnstream1.com/32102", false));
    }
    
    private void setupMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mediaPlayer.setOnPreparedListener(mp -> {
            mp.start();
            isPlaying = true;
            updateDisplay();
            RadioFeed feed = feeds.get(selectedIndex);
            updateOLED(feed.getName(), "Playing");
            Log.d(TAG, "▶ NOW PLAYING: " + feed.getName());
            speak("Stream connected");
        });

        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            Log.e(TAG, "MediaPlayer error: what=" + what + " extra=" + extra);
            isPlaying = false;
            RadioFeed feed = feeds.get(selectedIndex);
            updateOLED(feed.getName(), "Error");
            updateDisplay();
            speak("Error playing stream");
            return true;
        });

        mediaPlayer.setOnInfoListener((mp, what, extra) -> {
            Log.d(TAG, "MediaPlayer info: what=" + what + " extra=" + extra);
            return false;
        });
    }
    
    private void updateDisplay() {
        RadioFeed currentFeed = feeds.get(selectedIndex);
        
        if (isPlaying) {
            statusText.setText("▶ PLAYING");
            statusText.setTextColor(0xFF4CAF50);
        } else {
            statusText.setText("⏸ STOPPED");
            statusText.setTextColor(0xFFFF5722);
        }
        
        feedNameText.setText(currentFeed.getName());
        
        StringBuilder listBuilder = new StringBuilder();
        listBuilder.append("NEARBY FEEDS\n");
        listBuilder.append("────────────────\n\n");
        
        for (int i = 0; i < feeds.size(); i++) {
            if (i == selectedIndex) {
                listBuilder.append("▶ ");
            } else {
                listBuilder.append("  ");
            }
            listBuilder.append(feeds.get(i).getName());
            listBuilder.append("\n");
        }
        
        listBuilder.append("\n────────────────\n");
        listBuilder.append("ROTATE: Next/Prev\n");
        listBuilder.append("PRESS: Play/Stop");
        
        feedListText.setText(listBuilder.toString());
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "Key: " + keyCode + " (" + KeyEvent.keyCodeToString(keyCode) + ")");

        if (keyCode == KeyEvent.KEYCODE_F5 || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            nextFeed();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_F4 || keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            previousFeed();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_F8 || keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
            keyCode == KeyEvent.KEYCODE_ENTER) {

            // In PTT mode and connected, F8 down starts transmission
            if (isPTTConnected) {
                startPTTTransmission();
            } else {
                // Normal mode or not connected yet: toggle play/stop
                togglePlayStop();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // In PTT mode and connected, F8 release stops transmission
        if ((keyCode == KeyEvent.KEYCODE_F8 || keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
             keyCode == KeyEvent.KEYCODE_ENTER) && isPTTConnected) {
            stopPTTTransmission();
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    private void startPTTTransmission() {
        // Don't transmit if already transmitting, no audio manager, or not connected
        if (isPTTTransmitting || pttAudioManager == null || !isPTTConnected) {
            return;
        }

        // Prevent transmission in the same event frame as connection (500ms grace period)
        long timeSinceConnect = System.currentTimeMillis() - pttModeStartTime;
        if (timeSinceConnect < 500) {
            Log.d(TAG, "PTT transmission blocked - too soon after connect (" + timeSinceConnect + "ms)");
            return;
        }

        Log.d(TAG, "PTT transmission started");
        isPTTTransmitting = true;
        pttAudioManager.startTransmission();

        statusText.setText("📢 TRANSMITTING");
        statusText.setTextColor(0xFFFF5722); // Red for transmitting
        speak("Transmitting");
    }

    private void stopPTTTransmission() {
        if (!isPTTTransmitting || pttAudioManager == null) return;

        Log.d(TAG, "PTT transmission stopped");
        isPTTTransmitting = false;
        pttAudioManager.stopTransmission();

        statusText.setText("📻 PTT READY");
        statusText.setTextColor(0xFF2196F3); // Blue for ready
    }
    
    private void nextFeed() {
        selectedIndex = (selectedIndex + 1) % feeds.size();
        RadioFeed feed = feeds.get(selectedIndex);
        updateDisplay();
        updateOLED(feed.getName(), "Ready");
        speak(feed.getName());
        Log.d(TAG, "Next -> " + feed.getName());
    }
    
    private void previousFeed() {
        selectedIndex--;
        if (selectedIndex < 0) {
            selectedIndex = feeds.size() - 1;
        }
        RadioFeed feed = feeds.get(selectedIndex);
        updateDisplay();
        updateOLED(feed.getName(), "Ready");
        speak(feed.getName());
        Log.d(TAG, "Previous -> " + feed.getName());
    }
    
    private void togglePlayStop() {
        if (isPlaying) {
            stopPlayback();
            RadioFeed feed = feeds.get(selectedIndex);
            updateOLED(feed.getName(), "Stopped");
            speak("Stopped");
        } else {
            RadioFeed feed = feeds.get(selectedIndex);
            updateOLED(feed.getName(), "Playing");
            speak("Playing " + feed.getName());
            playSelected();
        }
    }

    public void handleNextFeed() {
        runOnUiThread(this::nextFeed);
    }

    public void handlePreviousFeed() {
        runOnUiThread(this::previousFeed);
    }

    public void handleTogglePlayStop() {
        runOnUiThread(this::togglePlayStop);
    }
    
    private void playSelected() {
        RadioFeed feed = feeds.get(selectedIndex);

        // Check if this is a PTT feed
        if (feed.isPTT()) {
            startPTTMode(feed);
        } else {
            startStreamMode(feed);
        }
    }

    private void startStreamMode(RadioFeed feed) {
        try {
            // Stop PTT if active
            stopPTTMode();

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();

            Log.d(TAG, "Starting stream playback: " + feed.getStreamUrl());

            mediaPlayer.setDataSource(feed.getStreamUrl());
            mediaPlayer.prepareAsync();

            statusText.setText("⏳ LOADING...");

        } catch (IOException e) {
            Log.e(TAG, "Error starting playback", e);
            speak("Cannot play feed");
            updateDisplay();
        }
    }

    private void startPTTMode(RadioFeed feed) {
        // Check microphone permission first
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.RECORD_AUDIO},
                        PERMISSION_REQUEST_RECORD_AUDIO);
                speak("Microphone permission required for walkie talkie");
                return;
            }
        }

        // Stop media player if active
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }

        isPTTMode = true;
        isPTTConnected = false;  // Not connected yet
        isPlaying = false;        // Don't set to true until connected
        pttModeStartTime = System.currentTimeMillis();  // Track when we started connecting

        String wsUrl = feed.getWebSocketUrl();
        String channel = feed.getChannelId();

        Log.d(TAG, "Starting PTT mode - URL: " + wsUrl + " Channel: " + channel);

        // Create PTT client if needed
        if (pttClient == null) {
            String screenName = "Siyata" + (int)(Math.random() * 1000);
            pttClient = new PTTWebSocketClient(wsUrl, screenName, this);
            pttAudioManager = new PTTAudioManager(pttClient);
        }

        // Connect and join channel
        pttClient.connect();
        pttClient.joinChannel(channel);

        statusText.setText("⏳ CONNECTING...");
        statusText.setTextColor(0xFFFFA500); // Orange for connecting
        updateDisplay();

        speak("Connecting to walkie talkie channel " + channel);
    }

    private void stopPTTMode() {
        if (!isPTTMode) return;

        Log.d(TAG, "Stopping PTT mode");

        if (pttAudioManager != null) {
            pttAudioManager.stopTransmission();
            pttAudioManager.stopPlayback();
        }

        if (pttClient != null) {
            pttClient.disconnect();
        }

        isPTTMode = false;
        isPTTConnected = false;
        isPTTTransmitting = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                RadioFeed feed = feeds.get(selectedIndex);
                if (feed.isPTT()) {
                    startPTTMode(feed);
                }
            } else {
                Toast.makeText(this, "Microphone permission required for PTT", Toast.LENGTH_LONG).show();
                speak("Permission denied");
            }
        }
    }
    
    private void stopPlayback() {
        if (isPTTMode) {
            stopPTTMode();
        } else {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
        }
        isPlaying = false;
        updateDisplay();
        Log.d(TAG, "Playback stopped");
    }

    private void updateOLED(String feedName, String status) {
        try {
            Intent oledIntent = new Intent("com.mcx.intent.action.PTT__Kodiak");
            oledIntent.putExtra("displayName", feedName);
            oledIntent.putExtra("status", status);
            oledIntent.putExtra("name", feedName);
            oledIntent.putExtra("username", feedName);
            sendBroadcast(oledIntent);
            Log.d(TAG, "OLED update: " + feedName + " - " + status);
        } catch (Exception e) {
            Log.e(TAG, "OLED update failed: " + e.getMessage());
        }
    }

    // PTTConnectionListener interface implementation

    @Override
    public void onConnected() {
        runOnUiThread(() -> {
            Log.d(TAG, "PTT WebSocket connected");

            // Now we're fully connected and ready
            isPTTConnected = true;
            isPlaying = true;

            statusText.setText("📻 PTT READY");
            statusText.setTextColor(0xFF2196F3); // Blue for ready
            updateDisplay();

            RadioFeed feed = feeds.get(selectedIndex);
            updateOLED(feed.getName(), "Ready");
            speak("Connected. Hold button to talk.");
        });
    }

    @Override
    public void onDisconnected() {
        runOnUiThread(() -> {
            Log.d(TAG, "PTT WebSocket disconnected");
            if (isPTTMode) {
                statusText.setText("❌ DISCONNECTED");
                statusText.setTextColor(0xFFFF5722);
                speak("Connection lost");
            }
        });
    }

    @Override
    public void onAudioStart(String screenName) {
        runOnUiThread(() -> {
            Log.d(TAG, "Receiving audio from: " + screenName);

            // Start playback if not already playing
            if (pttAudioManager != null && !pttAudioManager.isPlaying()) {
                pttAudioManager.startPlayback();
            }

            statusText.setText("📻 " + screenName + " TALKING");
            statusText.setTextColor(0xFF4CAF50); // Green for receiving
            speak(screenName + " is talking");

            RadioFeed feed = feeds.get(selectedIndex);
            updateOLED(feed.getName(), screenName + " talking");
        });
    }

    @Override
    public void onAudioData(byte[] pcmData) {
        // Queue audio data for playback
        if (pttAudioManager != null) {
            pttAudioManager.queueAudioData(pcmData);
        }
    }

    @Override
    public void onAudioEnd() {
        runOnUiThread(() -> {
            Log.d(TAG, "Audio transmission ended");

            if (pttAudioManager != null) {
                pttAudioManager.stopPlayback();
            }

            statusText.setText("📻 PTT READY");
            statusText.setTextColor(0xFF2196F3);

            RadioFeed feed = feeds.get(selectedIndex);
            updateOLED(feed.getName(), "Ready");
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            Log.e(TAG, "PTT error: " + error);
            Toast.makeText(this, "PTT Error: " + error, Toast.LENGTH_SHORT).show();
            speak("Connection error");
        });
    }
}
