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
    private static final String TASK_NAME = "task-damage";
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

        return analysisClient.analyze(TASK_NAME, posts);
    }
}
