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
        String prompt = "NHIỆM VỤ: Phân tích mức độ hài lòng của công chúng đối với công tác cứu trợ các mặt hàng: " + categories + ".\n" +
                "YÊU CẦU:\n" +
                "1. Đánh giá tỷ lệ Hài lòng, Trung lập và Không hài lòng dựa trên nội dung các bài đăng.\n" +
                "2. Tóm tắt các lý do chính khiến người dân hài lòng hoặc chưa hài lòng.\n" +
                "3. Định dạng kết quả báo cáo rõ ràng.\n\n" +
                "BẮT BUỘC: Cuối báo cáo phải có dòng dữ liệu sau để vẽ biểu đồ:\n" +
                "SATISFACTION_DATA: HAPPY=x, NEUTRAL=y, UNHAPPY=z\n" +
                "(Với x, y, z là tỷ lệ phần trăm, tổng bằng 100).";

        return analysisClient.analyze(prompt, posts);
    }
}
