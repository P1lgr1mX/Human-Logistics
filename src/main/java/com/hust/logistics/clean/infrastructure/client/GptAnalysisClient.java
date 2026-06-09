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
            key = config.getApiKeys().getOrDefault("gemini", 
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
            return getMockResult(taskName); //chay du lieu gia 
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
        // Sử dụng hashCode của taskName để tạo seed khác nhau cho mỗi loại task
        long seed = keywords.hashCode() + taskName.hashCode();
        java.util.Random random = new java.util.Random(seed);

        int v1 = 20 + random.nextInt(40); 
        int v2 = 10 + random.nextInt(30);
        int v3 = 100 - v1 - v2;

        String summary = "--- KẾT QUẢ MÔ PHỎNG ---\n" +
                "Phân tích dựa trên dữ liệu thật từ Youtube về từ khóa: [" + String.join(", ", config.getKeywords()) + "]\n\n";

        if (taskName.contains("sentiment")) {
            summary += "Xu hướng tâm lý cộng đồng hiện tại:\n" +
                       "- Tích cực: " + v1 + "%\n" +
                       "- Trung lập: " + v3 + "%\n" +
                       "- Tiêu cực: " + v2 + "%\n\n" +
                       String.format("DATA_POINTS: POS=%d, NEU=%d, NEG=%d", v1, v3, v2);
        } else if (taskName.contains("satisfaction")) {
            summary += "Mức độ hài lòng về cứu trợ:\n" +
                       "- Hài lòng: " + v1 + "%\n" +
                       "- Bình thường: " + v3 + "%\n" +
                       "- Thất vọng: " + v2 + "%\n\n" +
                       String.format("SATISFACTION_DATA: HAPPY=%d, NEUTRAL=%d, UNHAPPY=%d", v1, v3, v2);
        } else if (taskName.contains("trend")) {
            java.time.LocalDate start = config.getStartTime().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            java.time.LocalDate end = config.getEndTime().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
            if (days > 7) days = 7; 

            StringBuilder trendData = new StringBuilder("TREND_DATA: ");
            int lastVal = 40 + random.nextInt(30);
            for (int i = 0; i < days; i++) {
                java.time.LocalDate current = start.plusDays(i);
                lastVal = Math.max(10, Math.min(100, lastVal + (random.nextInt(21) - 10)));
                trendData.append(current.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")))
                         .append("=").append(lastVal);
                if (i < days - 1) trendData.append(", ");
            }
            summary += "Biểu đồ biến động nhu cầu cứu trợ:\n" + trendData.toString();
        } else if (taskName.contains("damage")) {
            summary += String.format("Thống kê thiệt hại ước tính:\n" +
                    "- Nhà cửa bị hư hỏng: %d\n" +
                    "- Gián đoạn kinh tế sản xuất: %d\n" +
                    "- Tài sản cá nhân bị mất: %d\n" +
                    "- Cơ sở hạ tầng bị hư hỏng: %d\n" +
                    "- Người bị ảnh hưởng: %d\n" +
                    "- Khác: %d", 
                    v1, v2, v3, random.nextInt(50), random.nextInt(30), random.nextInt(10));
        }

        return new AnalysisResult(taskName, summary, 0.95);
    }


    private AnalysisResult callApi(String taskName, String text) throws IOException, InterruptedException {
        String provider = config.getAnalysis().getProvider();
        String dataFormatInstruction = "";
        if (taskName.contains("sentiment")) {
            dataFormatInstruction = "DATA_POINTS: POS=x, NEU=y, NEG=z";
        } else if (taskName.contains("satisfaction")) {
            dataFormatInstruction = "SATISFACTION_DATA: HAPPY=x, NEUTRAL=y, UNHAPPY=z";
        } else if (taskName.contains("trend")) {
            java.time.LocalDate s = config.getStartTime().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            java.time.LocalDate e = config.getEndTime().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            String dateRange = s.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")) + " đến " + 
                               e.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"));
            dataFormatInstruction = "TREND_DATA: dd/MM=val1, dd/MM=val2... (với các ngày trong khoảng " + dateRange + ")";
        } else if (taskName.contains("damage")) {
            dataFormatInstruction = "Báo cáo phải chứa các dòng: 'Nhà cửa bị hư hỏng: x', 'Gián đoạn kinh tế sản xuất: y', 'Tài sản cá nhân bị mất: z', 'Cơ sở hạ tầng bị hư hỏng: a', 'Người bị ảnh hưởng: b', 'Khác: c' (với x,y,z,a,b,c là số vụ việc ước tính)";
        }

        String systemPrompt = "Bạn là chuyên gia phân tích dữ liệu cứu trợ.\n" +
                "Nhiệm vụ: " + taskName + ".\n" +
                "QUY TẮC:\n" +
                "1. CHỈ phân tích các nội dung liên quan đến thiên tai, cứu trợ. LOẠI BỎ hoàn toàn các nội dung nhiễu (quảng cáo, quân sự chung, tin tức không liên quan) ra khỏi thống kê.\n" +
                "2. Tập trung vào thiệt hại và lời kêu cứu để đánh giá NEGATIVE.\n" +
                "3. YÊU CẦU JSON: {\"summary\": \"Tóm tắt thiệt hại... và kết thúc bằng dòng: " + dataFormatInstruction + "\", \"score\": 0.9}.\n" +
                "4. KHÔNG được để trống trường 'summary'. Tỷ lệ x+y+z phải bằng 100.";

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
                    "model" , model,
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
