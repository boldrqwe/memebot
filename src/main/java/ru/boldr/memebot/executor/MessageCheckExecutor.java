package ru.boldr.memebot.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.boldr.memebot.TelegramBot;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageCheckExecutor {

//    private final TelegramBot telegramBot;
//
//    @Scheduled(cron = "0/10 * * ? * *")
//    public void checkMessages() throws TelegramApiException {
//        log.info("синхронизирую сообщения");
//        telegramBot.checkMessages();
//    }

}
