import elpepe_creator
from pathlib import Path
import uuid

promt_folder = Path('promt')

# Получаем список всех MP4 файлов в папке
source_video_paths = [str(file) for file in promt_folder.glob('*.mp4')]
elpepe_clip_path = str(Path('elpepe') / 'elpepe.mp4')  # Путь к вашему видео "эльпепе"
unique_id = uuid.uuid4()
output_video_path = str(Path('output_video') / f'output_video_{unique_id}.mp4')  # Путь для сохранения итогового видео

# Задаем индивидуальные параметры для каждого куска
clip_params = [
    {'video_part_duration': 2.9, 'elpepe_part_duration': 0.8, 'delay': 0.0},
    {'video_part_duration': 2.8, 'elpepe_part_duration': 0.65, 'delay': 0.0},
    {'video_part_duration': 4.55, 'elpepe_part_duration': 0.55, 'delay': 1.4},
    {'video_part_duration': 2.7, 'elpepe_part_duration': 0.4, 'delay': 0.0},
    {'video_part_duration': 3.8, 'elpepe_part_duration': 0.5, 'delay': 0.5}
]

elpepe_creator.create_elpepe_sequence(source_video_paths, elpepe_clip_path, output_video_path, clip_params=clip_params)
