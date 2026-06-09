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

        String instruction = "NHIỆM VỤ: Thống kê thiệt hại dựa trên các danh mục sau:\n" +
                "1. Nhà cửa bị hư hỏng: Các bài đăng nói về sập nhà, tốc mái, ngập nhà.\n" +
                "2. Gián đoạn kinh tế sản xuất: Mất mùa, chết gia súc, hỏng xưởng, dừng kinh doanh.\n" +
                "3. Tài sản cá nhân bị mất: Hỏng xe cộ, đồ gia dụng, trôi đồ đạc.\n" +
                "4. Cơ sở hạ tầng bị hư hỏng: Sập cầu, hỏng đường, đổ cột điện, mất mạng.\n" +
                "5. Người bị ảnh hưởng: Bị thương, mất tích, cần cứu hộ y tế.\n" +
                "6. Khác: Các thiệt hại không thuộc nhóm trên.\n\n" +
                "QUY TẮC: Chỉ đếm các trường hợp riêng biệt. Trình bày kết quả theo định dạng:\n" +
                "- Danh mục: X occurrences\n" +
                "Cuối báo cáo PHẢI có dòng DATA_POINTS cho biểu đồ.";
        
        return analysisClient.analyze(instruction, posts);
    }
}
