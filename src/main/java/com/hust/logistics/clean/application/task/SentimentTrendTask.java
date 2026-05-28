package com.hust.logistics.clean.application.task;

import com.hust.logistics.clean.domain.entity.AnalysisResult;
import com.hust.logistics.clean.domain.entity.SocialPost;
import com.hust.logistics.clean.domain.gateway.AnalysisClient;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Task for analyzing sentiment trends over time.
 * Groups data by date and calculates percentage of positive/negative sentiment.
 */
public class SentimentTrendTask implements AnalyticsTask {
    private static final String TASK_NAME = "task-sentiment";
    private final AnalysisClient analysisClient;

    public SentimentTrendTask(AnalysisClient analysisClient) {
        this.analysisClient = analysisClient;
    }

    @Override
    public String name() {
        return TASK_NAME;
    }

    @Override
    public AnalysisResult execute(List<SocialPost> posts) {
        if (posts == null || posts.isEmpty()) {
            return new AnalysisResult(TASK_NAME, "No data to analyze", 0.0);
        }

        // Gọi AnalysisClient để phân tích tổng thể thay vì tự tính toán thủ công
        return analysisClient.analyze(TASK_NAME, posts);
    }
}
