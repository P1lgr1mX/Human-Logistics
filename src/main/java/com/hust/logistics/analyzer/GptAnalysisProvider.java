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
import java.util.List;
import java.util.Map;

/**
 * Generic GPT-style API provider (DeepSeek, Groq, OpenAI, etc.)
 * Performs sentiment analysis, damage classification, and relief supply detection in one batch call.
 */
public class GptAnalysisProvider implements SentimentAnalyzer {
    private final String apiKey;
    private final String endpoint;
    private final String model;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GptAnalysisProvider(String apiKey, String endpoint, String model) {
        this.apiKey = apiKey;
        this.endpoint = endpoint;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public AnalysisResult analyze(String text) {
        String systemPrompt = "Bạn là một hệ thống AI phân tích dữ liệu cứu trợ nhân đạo chuyên nghiệp. " +
                "Nhiệm vụ của bạn là phân tích văn bản đầu vào và thực hiện đồng thời 3 nhiệm vụ sau:\n" +
                "1. Phân tích tâm lý (Sentiment): Tích cực, Tiêu cực, hoặc Trung lập.\n" +
                "2. Phân loại 5 loại thiệt hại (Damage): Người, Kinh tế, Nhà cửa, Tài sản, Hạ tầng.\n" +
                "3. Phân loại loại hàng cứu trợ (Relief): Thực phẩm, Y tế, Giao thông, Nhu yếu phẩm, Khác.\n\n" +
                "YÊU CẦU QUAN TRỌNG:\n" +
                "- Chỉ trả về định dạng JSON chuẩn.\n" +
                "- Không thêm bất kỳ văn bản giải thích nào ngoài JSON.\n" +
                "- Các trường trong JSON phải khớp chính xác với mẫu dưới đây:\n" +
                "{\n" +
                "  \"sentiment\": \"...\",\n" +
                "  \"damageType\": \"...\",\n" +
                "  \"reliefSupplies\": \"...\",\n" +
                "  \"confidence\": 0.95\n" +
                "}";

        try {
            Map<String, Object> requestBodyMap = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", text)
                    ),
                    "response_format", Map.of("type", "json_object"),
                    "temperature", 0.1
            );

            String requestBody = objectMapper.writeValueAsString(requestBodyMap);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new RuntimeException("AI API error: HTTP " + response.statusCode() + " - " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices").get(0).path("message").path("content").asText();
            
            JsonNode resultNode = objectMapper.readTree(content);
            
            AnalysisResult result = new AnalysisResult();
            result.setSentiment(resultNode.path("sentiment").asText("Trung lập"));
            result.setDamageType(resultNode.path("damageType").asText("Khác"));
            result.setReliefSupplies(resultNode.path("reliefSupplies").asText("Khác"));
            result.setConfidence(resultNode.path("confidence").asDouble(0.0));
            result.setTimestamp(java.time.Instant.now().toString());
            
            return result;

        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Failed to call AI API provider", e);
        }
    }
}
