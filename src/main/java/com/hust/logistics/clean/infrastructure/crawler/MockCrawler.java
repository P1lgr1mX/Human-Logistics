package com.hust.logistics.clean.infrastructure.crawler;

import com.hust.logistics.clean.domain.entity.SocialPost;
import com.hust.logistics.clean.domain.gateway.SocialMediaCrawler;
import com.hust.logistics.clean.infrastructure.config.AppConfig;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MockCrawler implements SocialMediaCrawler {
    private final AppConfig config;

    public MockCrawler(AppConfig config) {
        this.config = config;
    }

    @Override
    public String platform() {
        return "mock";
    }

    @Override
    public List<SocialPost> crawl() {
        List<SocialPost> posts = new ArrayList<>();
        List<String> keywords = safe(config.getKeywords());
        List<String> hashtags = safe(config.getHashtags());
        Instant start = config.getStartTime();
        Instant end = config.getEndTime();

        long seconds = Math.max(1L, Duration.between(start, end).getSeconds());
        long step = Math.max(1L, seconds / Math.max(1, keywords.size() + hashtags.size()));

        int index = 0;
        for (String keyword : keywords) {
            String hashtag = hashtags.isEmpty() ? "#relief" : normalizeHashtag(hashtags.get(index % hashtags.size()));
            Instant timestamp = start.plusSeconds(step * (index + 1));
            posts.add(new SocialPost(
                    UUID.randomUUID().toString(),
                    platform(),
                    "Urgent update: " + keyword + " " + hashtag,
                    timestamp.isAfter(end) ? end : timestamp,
                    "mock-user-" + (index + 1)
            ));
            index++;
        }
        if (posts.isEmpty()) {
            posts.add(new SocialPost(
                    UUID.randomUUID().toString(),
                    platform(),
                    "General humanitarian update #aid",
                    start,
                    "mock-user-default"
            ));
        }
        return posts;
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
        String normalizedHashtag = normalizeHashtag(hashtag);
        return crawl().stream()
                .filter(post -> containsIgnoreCase(post.getContent(), normalizedHashtag))
                .toList();
    }

    private List<String> safe(List<String> values) {
        return values == null ? List.of() : values;
    }

    private String normalizeHashtag(String hashtag) {
        return hashtag.startsWith("#") ? hashtag : "#" + hashtag;
    }

    private boolean containsIgnoreCase(String content, String term) {
        return content.toLowerCase(Locale.ROOT).contains(term.toLowerCase(Locale.ROOT));
    }
}
