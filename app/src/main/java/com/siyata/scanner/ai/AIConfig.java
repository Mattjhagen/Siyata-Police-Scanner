package com.siyata.scanner.ai;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages AI configuration including API keys and settings.
 * Provides secure storage for sensitive credentials.
 */
public class AIConfig {
    private static final String PREFS_NAME = "ai_config";
    private static final String KEY_OPENAI_API_KEY = "openai_api_key";
    private static final String KEY_ANTHROPIC_API_KEY = "anthropic_api_key";
    private static final String KEY_AI_ENABLED = "ai_enabled";
    private static final String KEY_AUTO_PLAY_RESPONSES = "auto_play_responses";
    private static final String KEY_RADIO_CHIRPS_ENABLED = "radio_chirps_enabled";

    private static AIConfig instance;
    private SharedPreferences prefs;

    private AIConfig(Context context) {
        this.prefs = context.getApplicationContext()
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized AIConfig getInstance(Context context) {
        if (instance == null) {
            instance = new AIConfig(context);
        }
        return instance;
    }

    // API Keys
    public void setOpenAIApiKey(String key) {
        prefs.edit().putString(KEY_OPENAI_API_KEY, key).apply();
    }

    public String getOpenAIApiKey() {
        return prefs.getString(KEY_OPENAI_API_KEY, null);
    }

    public void setAnthropicApiKey(String key) {
        prefs.edit().putString(KEY_ANTHROPIC_API_KEY, key).apply();
    }

    public String getAnthropicApiKey() {
        return prefs.getString(KEY_ANTHROPIC_API_KEY, null);
    }

    // Settings
    public void setAIEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_AI_ENABLED, enabled).apply();
    }

    public boolean isAIEnabled() {
        return prefs.getBoolean(KEY_AI_ENABLED, true);
    }

    public void setAutoPlayResponses(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUTO_PLAY_RESPONSES, enabled).apply();
    }

    public boolean isAutoPlayResponsesEnabled() {
        return prefs.getBoolean(KEY_AUTO_PLAY_RESPONSES, true);
    }

    public void setRadioChirpsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_RADIO_CHIRPS_ENABLED, enabled).apply();
    }

    public boolean isRadioChirpsEnabled() {
        return prefs.getBoolean(KEY_RADIO_CHIRPS_ENABLED, true);
    }

    // Validation
    public boolean hasOpenAIKey() {
        String key = getOpenAIApiKey();
        return key != null && !key.isEmpty();
    }

    public boolean hasAnthropicKey() {
        String key = getAnthropicApiKey();
        return key != null && !key.isEmpty();
    }

    public boolean isConfigured() {
        return hasOpenAIKey() || hasAnthropicKey();
    }

    // Clear all configuration
    public void clearAll() {
        prefs.edit().clear().apply();
    }
}
