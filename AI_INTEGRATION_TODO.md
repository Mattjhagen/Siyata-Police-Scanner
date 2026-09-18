# AI Agent Integration - Remaining Work

## Status: Phase 4 In Progress

### ✅ Completed
- **Phase 1:** Agent data model (AIAgent, ConversationMessage, AgentManager)
- **Phase 2:** Provider layer (OpenAIProvider, AIConfig)
- **Phase 3:** Voice pipeline (AIAudioManager with chirps, TTS, STT)
- **Phase 4 Partial:** RadioFeed updated to support AI_AGENT type
- **Feeds:** AI agents added to scanner_feeds.txt

### 🚧 Remaining: MainActivity Integration

#### 1. Add AI Components to MainActivity

```java
// Add to MainActivity fields:
private AgentManager agentManager;
private AIAudioManager aiAudioManager;
private OpenAIProvider aiProvider;
private AIAgent currentAgent;
private boolean isAIMode = false;
private long f8PressTime = 0;
private static final long LONG_PRESS_THRESHOLD = 300; // ms
```

#### 2. Initialize in onCreate()

```java
// Initialize AI system
agentManager = AgentManager.getInstance(this);
aiProvider = new OpenAIProvider(this);

// Map agent IDs from feeds to actual agents
// When loading feeds, match ai://agentId to agent names
```

#### 3. Update onKeyDown for Long Press Detection

```java
@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_F8) {
        f8PressTime = System.currentTimeMillis();
        
        // If in AI mode and connected, start recording
        if (isAIMode && aiAudioManager != null) {
            aiAudioManager.startRecording();
            statusText.setText("🎤 LISTENING...");
            return true;
        }
    }
    // ... rest of existing key handling
}
```

#### 4. Update onKeyUp for Long Press vs Short Press

```java
@Override
public boolean onKeyUp(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_F8) {
        long pressDuration = System.currentTimeMillis() - f8PressTime;
        
        if (isAIMode && aiAudioManager != null && aiAudioManager.isRecording()) {
            // Stop AI recording
            aiAudioManager.stopRecording();
            statusText.setText("🤔 THINKING...");
            return true;
        }
        
        // Short press - select/toggle
        if (pressDuration < LONG_PRESS_THRESHOLD) {
            // Existing toggle behavior
        }
        return true;
    }
    // ... existing PTT handling
}
```

#### 5. Add AI Mode Activation

```java
private void startAIMode(RadioFeed feed) {
    String agentId = feed.getAgentId();
    
    // Find agent by matching name (feeds use simplified IDs)
    for (AIAgent agent : agentManager.getAgents()) {
        String simpleName = agent.getName().toLowerCase().replace(" ", "");
        if (agentId.contains(simpleName) || simpleName.contains(agentId)) {
            currentAgent = agent;
            break;
        }
    }
    
    if (currentAgent == null) {
        speak("Agent not found");
        return;
    }
    
    // Check API key
    if (!aiProvider.isConfigured()) {
        speak("API key not configured. Check settings.");
        return;
    }
    
    // Initialize AI audio manager
    aiAudioManager = new AIAudioManager(this, new AIAudioManager.AudioListener() {
        @Override
        public void onRecordingStarted() {
            runOnUiThread(() -> {
                statusText.setText("🎤 LISTENING...");
                feedNameText.setText(currentAgent.getName());
            });
        }
        
        @Override
        public void onRecordingData(byte[] audioData, int length) {
            // Audio data being recorded
        }
        
        @Override
        public void onRecordingStopped() {
            runOnUiThread(() -> {
                statusText.setText("🤔 THINKING...");
            });
        }
        
        @Override
        public void onTranscriptionResult(String text) {
            Log.d(TAG, "User said: " + text);
            
            // Add user message to conversation
            ConversationMessage userMsg = new ConversationMessage(
                currentAgent.getId(),
                ConversationMessage.Role.USER,
                text
            );
            agentManager.addMessage(currentAgent.getId(), userMsg);
            
            // Send to AI
            aiProvider.sendMessage(
                currentAgent,
                agentManager.getConversation(currentAgent.getId()),
                text,
                new AIProviderInterface.ResponseCallback() {
                    @Override
                    public void onSuccess(String responseText) {
                        // Add AI response to conversation
                        ConversationMessage aiMsg = new ConversationMessage(
                            currentAgent.getId(),
                            ConversationMessage.Role.ASSISTANT,
                            responseText
                        );
                        agentManager.addMessage(currentAgent.getId(), aiMsg);
                        
                        // Speak response
                        runOnUiThread(() -> {
                            statusText.setText("📻 " + currentAgent.getName().toUpperCase());
                            feedListText.setText(responseText);
                            aiAudioManager.speak(responseText);
                        });
                    }
                    
                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            statusText.setText("❌ ERROR");
                            speak("Error: " + error);
                        });
                    }
                }
            );
        }
        
        @Override
        public void onTranscriptionError(String error) {
            runOnUiThread(() -> {
                statusText.setText("❌ ERROR");
                speak("Could not understand. Try again.");
            });
        }
        
        @Override
        public void onSpeechStarted() {
            // AI started speaking
        }
        
        @Override
        public void onSpeechCompleted() {
            runOnUiThread(() -> {
                statusText.setText("✅ READY");
                speak("Ready");
            });
        }
        
        @Override
        public void onError(String error) {
            runOnUiThread(() -> {
                statusText.setText("❌ ERROR");
                speak(error);
            });
        }
    });
    
    isAIMode = true;
    isPlaying = true;
    
    statusText.setText("✅ READY");
    statusText.setTextColor(0xFF4CAF50);
    feedNameText.setText(currentAgent.getName());
    
    // Show instructions
    StringBuilder instructions = new StringBuilder();
    instructions.append("AI AGENT MODE\n");
    instructions.append("────────────────\n\n");
    instructions.append(currentAgent.getDescription()).append("\n\n");
    instructions.append("HOLD F8 to talk\n");
    instructions.append("RELEASE to send\n\n");
    
    if (agentManager.hasConversation(currentAgent.getId())) {
        ConversationMessage lastMsg = agentManager.getLastMessage(currentAgent.getId());
        if (lastMsg != null) {
            instructions.append("Last:\n");
            instructions.append(lastMsg.getPreview(200));
        }
    }
    
    feedListText.setText(instructions.toString());
    speak("Connected to " + currentAgent.getName() + ". Hold button to talk.");
}
```

#### 6. Update playSelected() to Handle AI Agents

```java
private void playSelected() {
    RadioFeed feed = feeds.get(selectedIndex);
    
    // Check feed type
    if (feed.isAIAgent()) {
        startAIMode(feed);
    } else if (feed.isPTT()) {
        startPTTMode(feed);
    } else {
        startStreamMode(feed);
    }
}
```

#### 7. Update stopPlayback() to Handle AI Mode

```java
private void stopPlayback() {
    if (isAIMode) {
        stopAIMode();
    } else if (isPTTMode) {
        stopPTTMode();
    } else {
        // Existing stream stop logic
    }
    isPlaying = false;
    updateDisplay();
}

private void stopAIMode() {
    if (!isAIMode) return;
    
    if (aiAudioManager != null) {
        aiAudioManager.release();
        aiAudioManager = null;
    }
    
    isAIMode = false;
    currentAgent = null;
}
```

#### 8. Add RECORD_AUDIO Permission Handling

Already added to manifest. Just need runtime check in startAIMode():

```java
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                PERMISSION_REQUEST_RECORD_AUDIO);
        return;
    }
}
```

### 📝 Testing Checklist

1. **Build APK:**
   ```bash
   export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-21.jdk/Contents/Home
   gradle assembleDebug
   ```

2. **Install:**
   ```bash
   adb -s 182d9513 install -r app/build/outputs/apk/debug/app-debug.apk
   ```

3. **Test AI Mode:**
   - Rotate to "ChatGPT" feed
   - Press F8 short → Should enter AI mode ("READY")
   - Hold F8 → Should show "LISTENING" with chirp
   - Speak: "What's the weather like?"
   - Release F8 → Should show "THINKING"
   - AI response should play through loudspeaker
   - Should return to "READY"

4. **Test Long Press Detection:**
   - Verify short press (<300ms) = toggle mode
   - Verify long press (≥300ms) = AI PTT

5. **Check Logs:**
   ```bash
   adb logcat -s SiyataScanner AIAudioManager OpenAIProvider AgentManager
   ```

### 🔧 API Key Configuration

Create simple method to set OpenAI key from adb:

```bash
adb shell "am broadcast -a com.siyata.scanner.SET_OPENAI_KEY --es key 'sk-proj-...'"
```

Or add to onCreate for testing:
```java
if (BuildConfig.DEBUG) {
    AIConfig.getInstance(this).setOpenAIApiKey("sk-proj-YOUR_KEY_HERE");
}
```

### 🚀 Future Enhancements

- Settings activity for API key input
- Agent customization UI
- Conversation history viewer
- Custom agent creation
- Tool integration (Home Lab agent with SSH commands)
- OpenAI Whisper for better transcription
- OpenAI Realtime API for lower latency

### 📚 Dependencies Already Added

- OkHttp (for API calls) ✅
- Android SpeechRecognizer ✅
- Android TextToSpeech ✅
- All permissions ✅

No additional dependencies needed!
