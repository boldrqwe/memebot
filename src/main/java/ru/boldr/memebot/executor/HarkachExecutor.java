package ru.boldr.memebot.executor;

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
import ru.boldr.memebot.service.ThreadComment;

@Slf4j
@Service
@RequiredArgsConstructor
public class HarkachExecutor {

    private final TelegramBot telegramBot;
    private final HarkachModHistoryRepo harkachModHistoryRepo;
    private final HarkachParserService harkachParserService;
    private final HarkachMarkupConverter harkachMarkupConverter;

    @SneakyThrows
    @Scheduled(cron = "0/15 * * ? * *")
    void sendPictures() {

        List<HarkachModHistory> chats = harkachModHistoryRepo.findAll();

        for (HarkachModHistory chat : chats) {

            ThreadComment threadComment = harkachParserService.getContent(chat.getChatId());

            if ("шутки кончились ):".equals(threadComment.picture())) {
                continue;
            }

            InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(
                    List.of(List.of(
                            InlineKeyboardButton.builder()
                                    .text("перейти в тред")
                                    .url(getUrl(threadComment.picture()))
                                    .build()
                    ))
            );

            String picture = threadComment.picture();
            URL url = new URL(picture);

            String[] split = picture.split("\\.");

            var extension = split[split.length - 1];

            InputStream inputStream = url.openStream();



            InputFile inputFile = new InputFile(inputStream, "file");

            String chatId = chat.getChatId();
            String comment = harkachMarkupConverter.convertToTgHtml(threadComment.comment());

            switch (extension) {
                case ("jpg"), ("png") ->
                        telegramBot.executeAsync(SendPhoto.builder()
                        .chatId(chatId)
                        .photo(inputFile)
                        .caption(comment)
                        .parseMode(ParseMode.HTML)
                        .replyMarkup(inlineKeyboard)
                        .build());

                case ("mp4"), ("webm") ->  telegramBot.executeAsync(SendVideo.builder()
                        .chatId(chatId)
                        .caption(comment)
                        .video(inputFile)
                        .parseMode(ParseMode.HTML)
                        .replyMarkup(inlineKeyboard)
                        .build());

                default -> throw new IllegalStateException("Unexpected value: " + extension);
            }
        }
    }

    private String getUrl(String picture) {
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
