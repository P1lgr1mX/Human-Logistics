package com.hust.logistics.analyzer;

import com.hust.logistics.model.AnalysisResult;

/**
 * Tiny heuristic analyzer for demo purposes.
 */
public class KeywordSentimentAnalyzer implements SentimentAnalyzer {

    @Override
    public AnalysisResult analyze(String text) {
        String normalized = text.toLowerCase();
        if (normalized.contains("blocked") || normalized.contains("delayed")) {
            return new AnalysisResult("Tiêu cực", "Cơ sở hạ tầng", 0.7);
        }
        if (normalized.contains("completed") || normalized.contains("delivered")) {
            return new AnalysisResult("Tích cực", "Khác", 0.8);
        }
        return new AnalysisResult("Trung lập", "Khác", 0.5);
    }
}
