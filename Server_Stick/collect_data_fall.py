import cv2
import asyncio
import websockets
import pandas as pd
DATA_QUEUE = asyncio.Queue(maxsize=100)
results = pd.DataFrame(columns=['Accelerometer: x-axis (g)', 'Accelerometer: y-axis (g)', 'Accelerometer: z-axis (g)', 'Gyroscope: x-axis (rad/s)', 'Gyroscope: y-axis (rad/s)', 'Gyroscope: z-axis (rad/s)'])
async def recv_data(websocket):
    try:
        count = 1
        while True:
            data = await websocket.recv()
            data = data.split(',')
            print(f"Đã nhận dữ liệu: {count} {data}")
            if len(data) == 6:
                data_dict = {
                    'Accelerometer: x-axis (g)': float(data[0]),
                    'Accelerometer: y-axis (g)': float(data[1]),
                    'Accelerometer: z-axis (g)': float(data[2]),
                    'Gyroscope: x-axis (rad/s)': float(data[3]),
                    'Gyroscope: y-axis (rad/s)': float(data[4]),
                    'Gyroscope: z-axis (rad/s)': float(data[5])
                }
                data_frame = pd.DataFrame([data_dict])
                count += 1
                if not DATA_QUEUE.full():
                    await DATA_QUEUE.put(data_frame)
    except websockets.ConnectionClosed:
        print("Client đã ngắt kết nối...!")
    except Exception as e:
        print(f"Lỗi khi nhận ảnh: {e}")

async def process_data(websocket):
    global results
    try:
        while True:
            data = await DATA_QUEUE.get()
            if not data.empty:
                results = pd.concat([results, data], ignore_index=True)
            DATA_QUEUE.task_done()
    except Exception as e:
        print(f"Lỗi khi xử lý data: {e}")

async def handler(websocket):
    global results
    try:
        recv_task = asyncio.create_task(recv_data(websocket))
        process_task = asyncio.create_task(process_data(websocket))
        await asyncio.gather(recv_task, process_task)
    except Exception as e:
        print(f"Lỗi handler: {e}")
        
    finally:

        results.to_csv('te_nga/nga_trai_phai/fall_data_360.csv', index=False)


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
    finally:
        cv2.destroyAllWindows()
