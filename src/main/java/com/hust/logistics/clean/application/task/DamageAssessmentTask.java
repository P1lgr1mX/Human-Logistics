package com.hust.logistics.clean.application.task;

import com.hust.logistics.clean.domain.entity.AnalysisResult;
import com.hust.logistics.clean.domain.entity.SocialPost;
import com.hust.logistics.clean.domain.gateway.AnalysisClient;
import com.hust.logistics.clean.infrastructure.config.AppConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Task for assessing damage frequencies based on categories defined in configuration.
 * Statistics are used for Bar Chart visualization.
 */
public class DamageAssessmentTask implements AnalyticsTask {
    private static final String TASK_NAME = "damage-assessment";
    private final AnalysisClient analysisClient;
    private final AppConfig config;

    public DamageAssessmentTask(AnalysisClient analysisClient, AppConfig config) {
        this.analysisClient = analysisClient;
        this.config = config;
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

        List<String> categories = config.getDamageCategories();
        Map<String, Integer> frequencyMap = new HashMap<>();
        
        // Initialize frequency map with categories from config
        for (String category : categories) {
            frequencyMap.put(category, 0);
        }
        // Ensure "Khác" is always present
        frequencyMap.putIfAbsent("Khác", 0);

        // Analyze each post content for damage keywords (simplified simulation)
        for (SocialPost post : posts) {
            String content = post.getContent().toLowerCase();
            boolean matched = false;
            for (String category : categories) {
                if (content.contains(category.toLowerCase())) {
                    frequencyMap.put(category, frequencyMap.get(category) + 1);
                    matched = true;
                }
            }
            if (!matched) {
                frequencyMap.put("Khác", frequencyMap.get("Khác") + 1);
            }
        }

        StringBuilder summaryBuilder = new StringBuilder("Damage Assessment Statistics:\n");
        frequencyMap.forEach((category, count) -> {
            summaryBuilder.append(String.format("- %s: %d occurrences\n", category, count));
        });

        return new AnalysisResult(TASK_NAME, summaryBuilder.toString(), 1.0);
    }
}
