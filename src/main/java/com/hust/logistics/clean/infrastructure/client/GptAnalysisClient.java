package com.hust.logistics.clean.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hust.logistics.clean.domain.entity.AnalysisResult;
import com.hust.logistics.clean.domain.entity.SocialPost;
import com.hust.logistics.clean.domain.gateway.AnalysisClient;
import com.hust.logistics.clean.infrastructure.config.AppConfig;
import com.hust.logistics.clean.infrastructure.preprocess.TextPreprocessor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GptAnalysisClient implements AnalysisClient {
    private final AppConfig config;
    private final String apiKey;
    private final String endpoint;
    private final String model;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final TextPreprocessor preprocessor;

    public GptAnalysisClient(AppConfig config) {
        this.config = config;
        this.model = config.getAnalysis().getModel();
        this.endpoint = config.getAnalysis().getEndpoint();
        this.preprocessor = new TextPreprocessor();
        
        String key = config.getApiKeys().get(model);
        if (key == null) {
            key = config.getApiKeys().getOrDefault("deepseek", 
                     config.getApiKeys().values().stream().findFirst().orElse(""));
        }
        this.apiKey = key;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public AnalysisResult analyze(String taskName, List<SocialPost> posts) {
        String combinedText = posts.stream()
                .map(post -> preprocessor.preprocess(post.getContent()))
                .collect(Collectors.joining("\n---\n"));

        int maxRetries = 2;
        int retryCount = 0;

        while (retryCount <= maxRetries) {
            try {
                return callApi(taskName, combinedText);
            } catch (Exception e) {
                retryCount++;
                if (retryCount > maxRetries) {
                    return new AnalysisResult(taskName, "Lỗi API sau " + maxRetries + " lần thử: " + e.getMessage(), 0.0);
                }
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        }
        return new AnalysisResult(taskName, "Lỗi không xác định", 0.0);
    }

    private AnalysisResult callApi(String taskName, String text) throws IOException, InterruptedException {
        String provider = config.getAnalysis().getProvider();
        String systemPrompt = "Bạn là một hệ thống AI phân tích dữ liệu cứu trợ nhân đạo chuyên nghiệp. " +
                "Nhiệm vụ của bạn là phân tích văn bản đầu vào và thực hiện nhiệm vụ: " + taskName + ".\n" +
                "YÊU CẦU QUAN TRỌNG:\n" +
                "- Chỉ trả về định dạng JSON chuẩn.\n" +
                "- Không thêm bất kỳ văn bản giải thích nào ngoài JSON.\n" +
                "- Các trường trong JSON phải bao gồm: \"summary\" (tóm tắt kết quả), \"score\" (độ tin cậy từ 0.0 đến 1.0).\n" +
                "Ví dụ: {\"summary\": \"Kết quả phân tích...\", \"score\": 0.95}";

        String requestBody;
        if ("google".equalsIgnoreCase(provider)) {
            Map<String, Object> requestBodyMap = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", systemPrompt + "\n\nNội dung cần phân tích:\n" + text)
                            ))
                    ),
                    "generationConfig", Map.of(
                            "temperature", 0.1,
                            "response_mime_type", "application/json"
                    )
            );
            requestBody = objectMapper.writeValueAsString(requestBodyMap);
        } else {
            Map<String, Object> requestBodyMap = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", text)
                    ),
                    "response_format", Map.of("type", "json_object"),
                    "temperature", 0.1
            );
            requestBody = objectMapper.writeValueAsString(requestBodyMap);
        }

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(endpoint + ("google".equalsIgnoreCase(provider) ? "?key=" + apiKey : "")))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));

        if (!"google".equalsIgnoreCase(provider)) {
            requestBuilder.header("Authorization", "Bearer " + apiKey);
        }

        HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        String content;
        if ("google".equalsIgnoreCase(provider)) {
            content = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } else {
            content = root.path("choices").get(0).path("message").path("content").asText();
        }
        
        JsonNode resultNode = objectMapper.readTree(content.replaceAll("```json", "").replaceAll("```", "").trim());
        
        return new AnalysisResult(
            taskName,
            resultNode.path("summary").asText("Không có tóm tắt"),
            resultNode.path("score").asDouble(0.0)
        );
    }
}
