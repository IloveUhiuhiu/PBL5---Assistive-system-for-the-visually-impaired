from gtts import gTTS
import os
DIR_ROOT = os.path.dirname(os.path.abspath(__file__))
DIR_AUDIO = os.path.join(DIR_ROOT, r'static\\audio')
# Tạo DIR_AUDIO nếu chưa tồn tại
os.makedirs(DIR_AUDIO, exist_ok=True)

labels = ['người', 'chiếc xe đạp', 'chiếc xe máy',
          'chiếc ô tô', 'chiếc xe buýt',
          'cái ghế', 'cái ghế sofa', 'cái bàn',
          'cái cửa', 'cái ghế dài', 'con chó', 'con mèo',
          'cầu thang', 'cái cây']

numbers = ['một', 'hai', 'ba', 'nhiều']
# Language in which you want to convert
language = 'vi'
count = 1


for label in labels:
    for number in numbers:
        mytext = f"Phía trước có {number} {label}"
        myobj = gTTS(text=mytext, lang=language)
        file_name = str(count).zfill(3)
        count += 1
        file_path = os.path.join(DIR_AUDIO, f'{file_name}.mp3')
        myobj.save(file_path)

# mytext = f"Dừng lại, phía trước có vật cản"
# myobj = gTTS(text=mytext, lang=language)
# count = 61
# file_name = str(count).zfill(3)
# file_path = os.path.join(DIR_AUDIO, f'{file_name}.mp3')
# myobj.save(file_path)
# times = []
# dict = {}
# from mutagen.mp3 import MP3
# import os
# for file_name in os.listdir(r'static\\audio'):
#     file_path = os.path.join(r'static\\audio', file_name)
#     audio = MP3(file_path)
#     times.append(audio.info.length)
#     print(f"Tên file: {file_name}, Thời lượng: {audio.info.length:.6f} giây")

# print(times)

# for id, time in enumerate(times):
#     dict[id + 1] = int(time * 1000)

# print(dict)
