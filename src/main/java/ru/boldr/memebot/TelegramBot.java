package ru.boldr.memebot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.boldr.memebot.handlers.UpdateHandler;
import ru.boldr.memebot.helpers.JsonHelper;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final static Logger logger = LoggerFactory.getLogger(TelegramBot.class);
    private final JsonHelper jsonHelper;
    private final UpdateHandler updateHandler;


    public TelegramBot(JsonHelper jsonHelper, UpdateHandler updateHandler) {
        this.jsonHelper = jsonHelper;
        this.updateHandler = updateHandler;
    }


    @Override
    public String getBotUsername() {
        return "MementosFunniestForMeBot";
    }

    @Override
    public String getBotToken() {
        return "1472867697:AAETU66PhtFNh7Sglei4wVWK314Y45kheRM";
    }


    @Override
    public void onUpdateReceived(Update update) {

        logger.info("new update: {}", jsonHelper.lineToMap(update));
        if (!update.hasMessage()){
            logger.warn("massage is empty");
            return;
        }
        try {
            execute(new SendMessage(update.getMessage().getChatId().toString(),updateHandler.answer(update)));
        } catch (TelegramApiException e) {
            logger.error(e.getMessage(), e);
            return;
        }
    }
}
