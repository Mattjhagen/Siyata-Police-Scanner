package com.siyata.scanner;

public class RadioFeed {
    private String name;
    private String description;
    private String streamUrl;
    private boolean isFavorite;

    public RadioFeed(String name, String description, String streamUrl, boolean isFavorite) {
        this.name = name;
        this.description = description;
        this.streamUrl = streamUrl;
        this.isFavorite = isFavorite;
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

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
