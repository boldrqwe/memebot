package ru.boldr.memebot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.boldr.memebot.handlers.UpdateHandler;
import ru.boldr.memebot.helpers.JsonHelper;
import ru.boldr.memebot.model.Command;
import ru.boldr.memebot.model.entity.BotMassageHistory;
import ru.boldr.memebot.service.HarkachParserService;
import ru.boldr.memebot.service.MassageHistoryService;
import ru.boldr.memebot.service.WikiParser;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private final static Logger logger = LoggerFactory.getLogger(TelegramBot.class);
    private final JsonHelper jsonHelper;
    private final UpdateHandler updateHandler;
    private final HarkachParserService harkachParserService;
    private final WikiParser wikiparser;
    private final MassageHistoryService massageHistoryService;


    @Override
    public String getBotUsername() {
        return "MementosFunniestForMeBot";
    }

    @Override
    public String getBotToken() {
        return "1472867697:AAESCvVv5UcIMNwjXFWpzq9cy4CdQoEY7QM";
    }


    @Override
    public void onUpdateReceived(Update update) {

        logger.info("new update: {}", jsonHelper.lineToMap(update));
        if (!update.hasMessage()) {
            logger.warn("massage is empty");
            return;
        }

        if (!updateHandler.checkWriteMessagePermission(update.getMessage())) {
            return;
        }

        String chatId = update.getMessage().getChatId().toString();

        try {
            String answer = updateHandler.saveFunnyJoke(update);
            if (answer != null) {
                execute(new SendMessage(chatId, answer));
            }

            execute(
                    update,
                    chatId,
                    update.getMessage().getText().toLowerCase(Locale.ROOT)
            );

        } catch (TelegramApiException e) {
            logger.error(e.getMessage(), e);
            return;
        }
    }

    private void execute(Update update, String chatId, String command) throws TelegramApiException {
        if (Command.MAN.getCommand().equals(command)) {
            execute(new SendAnimation(chatId, new InputFile(new File("files/man.mp4"))));
            massageHistoryService.save(BotMassageHistory.builder()
                    .chatId(chatId)
                    .messageId(update.getMessage().getMessageId())
                    .build());
        }
        if (Command.MAN_REVERSE.getCommand().equals(command)) {
            execute(new SendAnimation(chatId, new InputFile(new File("files/man_reverse.mp4"))));
        }
        if (Command.KAKASHKULES.getCommand().equals(command)) {
            try {
                wikiparser.toFile("какашкулес.html");
            } catch (IOException e) {
                e.printStackTrace();
            }
            File file = new File("./какашкулес.html");
            execute(new SendDocument(chatId, new InputFile(file, "какашкулес.html")));
        }
        if (Command.BURGERTRACH.getCommand().equals(command)) {
            try {
                wikiparser.toFile("бургертрах.html");
            } catch (IOException e) {
                e.printStackTrace();
            }
            File file = new File("./бургертрах.html");
            execute(new SendDocument(chatId, new InputFile(file, "бургертрах.html")));
        }

        if (Command.HARKACH.getCommand().equals(command)) {
            String picture = harkachParserService.getPicture(chatId);
            execute(new SendMessage(chatId, picture));
        }

        if (Command.HARKACHBASE_UPDATE.getCommand().equals(command)) {
            harkachParserService.getPictures();
        }

        if (Command.HARKACHMOD_ON.getCommand().equals(command)) {

        }

        if (Command.HARKACHMOD_OFF.getCommand().equals(command)) {

        }
    }

    public void checkMessages() throws TelegramApiException {
        List<BotMassageHistory> messages = massageHistoryService.findAll();
        if (messages.size() < 1) {
            log.info("история пуста");
            return;
        }
        for (BotMassageHistory message : messages) {

            try {
                CompletableFuture<Serializable> serializableCompletableFuture = executeAsync(
                        new EditMessageCaption(
                                message.getChatId(),
                                message.getMessageId(),
                                message.getMessageId().toString(),
                                "",
                                InlineKeyboardMarkup.builder()
                                        .build(),
                                "",
                                List.of()
                        ));
                if (!serializableCompletableFuture.isDone()) {
                    execute(SendVideo.builder()
                            .chatId(message.getChatId())
                            .allowSendingWithoutReply(true)
                            .video(new InputFile(new File("./files/video_2021-10-03_03-47-06.mp4")))
                            .build()
                    );
                    massageHistoryService.delete(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
