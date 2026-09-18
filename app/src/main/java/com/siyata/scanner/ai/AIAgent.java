package com.siyata.scanner.ai;

import java.util.UUID;

/**
 * Represents an AI agent with its configuration and personality.
 * Each agent has its own conversation context and can have different tools/capabilities.
 */
public class AIAgent {
    private String id;
    private String name;
    private String description;
    private String systemPrompt;
    private AIProvider provider;
    private String model;
    private String voice;
    private boolean enabled;

    public enum AIProvider {
        OPENAI("OpenAI"),
        ANTHROPIC("Anthropic"),
        LOCAL("Local");

        private final String displayName;

        AIProvider(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public AIAgent(String name, String description, String systemPrompt, AIProvider provider) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.systemPrompt = systemPrompt;
        this.provider = provider;
        this.model = getDefaultModel(provider);
        this.voice = getDefaultVoice(provider);
        this.enabled = true;
    }

    public AIAgent(String id, String name, String description, String systemPrompt,
                   AIProvider provider, String model, String voice, boolean enabled) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.systemPrompt = systemPrompt;
        this.provider = provider;
        this.model = model;
        this.voice = voice;
        this.enabled = enabled;
    }

    private String getDefaultModel(AIProvider provider) {
        switch (provider) {
            case OPENAI:
                return "gpt-4o"; // Latest OpenAI model
            case ANTHROPIC:
                return "claude-sonnet-4.5";
            case LOCAL:
                return "llama-3";
            default:
                return "gpt-4o";
        }
    }

    private String getDefaultVoice(AIProvider provider) {
        switch (provider) {
            case OPENAI:
                return "alloy"; // OpenAI TTS voice
            case ANTHROPIC:
                return "default";
            case LOCAL:
                return "default";
            default:
                return "alloy";
        }
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public AIProvider getProvider() {
        return provider;
    }

    public void setProvider(AIProvider provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return name;
    }
}
