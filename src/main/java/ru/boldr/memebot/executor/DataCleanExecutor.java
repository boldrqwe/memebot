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
import ru.boldr.memebot.repository.HarkachFileHistoryRepo;
import ru.boldr.memebot.service.FileReadWriteService;
import ru.boldr.memebot.service.HarkachParserService;
import ru.boldr.memebot.service.MediaFileService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataCleanExecutor {

    private final CoolFileRepo coolFileRepo;
    private final HarkachFileHistoryRepo harkachFileHistoryRepo;
    private final HarkachParserService harkachParserService;
    private final MediaFileService mediaFileService;
    private final FileReadWriteService fileReadWriteService;

    @Scheduled(cron = "0 0 0 ? * 4/1")
    @Transactional
    void cleanData() {
        coolFileRepo.deleteAll();
        harkachFileHistoryRepo.deleteAllByCreateTimeBefore(LocalDateTime.now().minus(7L, ChronoUnit.DAYS));
    }

    @Scheduled(cron = "0 0/7 * ? * *")
    @Transactional
    void checkAvailable() {
        log.info("startCheckAvailable");
        List<CoolFile> all = coolFileRepo.findAll();
        List<CoolFile> coolFiles = StreamEx.of(all).parallel()
                .filter(f -> !harkachParserService.isAvailableToDownloadFile(f))
                .toList();
        Set<String> fileUrls = StreamEx.of(coolFiles).map(CoolFile::getFileName).toSet();

        log.info("not available %d files".formatted(coolFiles.size()));
        List<MediaFile> mediaFiles = mediaFileService.findAllByFileUrlIn(fileUrls);
        List<Long> ids = StreamEx.of(mediaFiles).map(MediaFile::getId).toList();

        Collection<MediaFile> byParentIdIn = mediaFileService.findByParentIdIn(ids);
        if (!byParentIdIn.isEmpty()) {
            fileReadWriteService.deleteFiles(byParentIdIn);
        }
        coolFileRepo.deleteAll(coolFiles);
        mediaFileService.deleteMediaFile(mediaFiles);
        mediaFileService.deleteMediaFile(byParentIdIn);
        fileReadWriteService.deleteFiles(mediaFiles);
        ReentrantLock reentrantLock = new ReentrantLock();
        reentrantLock.getHoldCount();

        ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
        Semaphore semaphore = new Semaphore(3);
        log.info("delete %d files".formatted(coolFiles.size()));
    }
}
