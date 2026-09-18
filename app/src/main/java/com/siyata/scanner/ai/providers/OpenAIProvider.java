package com.siyata.scanner.ai.providers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.siyata.scanner.ai.AIAgent;
import com.siyata.scanner.ai.ConversationMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * OpenAI API provider implementation
 * Uses GPT-4 for chat, Whisper for transcription, TTS for speech
 */
public class OpenAIProvider implements AIProviderInterface {
    private static final String TAG = "OpenAIProvider";

    // OpenAI API endpoints
    private static final String API_BASE = "https://api.openai.com/v1";
    private static final String CHAT_ENDPOINT = API_BASE + "/chat/completions";
    private static final String WHISPER_ENDPOINT = API_BASE + "/audio/transcriptions";
    private static final String TTS_ENDPOINT = API_BASE + "/audio/speech";

    private static final String PREFS_NAME = "openai_config";
    private static final String KEY_API_KEY = "api_key";

    private final OkHttpClient client;
    private final Context context;
    private String apiKey;
    private Call currentCall;

    public OpenAIProvider(Context context) {
        this.context = context.getApplicationContext();
        this.client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build();

        loadApiKey();
    }

    private void loadApiKey() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        apiKey = prefs.getString(KEY_API_KEY, null);
    }

    public void setApiKey(String key) {
        this.apiKey = key;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_API_KEY, key).apply();
    }

    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }

    @Override
    public void sendMessage(
        AIAgent agent,
        List<ConversationMessage> conversationHistory,
        String userMessage,
        ResponseCallback callback
    ) {
        if (!isConfigured()) {
            callback.onError("OpenAI API key not configured");
            return;
        }

        try {
            // Build messages array
            JSONArray messages = new JSONArray();

            // Add system prompt
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", agent.getSystemPrompt());
            messages.put(systemMsg);

            // Add conversation history
            for (ConversationMessage msg : conversationHistory) {
                if (msg.getRole() != ConversationMessage.Role.SYSTEM) {
                    JSONObject historyMsg = new JSONObject();
                    historyMsg.put("role", msg.getRole().getApiValue());
                    historyMsg.put("content", msg.getContent());
                    messages.put(historyMsg);
                }
            }

            // Add new user message
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.put(userMsg);

            // Build request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", agent.getModel());
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 500); // Limit for voice responses
            requestBody.put("temperature", 0.7);

            Log.d(TAG, "Sending chat request to OpenAI: " + userMessage);

            Request request = new Request.Builder()
                .url(CHAT_ENDPOINT)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(
                    requestBody.toString(),
                    MediaType.parse("application/json")
                ))
                .build();

            currentCall = client.newCall(request);
            currentCall.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Chat request failed", e);
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();

                    if (!response.isSuccessful()) {
                        Log.e(TAG, "Chat request error: " + responseBody);
                        callback.onError("API error: " + response.code());
                        return;
                    }

                    try {
                        JSONObject json = new JSONObject(responseBody);
                        JSONArray choices = json.getJSONArray("choices");
                        if (choices.length() > 0) {
                            JSONObject choice = choices.getJSONObject(0);
                            JSONObject message = choice.getJSONObject("message");
                            String content = message.getString("content");

                            Log.d(TAG, "Received response: " + content.substring(0, Math.min(100, content.length())) + "...");
                            callback.onSuccess(content);
                        } else {
                            callback.onError("No response from AI");
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing response", e);
                        callback.onError("Failed to parse response");
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error building request", e);
            callback.onError("Failed to build request");
        }
    }

    @Override
    public void transcribeAudio(
        byte[] audioData,
        int sampleRate,
        TranscriptionCallback callback
    ) {
        if (!isConfigured()) {
            callback.onError("OpenAI API key not configured");
            return;
        }

        // TODO: Implement Whisper API transcription
        // For now, use Android's SpeechRecognizer as fallback
        callback.onError("Audio transcription not yet implemented");
    }

    @Override
    public void textToSpeech(
        String text,
        String voice,
        TTSCallback callback
    ) {
        if (!isConfigured()) {
            callback.onError("OpenAI API key not configured");
            return;
        }

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "tts-1");
            requestBody.put("input", text);
            requestBody.put("voice", voice != null ? voice : "alloy");
            requestBody.put("response_format", "pcm"); // Get raw PCM for direct playback

            Log.d(TAG, "Sending TTS request to OpenAI");

            Request request = new Request.Builder()
                .url(TTS_ENDPOINT)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(
                    requestBody.toString(),
                    MediaType.parse("application/json")
                ))
                .build();

            currentCall = client.newCall(request);
            currentCall.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "TTS request failed", e);
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body().string();
                        Log.e(TAG, "TTS request error: " + errorBody);
                        callback.onError("API error: " + response.code());
                        return;
                    }

                    byte[] audioData = response.body().bytes();
                    Log.d(TAG, "Received TTS audio: " + audioData.length + " bytes");
                    callback.onSuccess(audioData);
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error building TTS request", e);
            callback.onError("Failed to build request");
        }
    }

    @Override
    public void cancelRequests() {
        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
            Log.d(TAG, "Cancelled ongoing request");
        }
    }
}
