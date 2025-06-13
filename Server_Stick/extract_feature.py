import numpy as np
from scipy.stats import kurtosis, skew, entropy
# import pytest
from scipy.fftpack import rfft, rfftfreq
from datetime import datetime as dt



def tdom(inpt,ftList):
    output = [] #the array we'll be returning:
    #[mean,standardDeviation,rootMeanSquare,maxAmp,minAmp,median,numZero-cross,
    #skewness,kurtosis,Q1,Q3,autocorrelation]
    rms = 0 #root mean square
    amp = [abs(inpt[i]) for i in range(0,len(inpt))] #to store amplitudes
    #variables used to count when zero is crossed
    bn = 1
    nzc = 0
    bnd = False
    bnF = False
    for i in range(0, len(inpt)):
        rms = rms + (inpt[i]**2)
        #this is to see when zero is being crossed:
        if (inpt[i] != 0):
            bnd = True
            if(bnF == False):
                bnF = True
                bn = inpt[i]/abs(inpt[i])
        else:
            bnd = False
        if (bnd):
            sgn = inpt[i]/abs(inpt[i])
            if (sgn != bn):
                nzc = nzc + 1
            bn = inpt[i]/abs(inpt[i])
            
    for feature in ftList:
        if feature == 'Mean':
            output.append(np.mean(inpt))
        elif feature == 'StandardDeviation':
            output.append(np.std(inpt))
        elif feature == 'RootMeanSquare':
            output.append((np.sqrt(rms) / np.sqrt(len(inpt))))
        elif feature == 'MaximalAmplitude':
            output.append("{:.6f}".format((max(amp))))
        elif feature == 'MinimalAmplitude':
            output.append(min(amp))
        elif feature == 'Median':
            output.append(np.median(inpt))
        elif feature == 'Number of zero-crossing':
            output.append(nzc)
        elif feature == 'Skewness':
            output.append(skew(inpt))
        elif feature == 'Kurtosis':
            output.append(kurtosis(inpt))
        elif feature == 'First Quartile':
            output.append(np.percentile(inpt,25,interpolation = 'midpoint'))
        elif feature == 'Third Quartile':
            output.append(np.percentile(inpt,75,interpolation = 'midpoint'))
        elif feature == 'Autocorrelation':
            kt = np.correlate(inpt,inpt,mode='full')
            output.append(np.median(kt))
    return output


def getSensors():
    sensorList = [ 
            'Accelerometer: x-axis (g)','Accelerometer: y-axis (g)',
            'Accelerometer: z-axis (g)','Gyroscope: x-axis (rad/s)',
            'Gyroscope: y-axis (rad/s)','Gyroscope: z-axis (rad/s)']  


    return sensorList

def getFeatures():
    ftList = [
        'Mean',
        'StandardDeviation',
        'RootMeanSquare',
        'MaximalAmplitude',
        'MinimalAmplitude',
        'Median',
        'Number of zero-crossing',
        'Skewness',
        'Kurtosis',
        'First Quartile',
        'Third Quartile',
        'Autocorrelation'
        ] 
    return ftList

import os
import numpy as np
import pandas as pd
from scipy.stats import skew, kurtosis

def extract_features(input_root, output_root, window_size = 60, step_size = 30):
    if not os.path.exists(output_root):
        os.makedirs(output_root)

    sensor_list = getSensors()
    feature_list = getFeatures()

    for filename in os.listdir(input_root):
        if filename.endswith('.csv'):
            input_file = os.path.join(input_root, filename)
            data = pd.read_csv(input_file)

            for i in range(0, len(data) - window_size + 1, step_size):
                window_data = data.iloc[i:i + window_size]
                if len(window_data) < window_size:
                    continue

                features = []
                for sensor in sensor_list:
                    sensor_data = window_data[sensor].values
                    sensor_features = tdom(sensor_data, feature_list)
                    features.extend(sensor_features)

                feature_row = pd.DataFrame([features], columns=[f"{sensor}_{feature}" for sensor in sensor_list for feature in feature_list])
                output_file = os.path.join(output_root, f"features_{filename}")
                
                if not os.path.exists(output_file):
                    feature_row.to_csv(output_file, index=False)
                else:
                    feature_row.to_csv(output_file, mode='a', header=False, index=False)
  

def cut_fall_data(input_file, output_file, window_size=60, step_size=30):
    data = pd.read_csv(input_file)
    index = -1
    s_max = 0
    for i in range(len(data)):
        row = data.iloc[i]
        print(row.iloc[0], row.iloc[1], row.iloc[2])
        s = row.iloc[0]**2 + row.iloc[1]**2 + row.iloc[2]**2
        if s > s_max:
            s_max = s
            index = i
    
    if index != -1:
        index_left = index - window_size // 2 + 1
        index_right = index + window_size // 2

        if index_left < 0:
            index_left = 0
            index_right = window_size - 1
        elif index_right >= len(data):
            index_right = len(data) - 1
            index_left = index_right - window_size + 1
        else:
            index_left = max(0, index_left)
            index_right = min(len(data) - 1, index_right)
        
        data = data.iloc[index_left:index_right + 1]
        data.reset_index(drop=True, inplace=True)
        data.to_csv(output_file, index=False)
        
    else:
        print(f"Warning: No valid data found in {input_file}.")
        return


# os.makedirs('te_nga_out', exist_ok=True)
# for i in range(1,401):
#     try:
#         cut_fall_data(f'merge_te_nga/fall_data_{i}.csv', f'te_nga_out/fall_data_{i}.csv', window_size=60, step_size=30)
#     except Exception as e:
#         print(f"Error processing file te_nga/fall_data_{i}.csv: {e}")


# def split_data():
#     ratio = 0.7
#     path = 'E:\PycharmProjects\server-smart-stick\new_fall_data\Traning'
#     for subdir in os.listdir(path):
#         subdir_path = os.path.join(path, subdir)
#         if os.path.isdir(subdir_path):
#             for file in os.listdir(subdir_path):
#                 if file.endswith('.csv'):
#                     input_file = os.path.join(subdir_path, file)
#                     output_file = os.path.join('data_fall', subdir, file)
#                     cut_fall_data(input_file, output_file, window_size=60, step_size=30)
    
#     path = 'E:\PycharmProjects\server-smart-stick\new_fall_data\Testing'
#     for subdir in os.listdir(path):
#         subdir_path = os.path.join(path, subdir)
#         if os.path.isdir(subdir_path):
#             for file in os.listdir(subdir_path):
#                 if file.endswith('.csv'):
#                     input_file = os.path.join(subdir_path, file)
#                     output_file = os.path.join('data_fall', subdir, file)
#                     cut_fall_data(input_file, output_file, window_size=60, step_size=30)

# extract_features('te_nga_out', 'data_fall/fall', window_size=60, step_size=30)

extract_features('n_te_nga', 'data_fall/n_fall', window_size=60, step_size=30)