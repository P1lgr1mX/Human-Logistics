package com.hust.logistics.clean.infrastructure.crawler;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.hust.logistics.clean.domain.gateway.SocialMediaCrawler;
import com.hust.logistics.clean.infrastructure.config.AppConfig;
import com.hust.logistics.clean.infrastructure.crawler.impl.MockCrawler;

@Component
public class CrawlerFactory {

    private final Map<String, SocialMediaCrawler> crawlers;

    public CrawlerFactory(List<SocialMediaCrawler> crawlerList) {
        // Ánh xạ các crawler theo tên platform (viết thường)
        this.crawlers = crawlerList.stream()
                .collect(Collectors.toMap(
                        c -> c.platform().toLowerCase(Locale.ROOT),
                        c -> c
                ));
    }

    public SocialMediaCrawler create(String platform, AppConfig config) {
        try {
            if (platform == null || platform.isBlank()) {
                platform = "mock";
            }

            String normalized = platform.toLowerCase(Locale.ROOT);

            SocialMediaCrawler crawler = crawlers.get(normalized);

            if (crawler != null) {
                return crawler;
            }

            System.err.println(
                    "Unsupported platform: " + platform + ", fallback to mock crawler");

            return new MockCrawler(config);

        } catch (Exception e) {
            System.err.println(
                    "Error creating crawler: " + e.getMessage());

            return new MockCrawler(config);
        }
    }
}
