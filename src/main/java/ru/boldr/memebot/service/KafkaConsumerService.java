package ru.boldr.memebot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.boldr.memebot.model.entity.CoolFile;
import ru.boldr.memebot.model.entity.MediaFile;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {
    private final HarkachParserService harkachParserService;
    private final PictureService pictureService;
    private final FileReadWriteService readWriteService;
    private final MediaFileService mediaFileService;

    @KafkaListener(topics = "thread-requests-topic", groupId = "myGroup")
    @Transactional
    public void listen(String message) {
        List<MediaFile> parentMedia = mediaFileService.findByFileUrl(message);
        if (parentMedia.isEmpty()) {
            log.info("файл не найден, возможно удален");
            return;
        }
        MediaFile mediaFile = parentMedia.get(0);
        if (mediaFile.getIsTreadDownloaded()) {
            log.info("файлы уже скачены");
            return;
        }
        log.info("execute message");

        String[] split = message.split("/");
//        https://2ch.hk/b/res/299480602.html
        String threadUrl = HarkachParserService.DVACH + "/b/res/" + split[split.length - 2] + ".html";
        List<CoolFile> coolFiles = harkachParserService.getCoolFiles(threadUrl);
        if (coolFiles.isEmpty()) {
            mediaFileService.deleteMediaFile(List.of(mediaFile));
            return;
        }
        Instant now = Instant.now();
        Set<MediaFile> mediaFiles = coolFiles.stream().parallel()
                .map(f -> pictureService.downloadIfAvailable(f, now, mediaFile.getId()))
                .collect(Collectors.toSet());
        mediaFileService.saveMediaFiles(mediaFiles);
        mediaFile.setIsTreadDownloaded(true);
        mediaFileService.save(mediaFile);
    }


}