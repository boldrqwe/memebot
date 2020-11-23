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

import java.util.Objects;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final static Logger logger = LoggerFactory.getLogger(TelegramBot.class);



    private final ObjectMapper objectMapper;


    public TelegramBot(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
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
        String formattedUpdate;
        try {
            formattedUpdate = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(update);
        } catch (JsonProcessingException e) {
           throw new RuntimeException(e);
        }
        logger.info("new update: {}", formattedUpdate);
        if (!update.hasMessage()){
            logger.warn("massage is empty");
            return;
        }
        try {
            execute(new SendMessage(update.getMessage().getChatId().toString(),update.getMessage().getText()));
        } catch (TelegramApiException e) {
            logger.error(e.getMessage(), e);
            return;
        }
    }
}
