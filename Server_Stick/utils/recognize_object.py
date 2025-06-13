import cv2
import os
from model_ai import YOLO_model
from utils.args import names, time_delays
import time

# Kích thước ảnh đầu vào
image_width, image_height = 320, 320
image_dir = 'static/images'

# Trả về class_id có độ tin cậy cao nhất
def get_most_confident_class(class_confidences):
    return max(
        class_confidences.items(),
        key=lambda item: item[1]
    )


# Ánh xạ số lượng và class sang file âm thanh & delay
def get_audio_info(class_count, class_id):
    audio_id = class_id * 4 + (class_count if class_count < 4 else 4)
    delay = time_delays[audio_id] + 1000
    return audio_id, delay


# Lưu ảnh và nhãn dưới định dạng YOLO
def save_detection_result(image,  class_name):
    timestamp = int(time.time())
    os.makedirs(image_dir, exist_ok=True)
    image_path = f'{image_dir}/{class_name}_{timestamp}.jpg'
    cv2.imwrite(image_path, image)


# Hàm chính nhận diện đối tượng
def detect_objects(image, confidence_threshold=0.5):
    class_confidences = {}
    class_counts = {}
    results = YOLO_model(image)

    result = results[0]

    # image_copy = image.copy()

    for box in result.boxes:
        class_id = int(box.cls[0].item())
        class_name = result.names[class_id]
        x1, y1, x2, y2 = [round(x) for x in box.xyxy[0].tolist()]
        confidence = box.conf[0].item()

        # Vẽ bounding box và label
        cv2.rectangle(image, (x1, y1), (x2, y2), (0, 255, 0), 2)
        cv2.putText(image, f"{class_name} {confidence * 100:.1f}%",
                    (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX,
                    0.5, (0, 255, 0), 2, cv2.LINE_AA)

        if confidence > confidence_threshold:
            class_counts[class_id] = class_counts.get(class_id, 0) + 1
            class_confidences[class_id] = max(class_confidences.get(class_id, 0), confidence)


    if class_counts:
        top_class_id, _ = get_most_confident_class(class_confidences)
        top_class_count = class_counts[top_class_id]
        if top_class_id == 4:
            return None
        audio_id, delay = get_audio_info(top_class_count, top_class_id)

        #save_detection_result(image_copy, result.names[top_class_id])
        
        return f"{audio_id} {delay}"
    else:
        return None
