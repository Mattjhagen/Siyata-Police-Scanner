package com.siyata.scanner;

public class RadioFeed {
    private String name;
    private String description;
    private String streamUrl;
    private boolean isFavorite;
    private FeedType type;

    public enum FeedType {
        STREAM,  // Traditional HTTP/HTTPS audio stream
        PTT,     // WebSocket PTT walkie-talkie channel
        AI_AGENT // AI conversational agent
    }

    public RadioFeed(String name, String description, String streamUrl, boolean isFavorite) {
        this.name = name;
        this.description = description;
        this.streamUrl = streamUrl;
        this.isFavorite = isFavorite;

        // Detect feed type from URL scheme
        if (streamUrl.startsWith("ai://")) {
            this.type = FeedType.AI_AGENT;
        } else if (streamUrl.startsWith("ptt://") || streamUrl.startsWith("ws://") || streamUrl.startsWith("wss://")) {
            this.type = FeedType.PTT;
        } else {
            this.type = FeedType.STREAM;
        }
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public FeedType getType() {
        return type;
    }

    public boolean isPTT() {
        return type == FeedType.PTT;
    }

    public boolean isAIAgent() {
        return type == FeedType.AI_AGENT;
    }

    // Extract channel ID from PTT URL
    // ptt://server:8080/channelId -> channelId
    public String getChannelId() {
        if (isPTT()) {
            String url = streamUrl;
            // Remove ptt:// prefix if present
            if (url.startsWith("ptt://")) {
                url = url.substring(6);
            }
            // Extract channel from end of URL
            int lastSlash = url.lastIndexOf('/');
            if (lastSlash >= 0) {
                return url.substring(lastSlash + 1);
            }
        }
        return "1"; // Default channel
    }

    // Get WebSocket server URL from PTT URL
    // ptt://server:8080/channelId -> ws://server:8080
    public String getWebSocketUrl() {
        if (isPTT()) {
            String url = streamUrl;
            // Replace ptt:// with ws://
            if (url.startsWith("ptt://")) {
                url = "ws://" + url.substring(6);
            }
            // Remove channel suffix
            int lastSlash = url.lastIndexOf('/');
            if (lastSlash >= 0) {
                url = url.substring(0, lastSlash);
            }
            return url;
        }
        return null;
    }

    // Get AI agent ID from URL
    // ai://agentId -> agentId
    public String getAgentId() {
        if (isAIAgent() && streamUrl.startsWith("ai://")) {
            return streamUrl.substring(5); // Remove "ai://" prefix
        }
        return null;
    }
}
