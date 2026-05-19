package com.hust.logistics.clean.domain.entity;

import java.time.Instant;

public class SocialPost {
    private final String id;
    private final String platform;
    private final String content;
    private final Instant timestamp;
    private final String author;

    public SocialPost(String id, String platform, String content, Instant timestamp, String author) {
        this.id = id;
        this.platform = platform;
        this.content = content;
        this.timestamp = timestamp;
        this.author = author;
    }

    public String getId() {
        return id;
    }

    public String getPlatform() {
        return platform;
    }

    public String getContent() {
        return content;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public String toString() {
        return "SocialPost{" +
                "id='" + id + '\'' +
                ", platform='" + platform + '\'' +
                ", timestamp=" + timestamp +
                ", author='" + author + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
