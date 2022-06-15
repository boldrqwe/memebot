package ru.boldr.memebot.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Service
@RequiredArgsConstructor
public class HatGameService {

    public BotApiMethod createGame(Update update) {

        return SendMessage.builder().build();
    }

    public SendMessage sendAccept(Update update) {

        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(
                List.of(List.of(
                        InlineKeyboardButton.builder()
                                .text("Играть в шляпу")
                                .callbackData("accept")
                                .build()
                ))
        );
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId().toString())
                .text("примите участие в игре Шляпа")
                .replyMarkup(inlineKeyboard)
                .build();
    }

    public SendMessage process(Update update) {
        Message message = update.getMessage();
        String text = message.getText();
        var chatId = message.getChat().getId().toString();

        if (text.equalsIgnoreCase("/шляпа")) {
            return sendAccept(update);
        }

        if(update.hasCallbackQuery()){
            update.getCallbackQuery().getFrom().getId();
        }

        return SendMessage.builder().build();
    }
}
