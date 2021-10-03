package ru.boldr.memebot.handlers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.boldr.memebot.model.Command;
import ru.boldr.memebot.model.entity.FunnyJoke;
import ru.boldr.memebot.service.JokeService;

import static ru.boldr.memebot.model.Command.*;

@Component
@AllArgsConstructor
@Slf4j
public class UpdateHandler {


    private final JokeService jokeService;


    public String answer(Update update) {
        return null;
    }

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


    public Command executeCommand(Update update) {
        if (update.getMessage().getText().equals(MAN.getCommand())) {
            return MAN;
        }

        if (update.getMessage().getText().equals(MAN_REVERSE.getCommand())) {
            return MAN_REVERSE;
        }

        if (update.getMessage().getText().equals(KAKASHKULES.getCommand())) {
            return KAKASHKULES;
        }

        if (update.getMessage().getText().equals(BURGERTRACH.getCommand())) {
            return BURGERTRACH;
        }

        if (update.getMessage().getText().equals(HARKACH.getCommand())) {
            return HARKACH;
        }
        return null;
    }
}
