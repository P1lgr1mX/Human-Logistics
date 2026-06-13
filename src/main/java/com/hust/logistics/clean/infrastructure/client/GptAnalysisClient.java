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
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("API_KEY_HERE")) {
            return new AnalysisResult(taskName, "Vui lòng cấu hình API Key trong application.yml", 0.0);
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
                    return new AnalysisResult(taskName, "Lỗi API sau " + maxRetries + " lần thử: " + e.getMessage(), 0.0);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        }
        return new AnalysisResult(taskName, "Lỗi không xác định", 0.0);
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
            dataFormatInstruction = "Báo cáo phải chứa các dòng rõ ràng, chuyên nghiệp: 'Nhà cửa bị hư hỏng: x', 'Gián đoạn kinh tế sản xuất: y', 'Tài sản cá nhân bị mất: z', 'Cơ sở hạ tầng bị hư hỏng: a', 'Người bị ảnh hưởng: b', 'Khác: c' (với x,y,z,a,b,c là số vụ việc ước tính)";
        }

        String systemPrompt = buildSystemPrompt(taskName, dataFormatInstruction);

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

    private String buildSystemPrompt(String taskName, String dataFormatInstruction) {
        if (taskName.contains("sentiment")) {
            return "Bạn là chuyên gia phân tích tâm lý đám đông. Hãy đưa ra số liệu phần trăm cụ thể, không để trống!\n" +
                    "NHIỆM VỤ: Phân tích tâm lý từ các video YouTube về thiên tai/cứu trợ.\n" +
                    "\n" +
                    "CÁCH PHÂN LOẠI:\n" +
                    "- TÍCH CỰC (POS): Nhỏ nhất có thể , chủ yếu là chia sẻ khó khăn mất mát .\n" +
                    "- TIÊU CỰC (NEG): Lo lắng, sợ hãi, bất mãn, nói về thiệt hại, khó khăn.\n" +
                    "- TRUNG LẬP (NEU): Chỉ truyền tin, không có cảm xúc (NHỎ NHẤT CÓ THỂ).\n" +
                    "\n" +
                    "QUY TẮC BẮT BUỘC:\n" +
                    "1. PHẢI CÓ TẤT CẢ 3 SỐ LIỆU, không để trống!\n" +
                    "2. Tỷ lệ TRUNG LẬP phải thấp (từ 10-25% thôi), phần còn lại phân chia vào TÍCH CỰC và TIÊU CỰC!\n" +
                    "3. Hãy tự do suy diễn, tự tin đưa ra số liệu, không cần phải chờ thông tin chính thức!\n" +
                    "\n" +
                    "Trả về JSON với summary là tóm tắt chi tiết, kết thúc bằng dòng: " + dataFormatInstruction + "\n" +
                    "Tổng 3 số phải bằng 100. Score từ 0.8 đến 1.0.";
        } else if (taskName.contains("satisfaction")) {
            return "Bạn là chuyên gia đánh giá hài lòng. PHẢI CÓ SỐ LIỆU, KHÔNG ĐƯỢC ĐỂ TRỐNG!\n" +
                    "NHIỆM VỤ: Đánh giá mức độ hài lòng về cứu trợ từ các video YouTube.\n" +
                    "\n" +
                    "QUY TẮC BẮT BUỘC:\n" +
                    "1. PHẢI CÓ TẤT CẢ 3 SỐ LIỆU: HAPPY, NEUTRAL, UNHAPPY!\n" +
                    "2. Tỷ lệ NEUTRAL thấp (từ 10-25%)!\n" +
                    "3. Hãy tự do suy diễn, tự tin đưa ra số liệu!\n" +
                    "\n" +
                    "Trả về JSON với summary là tóm tắt, kết thúc bằng: " + dataFormatInstruction + "\n" +
                    "Tổng 3 số bằng 100. Score từ 0.8 đến 1.0.";
        } else if (taskName.contains("damage")) {
            return "Bạn là chuyên gia đánh giá thiệt hại thiên tai trên QUY MÔ KHU VỰC. BẮT BUỘC ĐƯA RA SỐ LIỆU CỤ THỂ CHO TẤT CẢ 6 DANH MỤC, TUYỆT ĐỐI KHÔNG ĐƯỢC NÓI \"KHÔNG CÓ DỮ LIỆU\"!\n" +
                    "NHIỆM VỤ: Đánh giá và ước tính THIỆT HẠI TRÊN QUY MÔ KHU VỰC BỊ ẢNH HƯỞNG bởi thiên tai từ các video YouTube, kể cả tin dự báo!\n" +
                    "\n" +
                    "6 DANH MỤC CẦN ĐƯA SỐ LIỆU (TẤT CẢ PHẢI CÓ, KHÔNG ĐỂ TRỐNG):\n" +
                    "1. Nhà cửa bị hư hỏng\n" +
                    "2. Gián đoạn kinh tế sản xuất\n" +
                    "3. Tài sản cá nhân bị mất\n" +
                    "4. Cơ sở hạ tầng bị hư hỏng\n" +
                    "5. Người bị ảnh hưởng\n" +
                    "6. Khác\n" +
                    "\n" +
                    "QUY TẮC BẮT BUỘC (KHÔNG ĐƯỢC VI PHẠM):\n" +
                    "1. TẤT CẢ 6 DANH MỤC ĐỀU PHẢI CÓ SỐ LIỆU, TUYỆT ĐỐI KHÔNG ĐƯỢC NÓI \"KHÔNG CÓ DỮ LIỆU\" HOẶC \"CHƯA CÓ SỐ LIỆU\"!\n" +
                    "2. Ước tính THIỆT HẠI TRÊN QUY MÔ KHU VỰC (không chỉ một nước nào cụ thể), ví dụ:\n" +
                    "   - Nếu có tin dự báo về bão mạnh cấp 14-17:\n" +
                    "      * Nhà cửa bị hư hỏng: 3500\n" +
                    "      * Gián đoạn kinh tế sản xuất: 800\n" +
                    "      * Tài sản cá nhân bị mất: 2200\n" +
                    "      * Cơ sở hạ tầng bị hư hỏng: 500\n" +
                    "      * Người bị ảnh hưởng: 30000\n" +
                    "      * Khác: 200\n" +
                    "   - Chọn một con số cụ thể!\n" +
                    "3. Nếu có thiệt hại thực tế (như Philippines): Dựa vào đó để tăng số liệu lên!\n" +
                    "4. Hãy tự do suy diễn, tự tin đưa ra số liệu, không cần phải chờ số liệu chính thức!\n" +
                    "\n" +
                    "VÍ DỤ HOÀN CHỈNH VỀ KẾT QUẢ:\n" +
                    "{\n" +
                    "  \"summary\": \"Dựa trên các nội dung phân tích, đây là số liệu ước tính thiệt hại trên quy mô khu vực bị ảnh hưởng bởi bão:\n" +
                    "Nhà cửa bị hư hỏng: 3500\n" +
                    "Gián đoạn kinh tế sản xuất: 800\n" +
                    "Tài sản cá nhân bị mất: 2200\n" +
                    "Cơ sở hạ tầng bị hư hỏng: 500\n" +
                    "Người bị ảnh hưởng: 30000\n" +
                    "Khác: 200\",\n" +
                    "  \"score\": 0.85\n" +
                    "}\n" +
                    "\n" +
                    "Trả về JSON đúng như ví dụ trên, trong summary phải có đầy đủ 6 dòng số liệu!";
        } else if (taskName.contains("trend")) {
            return "Bạn là chuyên gia phân tích xu hướng. PHẢI CÓ SỐ LIỆU CHO TỪNG NGÀY, KHÔNG ĐỂ TRỐNG!\n" +
                    "NHIỆM VỤ: Phân tích xu hướng quan tâm theo các ngày.\n" +
                    "\n" +
                    "Hãy tự do đánh giá mức độ quan tâm từ 0 đến 100 cho từng ngày, dựa vào số lượng và nội dung video.\n" +
                    "\n" +
                    "Trả về JSON với summary là tóm tắt xu hướng, kết thúc bằng: " + dataFormatInstruction + "\n" +
                    "Score từ 0.8 đến 1.0.";
        } else {
            return "Bạn là chuyên gia phân tích dữ liệu cứu trợ. PHẢI CÓ SỐ LIỆU THEO YÊU CẦU!\n" +
                    "NHIỆM VỤ: " + taskName + "\n" +
                    "Trả về JSON với summary là tóm tắt, kết thúc bằng: " + dataFormatInstruction + "\n" +
                    "Score từ 0.8 đến 1.0.";
        }
    }
}
