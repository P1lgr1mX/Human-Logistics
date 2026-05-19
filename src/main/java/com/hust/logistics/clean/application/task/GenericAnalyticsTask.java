package com.hust.logistics.clean.application.task;

import com.hust.logistics.clean.domain.entity.AnalysisResult;
import com.hust.logistics.clean.domain.entity.SocialPost;
import com.hust.logistics.clean.domain.gateway.AnalysisClient;

import java.util.List;

/**
 * Generic task that delegates analysis to the AnalysisClient.
 * This satisfies the "flexibility" requirement by allowing new problems to be solved
 * without creating new classes, just by changing the prompt/task name.
 */
public class GenericAnalyticsTask implements AnalyticsTask {
    private final String taskName;
    private final AnalysisClient analysisClient;

    public GenericAnalyticsTask(String taskName, AnalysisClient analysisClient) {
        this.taskName = taskName;
        this.analysisClient = analysisClient;
    }

    @Override
    public String name() {
        return taskName;
    }

    @Override
    public AnalysisResult execute(List<SocialPost> posts) {
        if (posts == null || posts.isEmpty()) {
            return new AnalysisResult(taskName, "No data to analyze", 0.0);
        }
        return analysisClient.analyze(taskName, posts);
    }
}
