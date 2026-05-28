
  📦 Humanitarian Logistics AI Analysis Dashboard 🚀
  > Đề tài: Ứng dụng dữ liệu mạng xã hội để nâng cao hiệu quả trong Logistics nhân đạo.

  ![Java Version (https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)
  ![Framework (https://img.shields.io/badge/Framework-Spring%20Boot%203.2-green.svg)](https://spring.io/projects/spring-boot)
  ![UI (https://img.shields.io/badge/UI-JavaFX%2021-blue.svg)](https://openjfx.io/)
  ![AI (https://img.shields.io/badge/AI-Google%20Gemini-red.svg)](https://aistudio.google.com/)

  ---

  🌟 Giới thiệu Dự án
  Hệ thống phân tích dữ liệu mạng xã hội (Social Media Mining) sử dụng trí tuệ nhân tạo để hỗ trợ điều phối Logistics trong các thảm họa thiên nhiên (như siêu bão Yagi).
  Ứng dụng giúp chuyển hóa hàng ngàn bài đăng từ Facebook, X, TikTok thành các biểu đồ trực quan, giúp các tổ chức cứu trợ đưa ra quyết định chính xác và kịp thời.

  ---

  ✨ Tính năng cốt lõi

  1. 📊 Phân tích Tâm lý (Sentiment Analysis)
   * Theo dõi thái độ cộng đồng (Tích cực / Tiêu cực / Trung lập).
   * Biểu đồ: BarChart đa màu sắc.
   * Giá trị: Nhận diện sớm sự hoảng loạn hoặc các tín hiệu tích cực từ vùng lũ.

  2. 🏠 Đánh giá Thiệt hại (Damage Assessment)
   * Thống kê số lượng vụ việc theo danh mục: Nhà cửa, Hạ tầng, Kinh tế, Con người.
   * Biểu đồ: BarChart (Tông màu cam cứu hộ).
   * Giá trị: Ưu tiên khu vực cần cứu trợ khẩn cấp dựa trên mức độ hư hại.

  3. 🍕 Mức độ Hài lòng Cứu trợ (Relief Satisfaction)
   * Đánh giá phản hồi về các nhu yếu phẩm: Thực phẩm, Y tế, Tiền mặt...
   * Biểu đồ: PieChart (Biểu đồ tròn trực quan).
   * Giá trị: Đo lường hiệu quả của công tác phân phối hàng cứu trợ.

  4. 📈 Theo dõi Xu hướng (Trend Monitoring)
   * Quan sát sự biến động của dữ liệu theo khoảng thời gian tùy chọn.
   * Biểu đồ: LineChart (Biểu đồ đường mượt mà).
   * Giá trị: Dự báo nhu cầu và theo dõi tiến độ phục hồi sau thảm họa.

  ---

  🛠 Công nghệ sử dụng
   * Backend: Java 17, Spring Boot (Dependency Injection, Bean Management).
   * Frontend: JavaFX, Scene Builder, FXML, CSS.
   * AI Integration: Google Gemini AI API (Phân tích ngôn ngữ tự nhiên).
   * Data Handling: Regex Parsing, JSON Processing (Jackson).
   * Architecture: Clean Architecture, Interface-based Design.

  ---

  🚀 Hướng dẫn Cài đặt & Chạy ứng dụng

  1. Yêu cầu hệ thống
   * Java JDK 17 trở lên.
   * Maven 3.6 trở lên.

  2. Cấu hình API Key (Tùy chọn)
  Mặc định ứng dụng chạy ở Chế độ Mô phỏng (Mock Mode). Để dùng AI thật:
   1. Mở file src/main/resources/application.yml.
   2. Thay API_KEY_HERE bằng key lấy từ Google AI Studio (https://aistudio.google.com/).

  3. Lệnh chạy ứng dụng
  Mở Terminal tại thư mục gốc và gõ:
   1 mvn clean spring-boot:run

  ---

  📐 Kiến trúc OOP Linh hoạt
  Dự án được thiết kế để dễ dàng mở rộng theo yêu cầu đề tài:
   * Dễ thêm nguồn dữ liệu: Chỉ cần triển khai thêm Interface SocialMediaCrawler.
   * Dễ đổi mô hình AI: Có thể thay Gemini bằng Python API thông qua Interface AnalysisClient.
   * Dễ đổi bài toán: Các Task được tách biệt hoàn toàn trong package application.task.

  ---
  © 2026 - Nhóm Phát triển Hệ thống Logistics Nhân đạo 🇻🇳
