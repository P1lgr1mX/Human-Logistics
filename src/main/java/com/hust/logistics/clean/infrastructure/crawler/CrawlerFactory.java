package com.hust.logistics.clean.infrastructure.crawler;

import com.hust.logistics.clean.domain.gateway.SocialMediaCrawler;
import com.hust.logistics.clean.infrastructure.config.AppConfig;
import com.hust.logistics.clean.infrastructure.crawler.impl.MockCrawler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
        if (platform == null || platform.isBlank()) {
            platform = "mock";
        }
        String normalized = platform.toLowerCase(Locale.ROOT);
        
        SocialMediaCrawler crawler = crawlers.get(normalized);
        if (crawler != null) {
            return crawler;
        }

        if ("mock".equals(normalized)) {
            return new MockCrawler(config);
        }

        throw new IllegalArgumentException("Unsupported platform: " + platform);
    }
}
