package com.siyata.scanner.ai;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Manages audio for AI push-to-talk interactions.
 * Handles recording, speech recognition, TTS playback, and radio chirps.
 */
public class AIAudioManager {
    private static final String TAG = "AIAudioManager";
    private static final int SAMPLE_RATE = 16000; // 16kHz for speech
    private static final int CHANNEL_CONFIG_IN = AudioFormat.CHANNEL_IN_MONO;
    private static final int CHANNEL_CONFIG_OUT = AudioFormat.CHANNEL_OUT_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int CHUNK_SIZE = 4096;

    private Context context;
    private AudioManager audioManager;
    private AudioRecord audioRecord;
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private boolean isRecording = false;
    private ByteArrayOutputStream recordingBuffer;
    private Handler handler;
    private AIConfig config;

    private AudioListener listener;

    public interface AudioListener {
        void onRecordingStarted();
        void onRecordingData(byte[] audioData, int length);
        void onRecordingStopped();
        void onTranscriptionResult(String text);
        void onTranscriptionError(String error);
        void onSpeechStarted();
        void onSpeechCompleted();
        void onError(String error);
    }

    public AIAudioManager(Context context, AudioListener listener) {
        this.context = context;
        this.listener = listener;
        this.handler = new Handler(Looper.getMainLooper());
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.config = AIConfig.getInstance(context);

        initializeTTS();
        initializeSpeechRecognizer();
    }

    private void initializeTTS() {
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                if (result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    ttsReady = true;
                    tts.setSpeechRate(1.0f);

                    // Set audio attributes to use loudspeaker
                    AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build();
                    tts.setAudioAttributes(audioAttributes);

                    // Set utterance progress listener
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            handler.post(() -> listener.onSpeechStarted());
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            handler.post(() -> listener.onSpeechCompleted());
                        }

                        @Override
                        public void onError(String utteranceId) {
                            handler.post(() -> listener.onError("TTS error"));
                        }
                    });

                    Log.d(TAG, "TTS initialized successfully");
                } else {
                    Log.e(TAG, "TTS language not supported");
                    ttsReady = false;
                }
            } else {
                Log.e(TAG, "TTS initialization failed");
                ttsReady = false;
            }
        });
    }

    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    Log.d(TAG, "Speech recognizer ready");
                }

                @Override
                public void onBeginningOfSpeech() {
                    Log.d(TAG, "Speech started");
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                    // Audio level monitoring
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                    // Audio data received
                }

                @Override
                public void onEndOfSpeech() {
                    Log.d(TAG, "Speech ended");
                }

                @Override
                public void onError(int error) {
                    String errorMessage = getSpeechRecognizerError(error);
                    Log.e(TAG, "Speech recognition error: " + errorMessage);
                    handler.post(() -> listener.onTranscriptionError(errorMessage));
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String transcription = matches.get(0);
                        Log.d(TAG, "Transcription: " + transcription);
                        handler.post(() -> listener.onTranscriptionResult(transcription));
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    // Partial results available
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                    // Reserved for future use
                }
            });
            Log.d(TAG, "Speech recognizer initialized");
        } else {
            Log.w(TAG, "Speech recognition not available");
        }
    }

    /**
     * Start recording audio for AI PTT
     * Plays open chirp and begins recording
     */
    public void startRecording() {
        if (isRecording) {
            Log.w(TAG, "Already recording");
            return;
        }

        try {
            // Request audio focus
            requestAudioFocus();

            // Play radio open chirp
            if (config.isRadioChirpsEnabled()) {
                playChirp(true);
            }

            // Initialize recording buffer
            recordingBuffer = new ByteArrayOutputStream();

            // Start microphone recording
            int bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG_IN,
                AUDIO_FORMAT
            );

            audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG_IN,
                AUDIO_FORMAT,
                bufferSize
            );

            audioRecord.startRecording();
            isRecording = true;

            handler.post(() -> listener.onRecordingStarted());

            // Start recording thread
            new Thread(() -> {
                byte[] buffer = new byte[CHUNK_SIZE];

                while (isRecording) {
                    int bytesRead = audioRecord.read(buffer, 0, buffer.length);

                    if (bytesRead > 0) {
                        // Store in buffer for potential transcription
                        recordingBuffer.write(buffer, 0, bytesRead);

                        // Notify listener
                        byte[] chunk = new byte[bytesRead];
                        System.arraycopy(buffer, 0, chunk, 0, bytesRead);
                        handler.post(() -> listener.onRecordingData(chunk, bytesRead));
                    }
                }

                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;

                Log.d(TAG, "Recording stopped");

            }, "AI-Recording-Thread").start();

            Log.d(TAG, "Started AI recording");

        } catch (Exception e) {
            Log.e(TAG, "Error starting recording", e);
            isRecording = false;
            handler.post(() -> listener.onError("Failed to start recording: " + e.getMessage()));
        }
    }

    /**
     * Stop recording and transcribe
     */
    public void stopRecording() {
        if (!isRecording) {
            return;
        }

        isRecording = false;

        // Play close chirp
        if (config.isRadioChirpsEnabled()) {
            handler.postDelayed(() -> playChirp(false), 100);
        }

        handler.post(() -> listener.onRecordingStopped());

        // Start transcription
        byte[] audioData = recordingBuffer.toByteArray();
        if (audioData.length > 0) {
            transcribeAudio(audioData);
        } else {
            handler.post(() -> listener.onTranscriptionError("No audio recorded"));
        }

        Log.d(TAG, "Stopped AI recording, audio size: " + audioData.length);
    }

    /**
     * Transcribe recorded audio to text
     */
    private void transcribeAudio(byte[] audioData) {
        if (speechRecognizer != null) {
            // Use Android SpeechRecognizer (works offline)
            android.content.Intent intent = new android.content.Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

            speechRecognizer.startListening(intent);
        } else {
            handler.post(() -> listener.onTranscriptionError(
                "Speech recognition not available"));
        }
    }

    /**
     * Speak text response using TTS
     */
    public void speak(String text) {
        if (!ttsReady) {
            handler.post(() -> listener.onError("TTS not ready"));
            return;
        }

        // Request audio focus
        requestAudioFocus();

        // Play receive chirp before speaking
        if (config.isRadioChirpsEnabled()) {
            playChirp(false);
        }

        // Speak text
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ai_response");

        // Use loudspeaker
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setSpeakerphoneOn(true);

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params);

        Log.d(TAG, "Speaking: " + text.substring(0, Math.min(50, text.length())) + "...");
    }

    /**
     * Play radio-style chirp (open or close)
     */
    private void playChirp(boolean isOpen) {
        try {
            ToneGenerator toneGen = new ToneGenerator(
                AudioManager.STREAM_MUSIC, 80);

            if (isOpen) {
                // Open chirp: higher frequency, short duration
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 100);
            } else {
                // Close chirp: lower frequency, short duration
                toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 80);
            }

            handler.postDelayed(toneGen::release, 200);
        } catch (Exception e) {
            Log.e(TAG, "Error playing chirp", e);
        }
    }

    /**
     * Request audio focus for AI interaction
     */
    private void requestAudioFocus() {
        AudioManager.OnAudioFocusChangeListener focusChangeListener =
            focusChange -> {
                Log.d(TAG, "Audio focus changed: " + focusChange);
            };

        int result = audioManager.requestAudioFocus(
            focusChangeListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
        );

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d(TAG, "Audio focus granted");
        }
    }

    /**
     * Stop any ongoing TTS
     */
    public void stopSpeaking() {
        if (tts != null && ttsReady) {
            tts.stop();
        }
    }

    /**
     * Check if currently recording
     */
    public boolean isRecording() {
        return isRecording;
    }

    /**
     * Check if TTS is ready
     */
    public boolean isTTSReady() {
        return ttsReady;
    }

    /**
     * Release resources
     */
    public void release() {
        if (isRecording) {
            stopRecording();
        }

        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }

        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }

        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }

        Log.d(TAG, "AI audio manager released");
    }

    private String getSpeechRecognizerError(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No speech match";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Recognition service busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech input";
            default:
                return "Unknown error";
        }
    }
}
