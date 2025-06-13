import cv2
from utils import recognize_object
cap = cv2.VideoCapture(0)

if not cap.isOpened():
    print("Cannot open webcam")
    exit()

window_closed = False

while not window_closed:
    ret, frame = cap.read()

    if not ret:
        print("Cannot read frame (end of stream?)")
        break
    frame = cv2.resize(frame, (640, 640))
    results = recognize_object.detect_objects(frame,0.7)
    cv2.imshow('Object Detection', frame)

    # Exit the loop when 'q' is pressed
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

    if cv2.getWindowProperty('Object Detection', cv2.WND_PROP_VISIBLE) < 1:
        window_closed = True

cap.release()
cv2.destroyAllWindows()
