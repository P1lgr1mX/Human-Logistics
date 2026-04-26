from fastapi import FastAPI
from pydantic import BaseModel
import uvicorn
from transformers import pipeline
from pyvi import ViTokenizer
from datetime import datetime
from pathlib import Path
import csv
import threading

app = FastAPI()
LOG_FILE = Path(__file__).resolve().parent / "analyze_logs.csv"
LOG_LOCK = threading.Lock()

# --- KHỞI TẠO MÔ HÌNH ---
print("--- Đang nạp não AI vào RAM... ---")
model_name = "lxyuan/distilbert-base-multilingual-cased-sentiments-student"


def load_sentiment_pipeline():
    # Ưu tiên ONNX Runtime để chạy CPU nhẹ hơn nếu môi trường hỗ trợ.
    try:
        import onnxruntime  # noqa: F401
        from transformers import AutoTokenizer
        from optimum.onnxruntime import ORTModelForSequenceClassification

        tokenizer = AutoTokenizer.from_pretrained(model_name)
        ort_model = ORTModelForSequenceClassification.from_pretrained(
            model_name, from_transformers=True
        )
        print(f"--- Dùng ONNX Runtime cho model: {model_name} ---")
        return pipeline(
            "sentiment-analysis",
            model=ort_model,
            tokenizer=tokenizer,
            device=-1,
        )
    except Exception as ort_error:
        print(f"--- ONNX không khả dụng, fallback CPU thường: {ort_error} ---")

    try:
        return pipeline(
            "sentiment-analysis",
            model=model_name,
            device=-1,
            model_kwargs={"low_cpu_mem_usage": True},
        )
    except Exception as e:
        print(f"Lỗi khi nạp model tùy chọn: {e}")
        return pipeline("sentiment-analysis", device=-1)


sentiment_model = load_sentiment_pipeline()
print(f"--- Đã nạp xong model cảm xúc: {model_name} ---")

class SocialPost(BaseModel):
    content: str


def append_analyze_log(
    content: str,
    sentiment: str,
    confidence: float,
    damage_type: str,
    relief_type: str,
) -> None:
    fieldnames = [
        "timestamp",
        "content",
        "sentiment",
        "confidence",
        "damage_type",
        "relief_type",
    ]
    row = {
        "timestamp": datetime.now().isoformat(timespec="seconds"),
        "content": content,
        "sentiment": sentiment,
        "confidence": float(confidence),
        "damage_type": damage_type,
        "relief_type": relief_type,
    }
    with LOG_LOCK:
        file_exists = LOG_FILE.exists()
        with LOG_FILE.open("a", newline="", encoding="utf-8") as csvfile:
            writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
            if not file_exists:
                writer.writeheader()
            writer.writerow(row)

# DÁN ĐỐNG CODE CỦA BẠN VÀO ĐÂY:
@app.post("/analyze")
async def analyze_post(post: SocialPost):
    content = post.content.strip()
    text_lower = content.lower()
    
    # 1. Tách từ tiếng Việt
    processed_text = ViTokenizer.tokenize(content)
    
    # 2. Lớp chặn đầu (Rule-based) - Trị cái "ngu" của AI
    urgent_keywords = ["bão", "storm", "lũ", "ngập", "sập", "chết", "mất tích"]
    
    # AI phân tích
    result = sentiment_model(processed_text)[0]
    label = result['label'].lower()
    score = result['score']

    # Logic xử lý nhãn
    label_map = {"positive": "Tích cực", "negative": "Tiêu cực", "neutral": "Trung lập"}
    sentiment_vn = label_map.get(label, "Trung lập")

    # FIX: Nếu có từ khóa thiên tai mà AI bảo "Tích cực" -> Ép về "Tiêu cực"
    if any(word in text_lower for word in urgent_keywords):
        if sentiment_vn == "Tích cực" or score < 0.45:
            sentiment_vn = "Tiêu cực"
            score = 0.99  # Ép độ tin cậy cao

    # 3. Phân loại thiệt hại
    damage_type = "Khác"
    damage_map = {
        "Người bị ảnh hưởng": ["người", "tử vong", "thương vong", "mất tích", "nạn nhân", "cứu"],
        "Cơ sở hạ tầng": ["cầu", "đường", "điện", "trạm", "giao thông", "ngập"],
        "Nhà cửa": ["nhà", "mái", "sập", "tòa nhà"],
        "Tài sản": ["xe", "ô tô", "xe máy", "mất đồ"]
    }
    
    for category, keys in damage_map.items():
        if any(k in text_lower for k in keys):
            damage_type = category
            break

    # 4. Phân loại hàng cứu trợ (Bài toán 3)
    relief_type = "Khác"
    relief_map = {
        "Tiền mặt": ["tiền mặt", "hỗ trợ tiền", "tiền", "cash", "chuyển khoản"],
        "Thực phẩm": ["gạo", "mì", "thực phẩm", "đồ ăn", "lương thực", "nước uống"],
        "Y tế": ["thuốc", "y tế", "băng gạc", "cứu thương", "bệnh viện", "medical"],
        "Chỗ ở": ["lều", "chỗ ở", "nhà tạm", "trú ẩn", "shelter", "tái định cư"],
        "Giao thông": ["xe tải", "xe cứu trợ", "giao thông", "vận chuyển", "đường", "cầu"],
    }

    for category, keys in relief_map.items():
        if any(k in text_lower for k in keys):
            relief_type = category
            break

    append_analyze_log(
        content=content,
        sentiment=sentiment_vn,
        confidence=score,
        damage_type=damage_type,
        relief_type=relief_type,
    )

    return {
        "sentiment": sentiment_vn,
        "damage_type": damage_type,
        "relief_type": relief_type,
        "confidence": float(score)
    }

if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8000)
