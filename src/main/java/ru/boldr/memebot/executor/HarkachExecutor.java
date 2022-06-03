package ru.boldr.memebot.executor;

import java.awt.Image;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Stack;

import javax.imageio.ImageIO;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
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

            InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(
                    List.of(List.of(
                            InlineKeyboardButton.builder()
                                    .text("перейти в тред")
                                    .url(getUrl(threadComment.picture()))
                                    .build(),
                            InlineKeyboardButton.builder()
                                    .text("скачать все фото из треда")
                                    .callbackData("зов обратно")
                                    .build()

                    ))
            );

            URL url = new URL(threadComment.picture());
            InputStream inputStream = url.openStream();

            InputFile inputFile = new InputFile(inputStream, "photo");

            SendPhoto sendPhoto = SendPhoto.builder()
                    .chatId(chat.getChatId())
                    .photo(inputFile)
                    .caption(threadComment.comment())
                    .replyMarkup(inlineKeyboard)
                    .build();

            telegramBot.executeAsync(sendPhoto);

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
