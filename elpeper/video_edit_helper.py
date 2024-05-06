import random
import logging
from moviepy.editor import VideoFileClip, concatenate_videoclips

# Настройка логирования
logging.basicConfig(level=logging.DEBUG, format='%(asctime)s - %(levelname)s - %(message)s')


def execute_video_editing_cycle(clip_params, elpepe_clip, final_clips, resolution, source_video_paths, total_duration):
    for index, params in enumerate(clip_params):
        source_video_clip = get_source_video_clip(resolution, source_video_paths)
        # Получаем параметры для текущего куска
        video_part_duration = params['video_part_duration']
        elpepe_part_duration = params['elpepe_part_duration']
        delay = params.get('delay', 0)  # Задержка с начала куска "эльпепе", по умолчанию 0

        video_part = get_video_part(source_video_clip, video_part_duration)
        elpepe_part = get_elpepe_part(delay, elpepe_clip, elpepe_part_duration, total_duration)
        final_clips.extend([video_part, elpepe_part])
        total_duration += video_part_duration + elpepe_part_duration


def write_video_file(elpepe_clip, final_clips, output_video_path):
    logging.info("Concatenating final video clips")
    final_video = concatenate_videoclips(final_clips)
    final_audio = elpepe_clip.audio.set_duration(final_video.duration)
    final_video = final_video.set_audio(final_audio)
    logging.info("Writing final video file")
    final_video.write_videofile(output_video_path, codec='libx264', audio_codec='aac')
    logging.info(f"Finished writing the video to {output_video_path}")
    elpepe_clip.close()
    for clip in final_clips:
        clip.close()


def get_source_video_clip(resolution, source_video_paths):
    source_video_path = random.choice(source_video_paths)
    logging.info(f"Selected source video: {source_video_path}")
    source_video_clip = VideoFileClip(source_video_path)
    source_video_clip = source_video_clip.resize(resolution)
    logging.info(f"Resized 'source_video_clip' to {resolution}")
    return source_video_clip


def get_elpepe_part(delay, elpepe_clip, elpepe_part_duration, total_duration):
    # Проверка и корректировка задержки для предотвращения выхода за пределы видео
    elpepe_start_time = max(0, min(total_duration + delay, elpepe_clip.duration - elpepe_part_duration))
    elpepe_end_time = min(elpepe_start_time + elpepe_part_duration, elpepe_clip.duration)
    # Создаем фрагмент видео "эльпепе" с учетом задержки
    elpepe_part = elpepe_clip.subclip(elpepe_start_time, elpepe_end_time)
    logging.info(f"Created elpepe part with delay {delay} from {elpepe_start_time} to {elpepe_end_time}")
    return elpepe_part


def get_video_part(source_video_clip, video_part_duration):
    max_start = max(0, source_video_clip.duration - video_part_duration)
    start_time = random.uniform(0, max_start)
    logging.info(f"Random start time for video part: {start_time}")
    video_part = source_video_clip.subclip(start_time, start_time + video_part_duration)
    logging.info(f"Created video part from {start_time} to {start_time + video_part_duration}")
    return video_part
