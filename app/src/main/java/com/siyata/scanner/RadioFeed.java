package com.siyata.scanner;

public class RadioFeed {
    private String name;
    private String description;
    private String streamUrl;
    private boolean isFavorite;
    private FeedType type;
    private Integer broadcastifyFeedId;  // For auto-refreshing Broadcastify URLs

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

        // Extract Broadcastify feed ID
        if (streamUrl.startsWith("broadcastify://")) {
            // Format: broadcastify://12345
            try {
                this.broadcastifyFeedId = Integer.parseInt(streamUrl.substring(15));
            } catch (NumberFormatException e) {
                this.broadcastifyFeedId = null;
            }
        } else {
            // Try to extract from full URL
            this.broadcastifyFeedId = BroadcastifyAuthManager.extractFeedId(streamUrl);
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

    // Get Broadcastify feed ID
    public Integer getBroadcastifyFeedId() {
        return broadcastifyFeedId;
    }

    // Check if this is a Broadcastify feed
    public boolean isBroadcastifyFeed() {
        return broadcastifyFeedId != null;
    }

    // Update the stream URL (for refreshing expired tokens)
    public void setStreamUrl(String newUrl) {
        this.streamUrl = newUrl;
    }
}
