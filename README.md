

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
