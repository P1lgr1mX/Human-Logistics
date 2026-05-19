package com.hust.logistics.clean.infrastructure.client;

import com.hust.logistics.analyzer.GptAnalysisProvider;
import com.hust.logistics.clean.domain.entity.AnalysisResult;
import com.hust.logistics.clean.domain.entity.SocialPost;
import com.hust.logistics.clean.domain.gateway.AnalysisClient;
import com.hust.logistics.clean.infrastructure.config.AppConfig;

import java.util.List;
import java.util.stream.Collectors;

public class GptAnalysisClient implements AnalysisClient {
    private final AppConfig config;
    private final GptAnalysisProvider provider;

    public GptAnalysisClient(AppConfig config) {
        this.config = config;
        String model = config.getAnalysis().getModel();
        String apiKey = config.getApiKeys().get(model);
        if (apiKey == null) {
            // Fallback to "deepseek" or first available key if model-specific key is missing
            apiKey = config.getApiKeys().getOrDefault("deepseek", 
                     config.getApiKeys().values().stream().findFirst().orElse(""));
        }
        
        this.provider = new GptAnalysisProvider(
            apiKey,
            config.getAnalysis().getEndpoint(),
            model
        );
    }

    @Override
    public AnalysisResult analyze(String taskName, List<SocialPost> posts) {
        // Concatenate posts for batch analysis or analyze them individually
        // For simplicity, we analyze the first post or a summary
        String combinedText = posts.stream()
                .map(SocialPost::getContent)
                .collect(Collectors.joining("\n---\n"));
        
        com.hust.logistics.model.AnalysisResult internalResult = provider.analyze(combinedText);
        
        return new AnalysisResult(
            taskName,
            String.format("Sentiment: %s, Damage: %s, Relief: %s", 
                internalResult.getSentiment(), 
                internalResult.getDamageType(), 
                internalResult.getReliefSupplies()),
            internalResult.getConfidence()
        );
    }
}
