# dir_name = ['v1', 'v2', 'v3']
base_path = r'E:\PycharmProjects\server-smart-stick\static\images'
# import os
# import shutil
# for dir in dir_name:
#     path = f'{base_path}/{dir}'
#     for filename in os.listdir(path):
#         labelname = filename.split('_')[0]
#         os.makedirs(f'{base_path}/{labelname}', exist_ok=True)
#         file_path = f'{path}/{filename}'
#         new_file_path = f'{base_path}/{labelname}/{filename}'
#         shutil.copy2(file_path, new_file_path)
#         print(f'copy {file_path}')


import os
import shutil
for filename in os.listdir(f'{base_path}/chair'):
    if filename.endswith('.jpg') or filename.endswith('.png'):
        labelname = filename.split('_')[0]
        new_labelname = 'door'
        new_filename = filename.replace(labelname, new_labelname)
        file_path = f'{base_path}/{labelname}/{filename}'
        new_file_path = f'{base_path}/{new_labelname}/{new_filename}'
        shutil.copy2(file_path, new_file_path)
        print(f'copy {file_path} to {new_file_path}')