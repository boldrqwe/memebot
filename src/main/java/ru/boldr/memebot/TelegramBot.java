package ru.boldr.memebot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.boldr.memebot.handlers.UpdateHandler;
import ru.boldr.memebot.helpers.JsonHelper;

import java.util.Objects;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final static Logger logger = LoggerFactory.getLogger(TelegramBot.class);



    private final JsonHelper jsonHelper;


    public TelegramBot(JsonHelper jsonHelper) {
        this.jsonHelper = jsonHelper;
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
            UpdateHandler updateHandler = new UpdateHandler(update);
            String answer = updateHandler.readAndReturnStr();
            execute(new SendMessage(update.getMessage().getChatId().toString(),answer));

        } catch (TelegramApiException e) {
            logger.error(e.getMessage(), e);
            return;
        }
    }
}
