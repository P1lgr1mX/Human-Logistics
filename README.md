🚀 Hướng dẫn cài đặt & Chạy dự án
1. Khởi động AI Service (Não bộ)

Yêu cầu: Python 3.9+
Bash

cd humanitarian-logistics-ai
python -m venv venv
source venv/bin/activate  # Linux/macOS
# venv\Scripts\activate   # Windows
pip install -r requirements.txt
python main.py

Server sẽ chạy tại: http://127.0.0.1:8000
2. Khởi động Java Dashboard (Giao diện)

Yêu cầu: JDK 17+, Maven
Bash

cd humanitarian-logistics
mvn clean compile
mvn exec:java -Dexec.mainClass="com.hust.logistics.MainApp"

📊 Các tính năng chính

    Bài toán 1: Phân tích biến động tâm lý người dân theo thời gian (Line Chart).

    Bài toán 2: Phân loại các nhóm thiệt hại: Sập nhà, ngập lụt, sạt lở (Bar Chart).

    Bài toán 3 & 4: Theo dõi điều phối hàng cứu trợ và mức độ hài lòng của khu vực nhận hỗ trợ.