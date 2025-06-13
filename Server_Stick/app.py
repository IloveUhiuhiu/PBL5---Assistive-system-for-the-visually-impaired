import cv2
import asyncio
import websockets
import numpy as np
import time
from utils.recognize_object import detect_objects
from utils.args import names_print, time_delays

FRAME_QUEUE = asyncio.Queue(maxsize=100)

async def recv_image(websocket):
    try:
        while True:
            img_data = await websocket.recv()
            np_arr = np.frombuffer(img_data, dtype=np.uint8)
            frame = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)
            if frame is not None:
                frame = cv2.resize(frame, (320, 320), interpolation=cv2.INTER_AREA)
                if not FRAME_QUEUE.full():
                    await FRAME_QUEUE.put(frame)
    except websockets.ConnectionClosed:
        print("Client đã ngắt kết nối...!")
    except Exception as e:
        print(f"Lỗi khi nhận ảnh: {e}")

async def process_and_send_images(websocket):
    last_time = int(time.time() * 1000)
    global FRAME_QUEUE
    
    interval_time = 6000
    last_result = None
    try:
        while True:
            frame = await FRAME_QUEUE.get()
            if frame is not None:
                result = detect_objects(frame, 0.5)
                current_time = int(time.time() * 1000)
                if result:
                    file_id = int(result.split()[0])
                    print(f"[INFOR]Class Id của đối tượng: {(file_id -  1) // 4}")
                    print(f"[INFOR]Phát hiện đối tượng: {names_print[(file_id - 1) // 4]}")
                    if file_id != last_result:
                        print(f"[INFOR]Phát hiện đối tượng mới!!!")
                        last_result = file_id
                        last_time = current_time
                        await websocket.send(result)
                    elif current_time - last_time >= interval_time:
                        print(f"[INFOR]Quá thời gian giới hạn, cảnh báo vật cản!!!")
                        last_time = current_time
                        await websocket.send(result) 
                else:
                    last_result = None
                    print("[INFOR]Không phát hiện đối tượng nào") 
                cv2.imshow("Video Monitor", frame)
                if cv2.waitKey(1) & 0xFF == ord('q'):
                    break
                FRAME_QUEUE.task_done()
           
    except websockets.ConnectionClosed:
        print("Client đã ngắt kết nối...!")
    except Exception as e:
        print(f"Lỗi khi gửi ảnh: {e}")


async def handler(websocket):
    try:
        recv_task = asyncio.create_task(recv_image(websocket))
        process_and_send_task = asyncio.create_task(process_and_send_images(websocket))
        await asyncio.gather(recv_task, process_and_send_task)
    except Exception as e:
        print(f"Lỗi handler: {e}")

async def main():
    try:
        server = await websockets.serve(
            handler,
            "0.0.0.0",
            5000,
            ping_interval=20,
            ping_timeout=10
        )
        print("Server đang chạy tại ws://0.0.0.0:5000")
        await server.wait_closed()
    except Exception as e:
        print(f"Lỗi server: {e}")

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("Server đã dừng thành công")
