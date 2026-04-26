package com.hust.logistics.model;

/**
 * POJO representing one logistics event.
 */
public class LogisticsRecord {
    private String message;
    private double sentimentScore;

    public LogisticsRecord() {
    }

    public LogisticsRecord(String message, double sentimentScore) {
        this.message = message;
        this.sentimentScore = sentimentScore;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public double getSentimentScore() {
        return sentimentScore;
    }

    public void setSentimentScore(double sentimentScore) {
        this.sentimentScore = sentimentScore;
    }

    @Override
    public String toString() {
        return "LogisticsRecord{" +
                "message='" + message + '\'' +
                ", sentimentScore=" + sentimentScore +
                '}';
    }
}
