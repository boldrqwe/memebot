package ru.boldr.memebot.model;

public enum Command {

    START ("/fun"),
    STOP ("/stopfun"),
    MAN("/мужик"),
    MAN_REVERSE("/обратно");



    private final String command;
    public String getCommand() {
        return command;
    }

    Command(String command){
        this.command = command;
    }


}
