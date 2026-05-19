package com.hust.logistics.clean.infrastructure.crawler;

import com.hust.logistics.clean.domain.gateway.SocialMediaCrawler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CrawlerStrategyRegistry {
    private final Map<String, SocialMediaCrawler> crawlerByPlatform = new HashMap<>();

    public void register(SocialMediaCrawler crawler) {
        crawlerByPlatform.put(crawler.platform(), crawler);
    }

    public Optional<SocialMediaCrawler> get(String platform) {
        return Optional.ofNullable(crawlerByPlatform.get(platform));
    }
}
