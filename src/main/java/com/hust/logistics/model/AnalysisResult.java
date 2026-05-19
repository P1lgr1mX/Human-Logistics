package com.hust.logistics.model;

/**
 * Result returned by sentiment analyzers.
 */
public class AnalysisResult {
    private String sentiment;
    private String damageType;
    private String reliefSupplies;
    private double confidence;
    private String timestamp;

    public AnalysisResult() {
    }

    public AnalysisResult(String sentiment, String damageType, double confidence) {
        this.sentiment = sentiment;
        this.damageType = damageType;
        this.reliefSupplies = "Khác";
        this.confidence = confidence;
    }

    public AnalysisResult(String sentiment, String damageType, String reliefSupplies, double confidence) {
        this.sentiment = sentiment;
        this.damageType = damageType;
        this.reliefSupplies = reliefSupplies;
        this.confidence = confidence;
    }

    public AnalysisResult(String sentiment, String damageType, String reliefSupplies, double confidence, String timestamp) {
        this.sentiment = sentiment;
        this.damageType = damageType;
        this.reliefSupplies = reliefSupplies;
        this.confidence = confidence;
        this.timestamp = timestamp;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public String getDamageType() {
        return damageType;
    }

    public void setDamageType(String damageType) {
        this.damageType = damageType;
    }

    public String getReliefSupplies() {
        return reliefSupplies;
    }

    public void setReliefSupplies(String reliefSupplies) {
        this.reliefSupplies = reliefSupplies;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "AnalysisResult{" +
                "sentiment='" + sentiment + '\'' +
                ", damageType='" + damageType + '\'' +
                ", reliefSupplies='" + reliefSupplies + '\'' +
                ", confidence=" + confidence +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
