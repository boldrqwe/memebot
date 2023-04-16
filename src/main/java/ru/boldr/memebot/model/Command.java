package ru.boldr.memebot.model;

import one.util.streamex.StreamEx;

import java.util.Arrays;

public enum Command {

    HAT("/шляпа"),
    START("/fun"),
    STOP("/stopfun"),
    MAN("мужик"),
    REAL_MAN("/настоящий"),
    ROMPOMPOM("/ромпомпом"),
    MAN_REVERSE("обратно"),
    KAKASHKULES("/какашкулес"),
    BURGERTRACH("/бургертрах"),
    HARKACH("/2ch"),
    HARKACHMOD_OFF("/hmodoff"),
    HARKACHMOD_ON("/hmodon"),
    HARKACHBASE_UPDATE("/hbu"),
    HELP("/help"),
    HUITA("");

    private final String command;

    public static String getCommands() {
        Command[] values = Command.values();
        StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        for (Command command : values) {
            if (HUITA == command) {
                continue;
            }
            count++;
            stringBuilder.append(count).append(") ").append(command.getCommand()).append(" ").append("\n");
        }
        return stringBuilder.toString();
    }

    public String getCommand() {
        return command;
    }

    Command(String command) {
        this.command = command;
    }


    public static Command checkThisSheet(String name) {
        return Arrays.stream(Command.values())
                .filter(c -> name.toLowerCase().contains(c.getCommand().toLowerCase()))
                .findFirst()
                .orElse(HUITA);
    }

}
