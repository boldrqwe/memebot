package ru.boldr.memebot.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.boldr.memebot.model.entity.CoolFile;
import ru.boldr.memebot.model.entity.MediaFile;
import ru.boldr.memebot.repository.CoolFileRepo;
import ru.boldr.memebot.repository.FileUrl;
import ru.boldr.memebot.service.FileReadWriteService;
import ru.boldr.memebot.service.MediaFileService;
import ru.boldr.memebot.service.PictureService;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaExecutor {
    private final CoolFileRepo coolFileRepo;
    private final PictureService pictureService;
    private final MediaFileService mediaFileService;
    private final FileReadWriteService fileReadWriteService;


    @Scheduled(cron = "0 * * ? * *")
    @Transactional
    void saveMedia() {
        log.info("startSaveMediaFiles");
        List<FileUrl> allMediaFiles = mediaFileService.findAllWithOutData();
        Set<String> urls = StreamEx.of(allMediaFiles).map(FileUrl::getFileUrl).toSet();
        Instant now = Instant.now();
        List<CoolFile> all = coolFileRepo.findAll();
        Set<CoolFile> coolFiles = StreamEx.of(all)
                .filter(i -> !urls.contains(i.getFileName()))
                .toSet();
        log.info("find %d files".formatted(coolFiles.size()));
        Set<MediaFile> mediaFiles = StreamEx.of(coolFiles).parallel()
                .map(f -> pictureService.downloadIfAvailable(f, now, null))
                .nonNull()
                .toSet();
        mediaFileService.saveMediaFiles(mediaFiles);
    }


}
