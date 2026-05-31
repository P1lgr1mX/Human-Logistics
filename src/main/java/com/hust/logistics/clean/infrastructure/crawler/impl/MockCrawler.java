package com.hust.logistics.clean.infrastructure.crawler;

import com.hust.logistics.clean.domain.entity.SocialPost;
import com.hust.logistics.clean.domain.gateway.SocialMediaCrawler;
import com.hust.logistics.clean.infrastructure.config.AppConfig;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

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
        List<String> damageCats = safe(config.getDamageCategories());
        List<String> reliefCats = safe(config.getReliefCategories());
        
        Instant start = config.getStartTime();
        Instant end = config.getEndTime();

        long seconds = Math.max(1L, Duration.between(start, end).getSeconds());
        int numPosts = 30; // Tăng lên 30 bài để dữ liệu phong phú hơn
        long step = Math.max(1L, seconds / numPosts);

        String[] templates = {
            "Tình hình %s rất căng thẳng, %s đang diễn ra nghiêm trọng. Rất cần %s ngay lúc này! %s",
            "Cập nhật từ vùng lũ: %s đã phá hủy %s. Người dân đang thiếu %s. Mọi người cẩn trọng. %s",
            "Cảm ơn các đoàn cứu trợ đã mang %s đến cho bà con vùng %s sau khi chịu %s. %s",
            "Mọi thứ thật tồi tệ do %s. %s khiến chúng tôi kiệt sức. Hy vọng sớm có %s. %s",
            "Tin mừng: Công tác khắc phục %s đang tiến triển. %s đã được kiểm soát. %s đang được phân phối. %s"
        };

        Random random = new Random();

        for (int i = 0; i < numPosts; i++) {
            String kw = keywords.isEmpty() ? "thiên tai" : keywords.get(random.nextInt(keywords.size()));
            String dc = damageCats.isEmpty() ? "thiệt hại" : damageCats.get(random.nextInt(damageCats.size()));
            String rc = reliefCats.isEmpty() ? "nhu yếu phẩm" : reliefCats.get(random.nextInt(reliefCats.size()));
            String ht = hashtags.isEmpty() ? "#cuutro" : hashtags.get(random.nextInt(hashtags.size()));
            
            String template = templates[random.nextInt(templates.length)];
            String content = String.format(template, kw, dc, rc, ht);
            
            Instant timestamp = start.plusSeconds(step * i);
            posts.add(new SocialPost(
                    UUID.randomUUID().toString(),
                    platform(),
                    content,
                    timestamp.isAfter(end) ? end : timestamp,
                    "user-" + (i + 1)
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
