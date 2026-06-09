package com.hust.logistics.clean.application.task;

import com.hust.logistics.clean.domain.entity.AnalysisResult;
import com.hust.logistics.clean.domain.entity.SocialPost;
import com.hust.logistics.clean.domain.gateway.AnalysisClient;
import com.hust.logistics.clean.infrastructure.config.AppConfig;

import java.util.List;

/**
 * Task for Problem 3 & 4: Analyze sentiment and satisfaction for each relief category.
 */
public class ReliefAnalysisTask implements AnalyticsTask {
    private static final String TASK_NAME = "task-satisfaction";
    private final AnalysisClient analysisClient;
    private final AppConfig config;

    public ReliefAnalysisTask(AnalysisClient analysisClient, AppConfig config) {
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

        String categories = String.join(", ", config.getReliefCategories());
        String prompt = "NHIỆM VỤ: Phân tích mức độ hài lòng về cứu trợ (" + categories + "). " +
                "SATISFACTION_DATA: HAPPY=x, NEUTRAL=y, UNHAPPY=z";

        return analysisClient.analyze(TASK_NAME, posts);
    }
}
