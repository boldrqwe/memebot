package ru.boldr.memebot.executor;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Stack;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.boldr.memebot.TelegramBot;
import ru.boldr.memebot.model.entity.HarkachModHistory;
import ru.boldr.memebot.repository.HarkachModHistoryRepo;
import ru.boldr.memebot.service.HarkachMarkupConverter;
import ru.boldr.memebot.service.HarkachParserService;
import ru.boldr.memebot.service.TelegramSemaphore;
import ru.boldr.memebot.service.ThreadComment;

@Slf4j
@Service
@RequiredArgsConstructor
public class HarkachExecutor {

    private final TelegramBot telegramBot;
    private final HarkachModHistoryRepo harkachModHistoryRepo;
    private final HarkachParserService harkachParserService;
    private final HarkachMarkupConverter harkachMarkupConverter;
    private final TelegramSemaphore telegramSemaphore;

    //* * * ? * * *
    @SneakyThrows
    @Scheduled(cron = "0/15 * * ? * *")
    void sendPictures() {

        List<HarkachModHistory> chats = harkachModHistoryRepo.findAll();

        for (HarkachModHistory chat : chats) {

            ThreadComment threadComment = harkachParserService.getContent(chat.getChatId());

            if ("шутки кончились ):".equals(threadComment.picture())) {
                continue;
            }
            String chatId = chat.getChatId();
            String picture = threadComment.picture();
            String threadUrl = getThreadUrlFromPictureUrl(picture);

            InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(
                    List.of(
                            List.of(
                                    InlineKeyboardButton.builder()
                                            .text("перейти в тред")
                                            .url(threadUrl)
                                            .build()),
                            List.of(
                                    InlineKeyboardButton.builder()
                                            .text("скачать все")
                                            .callbackData(threadUrl + "," + chatId + ",callback")
                                            .build())
                    )
            );
            URL url = new URL(picture);

            var extension = harkachParserService.getExtension(picture);
            int available = 0;
            try {
                available = url.openStream().available();
            } catch (Exception e) {
                log.info(url + " не скачать");
            }

            if (available < 1) {
                return;
            }

            InputStream inputStream = url.openStream();

            InputFile inputFile = new InputFile(inputStream, "file");

            String comment = harkachMarkupConverter.convertToTgHtml(threadComment.comment());

            log.info("отправляю " + picture);

            switch (extension) {
                case ("jpg"), ("png") -> telegramSemaphore
                        .executeInLock(() -> telegramBot.executeAsync(SendPhoto.builder()
                                .chatId(chatId)
                                .photo(inputFile)
                                .caption(comment)
                                .parseMode(ParseMode.HTML)
                                .replyMarkup(inlineKeyboard)
                                .build()), 1L);

                case ("mp4"), ("gif") -> telegramSemaphore
                        .executeInLock(() -> telegramBot.executeAsync(SendVideo.builder()
                                .chatId(chatId)
                                .caption(comment)
                                .video(inputFile)
                                .parseMode(ParseMode.HTML)
                                .replyMarkup(inlineKeyboard)
                                .build()), 1L);
                case ("webm") -> telegramSemaphore
                        .executeInLock(() -> telegramBot.executeAsync(SendVideo.builder()
                                .chatId(chatId)
                                .caption(comment)
                                .video(new InputFile(new File(harkachParserService.convertWebmToMp4(url))))
                                .parseMode(ParseMode.HTML)
                                .replyMarkup(inlineKeyboard)
                                .build()), 1L);

                default -> throw new IllegalStateException("Unexpected value: " + extension);
            }
        }
    }

    private String getThreadUrlFromPictureUrl(String picture) {
        int length = picture.length();
        Stack<Character> stack = new Stack<>();
        StringBuilder result = new StringBuilder();
        for (int i = length - 1; i > 0; i--) {
            char c = picture.charAt(i);
            if (c == '/') {
                break;
            }
            stack.add(c);
        }
        while (!stack.isEmpty()) {
            result.append(stack.pop());
        }

        return picture.replace("/" + result, "")
                .replace("src", "res") + ".html";
    }
}