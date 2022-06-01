package ru.boldr.memebot.executor;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.boldr.memebot.TelegramBot;
import ru.boldr.memebot.model.entity.HarkachModHistory;
import ru.boldr.memebot.repository.HarkachModHistoryRepo;
import ru.boldr.memebot.service.HarkachParserService;
import ru.boldr.memebot.service.ThreadComment;

@Slf4j
@Service
@RequiredArgsConstructor
public class HarkachExecutor {

    private final TelegramBot telegramBot;

    private final HarkachModHistoryRepo harkachModHistoryRepo;

    private final HarkachParserService harkachParserService;

    @SneakyThrows
    @Scheduled(cron = "0/15 * * ? * *")
    void sendPictures() {

        List<HarkachModHistory> chats = harkachModHistoryRepo.findAll();

        for (HarkachModHistory chat : chats) {

            ThreadComment threadComment = harkachParserService.getContent(chat.getChatId());

            if ("шутки кончились ):".equals(threadComment.picture())) {
                continue;
            }

                telegramBot.executeAsync(new SendMessage(chat.getChatId(), threadComment.picture()));

                String comment = threadComment.comment();
                if (comment != null && !comment.isEmpty()) {
                    telegramBot.executeAsync(new SendMessage(chat.getChatId(), comment));
                }

        }
    }
}
