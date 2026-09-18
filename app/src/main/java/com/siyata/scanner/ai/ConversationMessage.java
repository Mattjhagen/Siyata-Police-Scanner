package com.siyata.scanner.ai;

import java.util.Date;

/**
 * Represents a single message in a conversation with an AI agent.
 * Maintains conversation history for context across multiple PTT interactions.
 */
public class ConversationMessage {
    private String id;
    private String agentId;
    private Role role;
    private String content;
    private Date timestamp;
    private MessageType type;

    public enum Role {
        USER("user"),
        ASSISTANT("assistant"),
        SYSTEM("system");

        private final String apiValue;

        Role(String apiValue) {
            this.apiValue = apiValue;
        }

        public String getApiValue() {
            return apiValue;
        }
    }

    public enum MessageType {
        TEXT,           // Normal text message
        AUDIO,          // Audio recording (stored separately)
        TRANSCRIPTION,  // Transcribed from audio
        ERROR           // Error message
    }

    public ConversationMessage(String agentId, Role role, String content) {
        this.id = java.util.UUID.randomUUID().toString();
        this.agentId = agentId;
        this.role = role;
        this.content = content;
        this.timestamp = new Date();
        this.type = MessageType.TEXT;
    }

    public ConversationMessage(String id, String agentId, Role role, String content,
                             Date timestamp, MessageType type) {
        this.id = id;
        this.agentId = agentId;
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
        this.type = type;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getAgentId() {
        return agentId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    /**
     * Format message for display in UI
     */
    public String getDisplayText() {
        String prefix = role == Role.USER ? "YOU: " : role == Role.ASSISTANT ? "AI: " : "";
        return prefix + content;
    }

    /**
     * Get truncated preview for list display
     */
    public String getPreview(int maxLength) {
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength - 3) + "...";
    }
}
