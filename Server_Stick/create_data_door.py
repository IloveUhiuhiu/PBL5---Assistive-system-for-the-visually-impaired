source_path = r'E:\Downloads\Data_ObjectDetect_Filtered'
destination_path = r'E:\Downloads\Data_ObjectDetect_Filtered_Tree'
import os
import shutil

for subdir in os.listdir(source_path):
    if subdir in ['train', 'test', 'valid']:
        source_subdir = os.path.join(source_path, subdir)
        image_subdir = os.path.join(source_subdir, 'images')
        label_subdir = os.path.join(source_subdir, 'labels')
        destination_subdir = os.path.join(destination_path, subdir)

        if not os.path.exists(destination_subdir):
            os.makedirs(destination_subdir)
        
        destination_image_subdir = os.path.join(destination_subdir, 'images')
        destination_label_subdir = os.path.join(destination_subdir, 'labels')
        if not os.path.exists(destination_image_subdir):
            os.makedirs(destination_image_subdir)
        if not os.path.exists(destination_label_subdir):
            os.makedirs(destination_label_subdir)
        
        for filename in os.listdir(image_subdir):
            if filename.startswith('tree_stems'):
                source_image_file = os.path.join(image_subdir, filename)
                destination_image_file = os.path.join(destination_image_subdir, filename)
                shutil.copy(source_image_file, destination_image_file)

        for filname in os.listdir(label_subdir):
            if filname.startswith('tree_stems'):
                source_label_file = os.path.join(label_subdir, filname)
                destination_label_file = os.path.join(destination_label_subdir, filname)
                shutil.copy(source_label_file, destination_label_file)
        print(f"Processed {subdir} directory.")
