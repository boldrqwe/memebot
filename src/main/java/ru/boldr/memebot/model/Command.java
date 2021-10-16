package ru.boldr.memebot.model;

public enum Command {

    START("/fun"),
    STOP("/stopfun"),
    MAN("мужик"),
    MAN_REVERSE("обратно"),
    KAKASHKULES("/какашкулес"),
    BURGERTRACH("/бургертрах"),
    HARKACH("/2ch"),
    HARKACHMOD_OFF("/hmodoff"),
    HARKACHMOD_ON("/hmodon"),
    HARKACHBASE_UPDATE("/hbu"),
    HELP("/help");

    private final String command;

    public static String getCommands() {
        Command[] values = Command.values();
        StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        for (Command command : values) {
            count++;
            stringBuilder.append(count).append(") ").append(command.getCommand()).append(" ");
        }
        return stringBuilder.toString();
    }

    public String getCommand() {
        return command;
    }

    Command(String command) {
        this.command = command;
    }


}
