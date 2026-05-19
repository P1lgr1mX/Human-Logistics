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
    private static final String TASK_NAME = "sentiment-trend-analysis";
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

        // Group posts by date
        Map<LocalDate, List<SocialPost>> postsByDate = posts.stream()
                .collect(Collectors.groupingBy(
                        post -> post.getTimestamp().atZone(ZoneId.systemDefault()).toLocalDate(),
                        TreeMap::new,
                        Collectors.toList()
                ));

        StringBuilder summaryBuilder = new StringBuilder("Sentiment Trend Analysis:\n");
        
        for (Map.Entry<LocalDate, List<SocialPost>> entry : postsByDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<SocialPost> dailyPosts = entry.getValue();
            
            // In a real scenario, we would call analysisClient for each post or batch.
            // For this task, we'll simulate the trend calculation logic.
            long total = dailyPosts.size();
            // This is a placeholder for actual sentiment analysis results from the client
            long positive = dailyPosts.stream().filter(p -> p.getContent().contains("tốt") || p.getContent().contains("cảm ơn")).count();
            long negative = dailyPosts.stream().filter(p -> p.getContent().contains("tệ") || p.getContent().contains("khó khăn")).count();
            
            double posPercent = (double) positive / total * 100;
            double negPercent = (double) negative / total * 100;
            
            summaryBuilder.append(String.format("- %s: Positive %.1f%%, Negative %.1f%%\n", 
                    date, posPercent, negPercent));
        }

        return new AnalysisResult(TASK_NAME, summaryBuilder.toString(), 1.0);
    }
}
