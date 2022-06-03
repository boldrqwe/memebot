package ru.boldr.memebot.handlers;

import java.io.File;
import java.util.List;
import java.util.Locale;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.boldr.memebot.TelegramBot;
import ru.boldr.memebot.model.Command;
import ru.boldr.memebot.model.entity.BotMessageHistory;
import ru.boldr.memebot.model.entity.FunnyJoke;
import ru.boldr.memebot.model.entity.HarkachModHistory;
import ru.boldr.memebot.repository.HarkachModHistoryRepo;
import ru.boldr.memebot.service.HarkachParserService;
import ru.boldr.memebot.service.JokeService;
import ru.boldr.memebot.service.MessageHistoryService;
import ru.boldr.memebot.service.ThreadComment;

import static ru.boldr.memebot.model.Command.START;
import static ru.boldr.memebot.model.Command.STOP;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateHandler {

    private final JokeService jokeService;

    public String saveFunnyJoke(Update update) {
        FunnyJoke funnyJoke = jokeService.saveFunnyJoke(update);
        if (funnyJoke == null) {
            return null;
        }
        return jokeService.getStats(funnyJoke.getChatId());
    }

    public boolean checkWriteMessagePermission(Message message) {
        String text = message.getText();

        if (START.getCommand().equals(text) || STOP.getCommand().equals(text)) {
            boolean permission = text.equals(START.getCommand());
            jokeService.savePermission(message, permission);
            return permission;
        }

        return jokeService.checkWriteMessagePermission(message.getChatId());
    }

}
