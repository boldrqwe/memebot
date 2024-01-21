package ru.boldr.memebot.executor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.boldr.memebot.repository.BotMassageHistoryRepo;
import ru.boldr.memebot.repository.CoolFileRepo;
import ru.boldr.memebot.repository.HarkachFileHistoryRepo;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataCleanExecutor {

    private final BotMassageHistoryRepo botMassageHistoryRepo;
    private final CoolFileRepo coolFileRepo;
    private final HarkachFileHistoryRepo harkachFileHistoryRepo;

    @Scheduled(cron = "0 0 0 ? * 4/1")
   @Transactional
    void cleanData() {

        botMassageHistoryRepo.deleteAll();
        coolFileRepo.deleteAll();
        harkachFileHistoryRepo.deleteAllByCreateTimeBefore(LocalDateTime.now().minus(7L, ChronoUnit.DAYS));
    }

}
