package ru.boldr.memebot.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.boldr.memebot.service.JokeService;

import static ru.boldr.memebot.model.Command.START;
import static ru.boldr.memebot.model.Command.STOP;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateHandler {

    private final JokeService jokeService;


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
