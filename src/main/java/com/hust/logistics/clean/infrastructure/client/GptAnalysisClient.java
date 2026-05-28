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
        // Kiểm tra nếu API Key chưa được cấu hình
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("API_KEY_HERE")) {
            return getMockResult(taskName);
        }

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
                    return new AnalysisResult(taskName, "Lỗi API sau " + maxRetries + " lần thử: " + e.getMessage() + 
                            "\n\n(Gợi ý: Hãy kiểm tra API Key trong application.yml hoặc sử dụng chế độ Mock)", 0.0);
                }
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        }
        return new AnalysisResult(taskName, "Lỗi không xác định", 0.0);
    }

    private AnalysisResult getMockResult(String taskName) {
        String keywords = String.join("", config.getKeywords());
        long seed = 0;
        for (char c : keywords.toCharArray()) seed += c;
        java.util.Random random = new java.util.Random(seed + taskName.length());

        int v1 = 20 + random.nextInt(60); 
        int v2 = 5 + random.nextInt(100 - v1 - 10);
        int v3 = 100 - v1 - v2;

        String summary = "--- KẾT QUẢ MÔ PHỎNG ---\n" +
                "Hệ thống phân tích dựa trên từ khóa: [" + keywords + "]\n\n";

        if (taskName.equals("task-sentiment")) {
            summary += String.format("DATA_POINTS: POS=%d, NEU=%d, NEG=%d", v1, v3, v2);
        } else if (taskName.equals("task-satisfaction")) {
            summary += String.format("SATISFACTION_DATA: HAPPY=%d, NEUTRAL=%d, UNHAPPY=%d", v1, v3, v2);
        } else if (taskName.equals("task-trend")) {
            java.time.LocalDate start = config.getStartTime().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            java.time.LocalDate end = config.getEndTime().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
            if (days > 10) days = 10; // Giới hạn hiển thị 10 điểm để biểu đồ đẹp

            StringBuilder trendData = new StringBuilder("TREND_DATA: ");
            int lastVal = 30 + random.nextInt(40);
            for (int i = 0; i < days; i++) {
                java.time.LocalDate current = start.plusDays(i);
                // Làm mượt dữ liệu: giá trị sau phụ thuộc vào giá trị trước
                int change = random.nextInt(20) - 10; 
                lastVal = Math.max(10, Math.min(100, lastVal + change));

                trendData.append(current.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")))
                         .append("=")
                         .append(lastVal);
                if (i < days - 1) trendData.append(", ");
            }
            summary += trendData.toString();
        }
 else if (taskName.equals("task-damage")) {
            summary += String.format("Báo cáo thiệt hại:\n- Nhà cửa: %d vụ\n- Hạ tầng: %d vụ\n- Kinh tế: %d vụ", v1/10, v2/10, v3/10);
        }

        return new AnalysisResult(taskName, summary, 0.99);
    }


    private AnalysisResult callApi(String taskName, String text) throws IOException, InterruptedException {
        String provider = config.getAnalysis().getProvider();
        String dataFormatInstruction = "";
        if (taskName.equals("task-sentiment")) {
            dataFormatInstruction = "DATA_POINTS: POS=x, NEU=y, NEG=z";
        } else if (taskName.equals("task-satisfaction")) {
            dataFormatInstruction = "SATISFACTION_DATA: HAPPY=x, NEUTRAL=y, UNHAPPY=z";
        } else if (taskName.equals("task-trend")) {
            java.time.LocalDate s = config.getStartTime().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            java.time.LocalDate e = config.getEndTime().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            String dateRange = s.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")) + " đến " + 
                               e.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"));
            dataFormatInstruction = "TREND_DATA: dd/MM=val1, dd/MM=val2... (với các ngày trong khoảng " + dateRange + ")";
        } else {
             dataFormatInstruction = "Báo cáo chi tiết dạng văn bản.";
        }

        String systemPrompt = "Bạn là một hệ thống AI phân tích dữ liệu cứu trợ nhân đạo chuyên nghiệp.\n" +
                "Nhiệm vụ của bạn là phân tích văn bản đầu vào và thực hiện nhiệm vụ: " + taskName + ".\n" +
                "YÊU CẦU BẮT BUỘC:\n" +
                "1. Trả về định dạng JSON với các trường: \"summary\" và \"score\".\n" +
                "2. Trong trường \"summary\", bạn PHẢI thêm dòng dữ liệu sau ở cuối cùng:\n" +
                dataFormatInstruction + "\n" +
                "(Trong đó x, y, z là tỷ lệ phần trăm ước tính, tổng bằng 100).";

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

        HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
        
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
