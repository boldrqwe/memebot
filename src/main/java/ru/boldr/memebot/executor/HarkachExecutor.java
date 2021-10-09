package ru.boldr.memebot.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.boldr.memebot.TelegramBot;
import ru.boldr.memebot.model.entity.HarkachModHistory;
import ru.boldr.memebot.repository.HarkachModHistoryRepo;
import ru.boldr.memebot.service.HarkachParserService;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class HarkachExecutor {

    private final TelegramBot telegramBot;

    private final HarkachModHistoryRepo harkachModHistoryRepo;

    private final HarkachParserService harkachParserService;

    @Scheduled(cron = "0 * * ? * *")
    void sendPictures() {

        List<HarkachModHistory> chats = harkachModHistoryRepo.findAll();

        for (HarkachModHistory hMod : chats) {

            String picture = harkachParserService.getPicture(hMod.getChatId());

            if ("шутки кончились ):".equals(picture)) {
                continue;
            }

            try {
                telegramBot.executeAsync(new SendMessage(hMod.getChatId(), picture));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}
