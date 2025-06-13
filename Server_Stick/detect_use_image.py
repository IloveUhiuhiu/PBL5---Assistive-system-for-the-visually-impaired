import cv2
from utils import recognize_object
import numpy as np

image_path = r"static\images\z6418407686945_28a42b7905e1b1863d1e719e37bb5c32.jpg"
image = cv2.imread(image_path)
image = cv2.resize(image, (640, 640))
results = recognize_object.detect_object(image,0.25)
cv2.imshow('Object Detection', image)
cv2.waitKey(0)
cv2.destroyAllWindows()