package com.siyata.scanner;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String TAG = "SiyataScanner";
    
    private List<RadioFeed> feeds;
    private int selectedIndex = 0;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    
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
        if (mediaPlayer != null) {
            if (isPlaying) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
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
            togglePlayStop();
            return true;
        }
        
        return super.onKeyDown(keyCode, event);
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
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            
            RadioFeed feed = feeds.get(selectedIndex);
            Log.d(TAG, "Starting playback: " + feed.getStreamUrl());
            
            mediaPlayer.setDataSource(feed.getStreamUrl());
            mediaPlayer.prepareAsync();
            
            statusText.setText("⏳ LOADING...");
            
        } catch (IOException e) {
            Log.e(TAG, "Error starting playback", e);
            speak("Cannot play feed");
            updateDisplay();
        }
    }
    
    private void stopPlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
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
}
