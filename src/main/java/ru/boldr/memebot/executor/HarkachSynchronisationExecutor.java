package ru.boldr.memebot.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.boldr.memebot.service.HarkachParserService;
@Slf4j
@Service
@RequiredArgsConstructor
public class HarkachSynchronisationExecutor {

    private final HarkachParserService harkachParserService;

    @Scheduled(cron = "* 0/20 * ? * *")
    public void doSynchronisation() {
        log.info("синхронизирую двач");
        harkachParserService.loadContent();
        log.info("сохранил файлы");
    }
}