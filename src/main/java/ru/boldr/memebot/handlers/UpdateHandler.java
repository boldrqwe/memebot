package ru.boldr.memebot.handlers;

import org.telegram.telegrambots.meta.api.objects.Update;

public class UpdateHandler {
    private Update update;

    public UpdateHandler(Update update) {
        this.update = update;
    }


    public String readAndReturnStr() {
        String command = update.getMessage().getText().toLowerCase().replaceAll(",", "");
        String userName = update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName();
        String startOfCommand;
        String[] tokens = command.split(" ");
        String result = "";
        String[] cases = {
                "привет",
                "видео",
                "музыку",
                "картинку"
        };
        String[] answer = {
                String.format("привет %s", userName + ". "),
                "https://www.youtube.com/watch?v=anE4HaO3Bzc&ab_channel=vanzai ",
                "музыка ",
                "https://images.app.goo.gl/hQEBTqUZnFrMqw8XA "
        };
        for (int j = 0; j < tokens.length; j++) {
            for (int i = 0; i < cases.length; i++) {
                if (tokens[j].contains(cases[i])) {
                    result += answer[i];
                }
            }
        }
        return result;
    }
}
