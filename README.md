Được rồi, tôi sẽ lược bỏ các icon và trình bày lại theo phong cách tối giản, chuyên nghiệp và tập trung vào cấu trúc kỹ thuật để nhóm của bạn dễ theo dõi nhất.

Bạn copy nội dung này vào file README.md ở thư mục gốc nhé:
Hệ Thống Quản Lý và Phân Tích Logistics Nhân Đạo

Dự án này là công cụ hỗ trợ điều phối cứu trợ dựa trên phân tích dữ liệu mạng xã hội trong các tình huống thiên tai. Hệ thống tích hợp xử lý ngôn ngữ tự nhiên (NLP) để phân loại thiệt hại và đánh giá tâm lý người dân theo thời gian thực.
Đội ngũ phát triển

    Nguyễn Huy Quang (ssh1131): Trưởng nhóm, Phát triển ứng dụng Java, Thiết kế hệ thống.

    Nguyễn Minh Trung: Kỹ sư AI, Phân tích dữ liệu.

    Trịnh Đặng Việt Anh: Phát triển Crawler, Tiền xử lý dữ liệu.

    Trương Đức Hoàng: Thiết kế giao diện (UI), Kiểm thử phần mềm.

Cấu trúc thư mục

Dự án được tổ chức thành hai thành phần độc lập:

    /humanitarian-logistics: Mã nguồn ứng dụng Desktop (Java Maven).

    /humanitarian-logistics-ai: Dịch vụ phân tích cảm xúc và phân loại dữ liệu (Python FastAPI).

Hướng dẫn cài đặt và Vận hành
1. Khởi chạy Dịch vụ AI (Backend)

Yêu cầu: Python 3.9 trở lên.

    Truy cập thư mục: cd humanitarian-logistics-ai

    Tạo môi trường ảo: python -m venv venv

    Kích hoạt môi trường:

        Linux/macOS: source venv/bin/activate

        Windows: venv\Scripts\activate

    Cài đặt thư viện: pip install -r requirements.txt

    Chạy server: python main.py
    Địa chỉ mặc định: http://127.0.0.1:8000

2. Khởi chạy Ứng dụng Dashboard (Frontend)

Yêu cầu: JDK 17 và Maven.

    Truy cập thư mục: cd humanitarian-logistics

    Biên dịch dự án: mvn clean compile

    Chạy ứng dụng: mvn exec:java -Dexec.mainClass="com.hust.logistics.MainApp"