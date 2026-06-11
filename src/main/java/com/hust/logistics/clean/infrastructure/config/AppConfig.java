package com.hust.logistics.clean.infrastructure.config;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private List<String> keywords = new ArrayList<>();
    private List<String> hashtags = new ArrayList<>();
    private Instant startTime = Instant.parse("1970-01-01T00:00:00Z");
    private Instant endTime = Instant.parse("2100-01-01T00:00:00Z");
    private Map<String, String> apiKeys = Map.of();
    private List<String> damageCategories = new ArrayList<>();
    private List<String> reliefCategories = new ArrayList<>();
    private String platform = "mock";
    private AnalysisConfig analysis = new AnalysisConfig();
    private String defaultKeyword;

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public Map<String, String> getApiKeys() {
        return apiKeys;
    }

    public void setApiKeys(Map<String, String> apiKeys) {
        this.apiKeys = apiKeys;
    }

    public List<String> getDamageCategories() {
        return damageCategories;
    }

    public void setDamageCategories(List<String> damageCategories) {
        this.damageCategories = damageCategories;
    }

    public List<String> getReliefCategories() {
        return reliefCategories;
    }

    public void setReliefCategories(List<String> reliefCategories) {
        this.reliefCategories = reliefCategories;
    }

    public AnalysisConfig getAnalysis() {
        return analysis;
    }

    public void setAnalysis(AnalysisConfig analysis) {
        this.analysis = analysis;
    }

    public String getDefaultKeyword() {
        return defaultKeyword;
    }

    public void setDefaultKeyword(String defaultKeyword) {
        this.defaultKeyword = defaultKeyword;
    }

    public static class AnalysisConfig {
        private String provider = "deepseek";
        private String endpoint = "https://api.deepseek.com/v1/chat/completions";
        private String model = "deepseek-chat";
        private int connectTimeoutMs = 3000;
        private int readTimeoutMs = 10000;

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }
    }
}
