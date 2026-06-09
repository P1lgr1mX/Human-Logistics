package com.hust.logistics.clean.infrastructure.crawler.impl;

import com.hust.logistics.clean.infrastructure.crawler.impl.MockCrawler;
import com.hust.logistics.clean.infrastructure.crawler.impl.GenericSocialCrawler;
import com.hust.logistics.clean.domain.entity.SocialPost;
import com.hust.logistics.clean.domain.gateway.SocialMediaCrawler;
import com.hust.logistics.clean.infrastructure.config.AppConfig;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class GenericSocialCrawler implements SocialMediaCrawler {
    private final String platformName;
    private final AppConfig config;

    public GenericSocialCrawler(String platformName, AppConfig config) {
        this.platformName = platformName;
        this.config = config;
    }

    @Override
    public String platform() {
        return platformName;
    }

    @Override
    public List<SocialPost> crawl() {
        String query = buildQuery(config.getKeywords(), config.getHashtags());
        List<String> raw = fetchRawData(query);
        return filterByTimeRange(mapToSocialPost(raw));
    }

    @Override
    public List<SocialPost> crawlByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return crawl().stream()
                .filter(post -> containsIgnoreCase(post.getContent(), keyword))
                .toList();
    }

    @Override
    public List<SocialPost> crawlByHashtag(String hashtag) {
        if (hashtag == null || hashtag.isBlank()) {
            return List.of();
        }
        String normalizedHashtag = hashtag.startsWith("#") ? hashtag : "#" + hashtag;
        return crawl().stream()
                .filter(post -> containsIgnoreCase(post.getContent(), normalizedHashtag))
                .toList();
    }

    protected String buildQuery(List<String> keywords, List<String> hashtags) {
        List<String> terms = new ArrayList<>();
        if (keywords != null) {
            terms.addAll(keywords);
        }
        if (hashtags != null) {
            terms.addAll(hashtags);
        }
        return String.join(" OR ", terms);
    }

    protected List<String> fetchRawData(String query) {
        // TODO: replace this stub with real API calls for each platform.
        return List.of(
                "need food and water #relief",
                "road blocked after heavy flood #emergency",
                "temporary shelter requested #aid"
        );
    }

    protected List<SocialPost> mapToSocialPost(List<String> rawData) {
        List<SocialPost> posts = new ArrayList<>();
        Instant now = Instant.now();
        for (int i = 0; i < rawData.size(); i++) {
            posts.add(new SocialPost(
                    UUID.randomUUID().toString(),
                    platformName,
                    rawData.get(i),
                    now.minusSeconds(i * 1800L),
                    platformName + "-author-" + (i + 1)
            ));
        }
        return posts;
    }

    protected List<SocialPost> filterByTimeRange(List<SocialPost> posts) {
        Instant startTime = config.getStartTime();
        Instant endTime = config.getEndTime();
        return posts.stream()
                .filter(post -> !post.getTimestamp().isBefore(startTime) && !post.getTimestamp().isAfter(endTime))
                .toList();
    }

    private boolean containsIgnoreCase(String str, String searchStr) {
        if (str == null || searchStr == null) return false;
        return str.toLowerCase(Locale.ROOT).contains(searchStr.toLowerCase(Locale.ROOT));
    }
}
