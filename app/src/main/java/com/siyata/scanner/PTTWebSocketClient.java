package com.siyata.scanner;

import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;

public class PTTWebSocketClient extends WebSocketListener {
    private static final String TAG = "PTTWebSocketClient";

    private OkHttpClient client;
    private WebSocket webSocket;
    private String serverUrl;
    private String screenName;
    private String currentChannel;
    private boolean isConnected = false;
    private PTTConnectionListener listener;

    public interface PTTConnectionListener {
        void onConnected();
        void onDisconnected();
        void onAudioStart(String screenName);
        void onAudioData(byte[] pcmData);
        void onAudioEnd();
        void onError(String error);
    }

    public PTTWebSocketClient(String serverUrl, String screenName, PTTConnectionListener listener) {
        this.serverUrl = serverUrl;
        this.screenName = screenName;
        this.listener = listener;

        this.client = new OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build();
    }

    public void connect() {
        Request request = new Request.Builder()
            .url(serverUrl)
            .build();

        webSocket = client.newWebSocket(request, this);
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Client disconnect");
            webSocket = null;
        }
        isConnected = false;
    }

    public void joinChannel(String channel) {
        this.currentChannel = channel;
        sendMessage("join_channel", channel);
    }

    public void startTransmission() {
        if (!isConnected) {
            Log.w(TAG, "Cannot start transmission: not connected");
            return;
        }

        try {
            JSONObject msg = new JSONObject();
            msg.put("type", "audio_start");
            msg.put("channel", currentChannel);
            msg.put("sample_rate", 48000);

            webSocket.send(msg.toString());
            Log.d(TAG, "Started transmission on channel " + currentChannel);
        } catch (JSONException e) {
            Log.e(TAG, "Error starting transmission", e);
        }
    }

    public void sendAudioData(byte[] pcmData) {
        if (!isConnected) return;

        try {
            String base64Audio = android.util.Base64.encodeToString(
                pcmData, android.util.Base64.NO_WRAP);

            JSONObject msg = new JSONObject();
            msg.put("type", "audio_data");
            msg.put("channel", currentChannel);
            msg.put("data", base64Audio);
            msg.put("format", "pcm16");
            msg.put("sampleRate", 48000);
            msg.put("channels", 1);

            webSocket.send(msg.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Error sending audio data", e);
        }
    }

    public void endTransmission() {
        if (!isConnected) return;

        try {
            JSONObject msg = new JSONObject();
            msg.put("type", "audio_end");
            msg.put("channel", currentChannel);

            webSocket.send(msg.toString());
            Log.d(TAG, "Ended transmission");
        } catch (JSONException e) {
            Log.e(TAG, "Error ending transmission", e);
        }
    }

    private void sendMessage(String type, String channel) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("type", type);
            msg.put("channel", channel);

            if (webSocket != null) {
                webSocket.send(msg.toString());
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error sending message", e);
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        Log.d(TAG, "WebSocket connected");
        isConnected = true;

        // Send authentication/screen name
        try {
            JSONObject auth = new JSONObject();
            auth.put("type", "set_screen_name");
            auth.put("screen_name", screenName);

            webSocket.send(auth.toString());

            if (listener != null) {
                listener.onConnected();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error sending auth", e);
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        try {
            JSONObject msg = new JSONObject(text);
            String type = msg.getString("type");

            Log.d(TAG, "Received message type: " + type);

            switch (type) {
                case "audio_start":
                    if (listener != null) {
                        String sender = msg.optString("screen_name", "Unknown");
                        listener.onAudioStart(sender);
                    }
                    break;

                case "audio_data":
                    if (listener != null) {
                        String base64Data = msg.getString("data");
                        byte[] pcmData = android.util.Base64.decode(
                            base64Data, android.util.Base64.NO_WRAP);
                        listener.onAudioData(pcmData);
                    }
                    break;

                case "audio_end":
                    if (listener != null) {
                        listener.onAudioEnd();
                    }
                    break;

                case "participants":
                    int count = msg.getInt("count");
                    Log.d(TAG, "Participants: " + count);
                    break;
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing message", e);
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Log.e(TAG, "WebSocket error", t);
        isConnected = false;

        if (listener != null) {
            listener.onError(t.getMessage());
            listener.onDisconnected();
        }
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        Log.d(TAG, "WebSocket closed: " + reason);
        isConnected = false;

        if (listener != null) {
            listener.onDisconnected();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }
}
