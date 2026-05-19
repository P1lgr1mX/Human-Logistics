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
    private static final String TASK_NAME = "relief-sentiment-analysis";
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

        List<String> categories = config.getReliefCategories();
        String prompt = "Phân tích mức độ hài lòng và xu hướng tâm lý của công chúng đối với các loại hàng cứu trợ sau: " 
                + String.join(", ", categories) + ".\n"
                + "Đối với mỗi loại, hãy xác định tỷ lệ tích cực/tiêu cực và tóm tắt lý do chính.";

        return analysisClient.analyze(prompt, posts);
    }
}
