package com.hust.logistics.model;

/**
 * POJO representing one social media post.
 */
public class SocialPost {
    private String author;
    private String content;
    private String timestamp;
    private String source;

    public SocialPost() {
    }

    public SocialPost(String author, String content, String timestamp, String source) {
        this.author = author;
        this.content = content;
        this.timestamp = timestamp;
        this.source = source;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
