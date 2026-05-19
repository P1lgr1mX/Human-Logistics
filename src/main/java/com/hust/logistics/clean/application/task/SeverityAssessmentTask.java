package com.hust.logistics.clean.application.task;

import com.hust.logistics.clean.domain.entity.AnalysisResult;
import com.hust.logistics.clean.domain.entity.SocialPost;
import com.hust.logistics.clean.domain.gateway.AnalysisClient;

import java.util.List;

public class SeverityAssessmentTask implements AnalyticsTask {
    private static final String TASK_NAME = "severity-assessment";
    private final AnalysisClient analysisClient;

    public SeverityAssessmentTask(AnalysisClient analysisClient) {
        this.analysisClient = analysisClient;
    }

    @Override
    public String name() {
        return TASK_NAME;
    }

    @Override
    public AnalysisResult execute(List<SocialPost> posts) {
        return analysisClient.analyze(TASK_NAME, posts);
    }
}
