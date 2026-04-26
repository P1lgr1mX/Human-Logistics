package com.hust.logistics.analyzer;

import com.hust.logistics.model.AnalysisResult;

/**
 * Contract for sentiment analysis implementations.
 */
public interface SentimentAnalyzer {
    AnalysisResult analyze(String text);
}
