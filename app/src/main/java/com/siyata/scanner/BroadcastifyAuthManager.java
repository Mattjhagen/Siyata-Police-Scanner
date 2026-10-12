package com.siyata.scanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages Broadcastify authentication and automatic stream URL refresh
 */
public class BroadcastifyAuthManager {
    private static final String TAG = "BroadcastifyAuth";
    private static final String PREFS_NAME = "broadcastify_auth";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";

    private final Context context;
    private final OkHttpClient httpClient;
    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
    private boolean isAuthenticated = false;

    public BroadcastifyAuthManager(Context context) {
        this.context = context;

        // Create HTTP client with cookie management
        this.httpClient = new OkHttpClient.Builder()
            .cookieJar(new CookieJar() {
                @Override
                public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                    cookieStore.put(url.host(), cookies);
                }

                @Override
                public List<Cookie> loadForRequest(HttpUrl url) {
                    List<Cookie> cookies = cookieStore.get(url.host());
                    return cookies != null ? cookies : new ArrayList<>();
                }
            })
            .build();
    }

    /**
     * Save login credentials
     */
    public void saveCredentials(String username, String password) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
            .putString(PREF_USERNAME, username)
            .putString(PREF_PASSWORD, password)
            .apply();
    }

    /**
     * Authenticate with Broadcastify using saved credentials
     */
    public boolean authenticate() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String username = prefs.getString(PREF_USERNAME, null);
        String password = prefs.getString(PREF_PASSWORD, null);

        if (username == null || password == null) {
            Log.w(TAG, "No saved credentials");
            return false;
        }

        return authenticate(username, password);
    }

    /**
     * Authenticate with Broadcastify
     */
    public boolean authenticate(String username, String password) {
        try {
            Log.d(TAG, "Authenticating with Broadcastify...");

            // Build login form
            FormBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("action", "auth")
                .build();

            // POST to login endpoint
            Request request = new Request.Builder()
                .url("https://www.broadcastify.com/login/")
                .post(formBody)
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    isAuthenticated = true;
                    Log.d(TAG, "Authentication successful");
                    return true;
                } else {
                    Log.e(TAG, "Authentication failed: " + response.code());
                    return false;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Authentication error", e);
            return false;
        }
    }

    /**
     * Fetch fresh HLS stream URL for a given feed ID
     */
    public String getFreshStreamUrl(int feedId) {
        if (!isAuthenticated && !authenticate()) {
            Log.w(TAG, "Not authenticated, cannot fetch stream URL");
            return null;
        }

        try {
            // Try the listen page API endpoint which returns stream URLs
            String listenUrl = "https://www.broadcastify.com/listen/feed/" + feedId;
            Request request = new Request.Builder()
                .url(listenUrl)
                .addHeader("Accept", "text/html,application/xhtml+xml")
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Failed to fetch feed page: " + response.code());
                    return null;
                }

                String html = response.body().string();
                Log.d(TAG, "Fetched page for feed " + feedId + ", length: " + html.length());

                // Try multiple patterns to find the stream URL
                // Pattern 1: Direct HLS URL in JavaScript
                Pattern pattern1 = Pattern.compile("(https://[^\\s\"'<>]+hls[^\\s\"'<>]+playlist\\.m3u8[^\\s\"'<>]*)");
                Matcher matcher1 = pattern1.matcher(html);
                if (matcher1.find()) {
                    String streamUrl = matcher1.group(1).replaceAll("[\"']", "");
                    Log.d(TAG, "Found stream URL (pattern 1) for feed " + feedId);
                    return streamUrl;
                }

                // Pattern 2: Look for any m3u8 URL
                Pattern pattern2 = Pattern.compile("(https://[^\\s\"'<>]+\\.m3u8[^\\s\"'<>]*)");
                Matcher matcher2 = pattern2.matcher(html);
                if (matcher2.find()) {
                    String streamUrl = matcher2.group(1).replaceAll("[\"']", "");
                    Log.d(TAG, "Found stream URL (pattern 2) for feed " + feedId);
                    return streamUrl;
                }

                // Pattern 3: Look in data attributes
                Pattern pattern3 = Pattern.compile("data-stream[^=]*=[\"']([^\"']+)[\"']");
                Matcher matcher3 = pattern3.matcher(html);
                if (matcher3.find()) {
                    String streamUrl = matcher3.group(1);
                    Log.d(TAG, "Found stream URL (pattern 3) for feed " + feedId);
                    return streamUrl;
                }

                Log.w(TAG, "No stream URL found for feed " + feedId + " - HTML snippet: " + html.substring(0, Math.min(500, html.length())));
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error fetching stream URL", e);
            return null;
        }
    }

    /**
     * Extract feed ID from a Broadcastify URL
     */
    public static Integer extractFeedId(String url) {
        if (url == null || !url.contains("broadcastify.com")) {
            return null;
        }

        // Extract feed ID from URL like: .../feed/39790/playlist.m3u8
        Pattern pattern = Pattern.compile("/feed/(\\d+)/");
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    /**
     * Get the OkHttpClient with cookies for ExoPlayer
     */
    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }
}
