source_path = r'E:\Downloads\Data_ObjectDetect_Filtered'

import os
import shutil

for subdir in os.listdir(source_path):
    if subdir in ['train', 'test', 'valid']:
        source_subdir = os.path.join(source_path, subdir)
        image_subdir = os.path.join(source_subdir, 'images')
        label_subdir = os.path.join(source_subdir, 'labels')
        
       
        for filename in os.listdir(image_subdir):
            if filename.startswith('sofa') and 'preprocessed' in filename:
                os.remove(os.path.join(image_subdir, filename))

        for filname in os.listdir(label_subdir):
            if filname.startswith('sofa') and 'preprocessed' in filname:
                os.remove(os.path.join(label_subdir, filname))
                
        print(f"Processed {subdir} directory.")
