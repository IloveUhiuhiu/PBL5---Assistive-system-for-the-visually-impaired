import os
import pandas as pd
import matplotlib.pyplot as plt

folder_path = 'te_nga_out'

def plot_each_fall_separately(folder_path):
    if not os.path.exists(folder_path):
        print(f"Folder {folder_path} does not exist.")
        return

    files = [f for f in os.listdir(folder_path) if f.endswith('.csv')]
    if not files:
        print("No CSV files found in the folder.")
        return

    required_columns = ['Accelerometer: x-axis (g)', 'Accelerometer: y-axis (g)', 'Accelerometer: z-axis (g)']

    for file in files:
        csv_path = os.path.join(folder_path, file)
        try:
            data = pd.read_csv(csv_path)
            if not all(col in data.columns for col in required_columns):
                print(f"File {file} skipped: missing required columns.")
                continue

            # Mỗi file là một figure riêng
            plt.figure(figsize=(12, 6))
            plt.plot(data['Accelerometer: x-axis (g)'], label='X-axis', color='r')
            plt.plot(data['Accelerometer: y-axis (g)'], label='Y-axis', color='g')
            plt.plot(data['Accelerometer: z-axis (g)'], label='Z-axis', color='b')

            plt.title(f'Accelerometer Data - {file}')
            plt.xlabel('Sample Index')
            plt.ylabel('Acceleration (g)')
            plt.legend()
            plt.grid(True)
            plt.tight_layout()
            plt.show()

        except Exception as e:
            print(f"Error reading {file}: {e}")

plot_each_fall_separately(folder_path)
