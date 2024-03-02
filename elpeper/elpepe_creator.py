import random
import logging
import time
from moviepy.editor import VideoFileClip
import video_edit_helper

logging.basicConfig(level=logging.DEBUG, format='%(asctime)s - %(levelname)s - %(message)s')


# Функция для создания последовательности видео
def create_elpepe_sequence(source_video_paths, elpepe_clip_path, output_video_path, resolution=(1920, 1080),
                           clip_params=None):
    logging.info("Starting 'create_elpepe_sequence' function")

    # Инициализация генератора случайных чисел
    random.seed(time.time())

    elpepe_clip = VideoFileClip(elpepe_clip_path)
    elpepe_clip = elpepe_clip.resize(resolution)  # Изменение размера клипа "эльпепе"
    logging.info(f"Resized 'elpepe_clip' to {resolution}")

    if clip_params is None:
        # Если параметры для кусков не предоставлены, используем значения по умолчанию
        clip_params = [{'video_part_duration': 2.9, 'elpepe_part_duration': 0.8} for _ in range(5)]

    final_clips = []
    total_duration = 2.9  # Общая длительность для отслеживания текущего времени в видео "эльпепе"
    video_edit_helper.execute_video_editing_cycle(
        clip_params, elpepe_clip, final_clips, resolution, source_video_paths, total_duration)
    video_edit_helper.write_video_file(elpepe_clip, final_clips, output_video_path)
