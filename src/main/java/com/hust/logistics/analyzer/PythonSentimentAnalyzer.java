package com.hust.logistics.analyzer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hust.logistics.model.AnalysisResult;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Calls Python FastAPI service at /analyze.
 */
public class PythonSentimentAnalyzer implements SentimentAnalyzer {
    private static final String ANALYZE_URL = "http://127.0.0.1:8000/analyze";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PythonSentimentAnalyzer() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public AnalysisResult analyze(String text) {
        try {
            String requestBody = objectMapper.writeValueAsString(Map.of("content", text));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ANALYZE_URL))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Analyze API returned status " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            String sentiment = root.path("sentiment").asText("Trung lập");
            String damageType = root.path("damage_type").asText("Khác");
            double confidence = root.path("confidence").asDouble(0.0);
            return new AnalysisResult(sentiment, damageType, confidence);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Failed to call Python analyze API", e);
        }
    }
}
