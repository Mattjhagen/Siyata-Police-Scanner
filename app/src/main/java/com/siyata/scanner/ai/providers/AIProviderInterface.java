package com.siyata.scanner.ai.providers;

import com.siyata.scanner.ai.AIAgent;
import com.siyata.scanner.ai.ConversationMessage;

import java.util.List;

/**
 * Interface for AI providers (OpenAI, Anthropic, local models, etc.)
 * Providers handle the actual communication with AI APIs.
 */
public interface AIProviderInterface {

    /**
     * Send a message and get a response from the AI
     *
     * @param agent The AI agent configuration
     * @param conversationHistory Previous messages for context
     * @param userMessage The new user message
     * @param callback Callback for response or error
     */
    void sendMessage(
        AIAgent agent,
        List<ConversationMessage> conversationHistory,
        String userMessage,
        ResponseCallback callback
    );

    /**
     * Transcribe audio to text
     *
     * @param audioData PCM16 audio data
     * @param sampleRate Audio sample rate
     * @param callback Callback for transcription result or error
     */
    void transcribeAudio(
        byte[] audioData,
        int sampleRate,
        TranscriptionCallback callback
    );

    /**
     * Convert text to speech
     *
     * @param text Text to convert to speech
     * @param voice Voice ID to use
     * @param callback Callback for audio data or error
     */
    void textToSpeech(
        String text,
        String voice,
        TTSCallback callback
    );

    /**
     * Check if API credentials are configured
     */
    boolean isConfigured();

    /**
     * Cancel any ongoing requests
     */
    void cancelRequests();

    // Callback interfaces
    interface ResponseCallback {
        void onSuccess(String responseText);
        void onError(String error);
    }

    interface TranscriptionCallback {
        void onSuccess(String transcribedText);
        void onError(String error);
    }

    interface TTSCallback {
        void onSuccess(byte[] audioData);
        void onError(String error);
    }
}
