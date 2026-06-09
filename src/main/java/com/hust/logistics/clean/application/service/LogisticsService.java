
package com.hust.logistics.clean.application.service;

import com.hust.logistics.clean.application.task.*;
import com.hust.logistics.clean.domain.entity.AnalysisResult;
import com.hust.logistics.clean.domain.entity.SocialPost;
import com.hust.logistics.clean.domain.gateway.AnalysisClient;
import com.hust.logistics.clean.domain.gateway.SocialMediaCrawler;
import com.hust.logistics.clean.infrastructure.config.AppConfig;
import com.hust.logistics.clean.infrastructure.crawler.CrawlerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class LogisticsService {
    private final AnalysisClient analysisClient;
    private final CrawlerFactory crawlerFactory;
    private final AppConfig config;

    public LogisticsService(AnalysisClient analysisClient, CrawlerFactory crawlerFactory, AppConfig config) {
        this.analysisClient = analysisClient;
        this.crawlerFactory = crawlerFactory;
        this.config = config;
    }

    public AnalysisResult runTask(int taskId, String keywords, String startTime, String endTime) {
        // Update config with dynamic values if provided
        try {
            if (keywords != null && !keywords.isBlank()) {
                config.setKeywords(List.of(keywords.split("[,;]")));
            }
            if (startTime != null && !startTime.isBlank()) {
                config.setStartTime(Instant.parse(startTime));
            }
            if (endTime != null && !endTime.isBlank()) {
                config.setEndTime(Instant.parse(endTime));
            }
        } catch (Exception e) {
            // Fallback to default if parsing fails
        }

        SocialMediaCrawler crawler = crawlerFactory.create(config.getPlatform(), config);
        List<SocialPost> posts = crawler.crawl();


        if (posts.isEmpty()) {
            return new AnalysisResult("Error", "KHÔNG TÌM THẤY DỮ LIỆU: Vui lòng kiểm tra lại từ khóa hoặc API Key của YouTube. Hiện tại hệ thống không thể lấy thông tin từ YouTube.", 0.0);
        }

        AnalyticsTask task = switch (taskId) {
            case 1 -> new SentimentTrendTask(analysisClient);
            case 2 -> new DamageAssessmentTask(analysisClient, config);
            case 3 -> new ReliefAnalysisTask(analysisClient, config);
            case 4 -> new GenericAnalyticsTask("task-trend", analysisClient);
            default -> throw new IllegalArgumentException("Invalid task ID: " + taskId);
        };

        return task.execute(posts);
    }
}
