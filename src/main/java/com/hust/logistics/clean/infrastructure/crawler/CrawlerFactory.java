package com.hust.logistics.clean.infrastructure.crawler;

import com.hust.logistics.clean.domain.gateway.SocialMediaCrawler;
import com.hust.logistics.clean.infrastructure.config.AppConfig;

import java.util.Locale;

public class CrawlerFactory {

    public SocialMediaCrawler create(String platform, AppConfig config) {
        if (platform == null || platform.isBlank()) {
            throw new IllegalArgumentException("Platform must not be blank.");
        }
        String normalized = platform.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "mock" -> new MockCrawler(config);
            case "twitter", "youtube", "facebook", "tiktok" -> new GenericSocialCrawler(normalized, config);
            default -> throw new IllegalArgumentException("Unsupported platform: " + platform);
        };
    }
}
