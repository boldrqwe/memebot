package ru.boldr.memebot;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.boldr.memebot.controller.WikipediaController;
import ru.boldr.memebot.handlers.UpdateHandler;
import ru.boldr.memebot.helpers.JsonHelper;
import ru.boldr.memebot.model.Command;
import ru.boldr.memebot.service.HarkachParserService;
import ru.boldr.memebot.service.WikiParser;

import java.io.File;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private final static Logger logger = LoggerFactory.getLogger(TelegramBot.class);
    private final JsonHelper jsonHelper;
    private final UpdateHandler updateHandler;
    private final HarkachParserService harkachParserService;
    private final WikiParser wikiparser;


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

            Command command = updateHandler.executeCommand(update);
            if (Command.MAN.equals(command)) {
                execute(new SendAnimation(chatId, new InputFile(new File("files/man.mp4"))));
            }
            if (Command.MAN_REVERSE.equals(command)) {
                execute(new SendAnimation(chatId, new InputFile(new File("files/man_reverse.mp4"))));
            }
            if (Command.KAKASHKULES.equals(command)) {
                try {
                    wikiparser.toFile("какашкулес.html");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                File file = new File("./какашкулес.html");
                execute(new SendDocument(chatId, new InputFile(file, "какашкулес.html")));
            }
            if (Command.BURGERTRACH.equals(command)) {
                try {
                    wikiparser.toFile("бургертрах.html");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                File file = new File("./бургертрах.html");
                execute(new SendDocument(chatId, new InputFile(file, "бургертрах.html")));
            }

            if (Command.HARKACH.equals(command)) {
                String picture = harkachParserService.getPicture(chatId);
                execute(new SendMessage(chatId, picture));
            }

        } catch (TelegramApiException e) {
            logger.error(e.getMessage(), e);
            return;
        }
    }
}
