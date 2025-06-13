# # Lọc thư mục Stair, du
# import os
# import shutil

# path = r'E:\Downloads\Stair'

# for subdir in os.listdir(path):
#     if subdir in ['train', 'test', 'valid']:
#         source_subdir = os.path.join(path, subdir)
#         image_subdir = os.path.join(source_subdir, 'images')
#         label_subdir = os.path.join(source_subdir, 'labels')
        
#         for filename in os.listdir(label_subdir):
#             # đọc file label
#             source_label_file = os.path.join(label_subdir, filename)
#             with open(source_label_file, 'r') as file:
#                 lines = file.readlines()
            
#             # kiểm tra xem có dòng nào bắt đầu là 0 không
#             has_zero = any(line.startswith('0') for line in lines)
#             if has_zero:
#                 # nếu có, xóa file label và ảnh tương ứng
#                 source_image_file = os.path.join(image_subdir, filename.replace('.txt', '.jpg'))
#                 if os.path.exists(source_image_file):
#                     os.remove(source_image_file)
#                 os.remove(source_label_file)
#                 print(f"Removed {source_label_file} and {source_image_file}")
#             else:
#                 print(f"Kept {source_label_file} as it does not contain class 0.")

#         print(f"Processed {subdir} directory.")
                



# class_names = ['tree_stems', 'dog', 'cat', 'car', 'sofa', 'chair', 'bicycle', 'motorbike', 'bench', 'up_down_stairs', 'door']

# import os
# import shutil

# path = r'E:\Downloads\Data_ObjectDetect_Filtered'

# for subdir in os.listdir(path):
#     if subdir in ['train', 'test', 'valid']:
#         source_subdir = os.path.join(path, subdir)
#         image_subdir = os.path.join(source_subdir, 'images')
#         label_subdir = os.path.join(source_subdir, 'labels')
        
#         for filename in os.listdir(label_subdir):
#             Ok = False
#             for class_name in class_names:
#                 if filename.startswith(class_name):
#                     Ok = True
#                     break
#             if Ok:
#                 os.remove(os.path.join(label_subdir, filename))

#         for filename in os.listdir(image_subdir):
#             Ok = False
#             for class_name in class_names:
#                 if filename.startswith(class_name):
#                     Ok = True
#                     break
#             if Ok:
#                 os.remove(os.path.join(image_subdir, filename))   

#         print(f"Processed {subdir} directory.")

import os
import shutil
import numpy as np
source_path = r'E:\\Downloads\\Tree'
destination_path = r'E:\\Downloads\\Data_ObjectDetect_Filtered'

for subdir in os.listdir(source_path):
    class_source = 0
    class_destination = 13
    if subdir in ['train', 'test', 'valid']:
        source_subdir = os.path.join(source_path, subdir)
        label_subdir = os.path.join(source_subdir, 'labels')
        for filename in os.listdir(label_subdir):
            source_label_file = os.path.join(label_subdir, filename)
            with open(source_label_file, 'r') as file:
                lines = file.readlines()
            with open(source_label_file, 'w') as file:
                for line in lines:
                    parts = line.strip().split()
                    if parts and parts[0] == f'{class_source}':
                        parts[0] = f'{class_destination}'
                        file.write(' '.join(parts) + '\n') 


    image_files = []
    if subdir in ['train', 'test', 'valid']:
        source_subdir = os.path.join(source_path, subdir)
        image_subdir = os.path.join(source_subdir, 'images')
        for filename in os.listdir(image_subdir):
            image_files.append(os.path.join(image_subdir, filename))
    

    # Chia 7:2:1
    train_count = int(len(image_files) * 0.7)
    valid_count = int(len(image_files) * 0.2) 
    test_count = len(image_files) - train_count - valid_count
    indices = np.random.permutation(len(image_files))

    train_indices = indices[:train_count]
    valid_indices = indices[train_count:train_count + valid_count]
    test_indices = indices[train_count + valid_count:]

    train_files = [image_files[i] for i in train_indices]
    valid_files = [image_files[i] for i in valid_indices]
    test_files = [image_files[i] for i in test_indices]

    def copy_file(image_file, split_type):
        # Copy image
        destination_img = os.path.join(destination_path, split_type, 'images')
        os.makedirs(destination_img, exist_ok=True)
        shutil.copy(image_file, destination_img)

        # Build label path from image path
        image_dir, image_name = os.path.split(image_file)
        label_dir = image_dir.replace(os.path.join('images'), 'labels')
        label_file = os.path.join(label_dir, os.path.splitext(image_name)[0] + '.txt')

        if os.path.exists(label_file):
            destination_lbl = os.path.join(destination_path, split_type, 'labels')
            os.makedirs(destination_lbl, exist_ok=True)
            shutil.copy(label_file, destination_lbl)
        else:
            print(f"[⚠️] Không tìm thấy label cho: {image_file}")

    # Copy từng tập
    for f in train_files:
        copy_file(f, 'train')

    for f in valid_files:
        copy_file(f, 'valid')

    for f in test_files:
        copy_file(f, 'test')


