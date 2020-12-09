package ru.boldr.memebot.handlers;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.boldr.memebot.api.YoutubeApiSearch;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Component
public class UpdateHandler {
    private final String YOUTUBE_SEACH_LINE = "https://www.youtube.com/watch?v=";

    public YoutubeApiSearch getYoutubeAPIFInd() {
        return youtubeAPIFInd;
    }

    public void setYoutubeAPIFInd(YoutubeApiSearch youtubeAPIFInd) {
        this.youtubeAPIFInd = youtubeAPIFInd;
    }

    private YoutubeApiSearch youtubeAPIFInd;

    public UpdateHandler(YoutubeApiSearch youtubeAPIFInd) {
        this.youtubeAPIFInd = youtubeAPIFInd;
    }

    public String answer(Update update) {
        return prepareAnswerText(parse(update));
    }

    private String[] parse(Update update) {
        // userName = update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName();
        String command = update.getMessage().getText().toLowerCase().replaceAll(",", "");
        return command.split(" ");
    }

    private String prepareAnswerText(String[] tokens) {
        String findQ = find(tokens);
        List<String> commandsList = new LinkedList<>();
        if (!findQ.equals(null)) {
            commandsList.add(findQ);
        }
        return String.join("", commandsList);
    }

    public static void main(String[] args) {
    }

    public String find(String[] tokens) {
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equals("найди")) {
                String query = Arrays.toString(tokens);
                query = query.substring(query.indexOf("("), query.indexOf(")"));
                query = query.replaceAll("\\(", "");
                switch (tokens[i + 1]) {
                    case "видео":
                        return (YOUTUBE_SEACH_LINE + youtubeAPIFInd.find(query));
                    case "музыку":
                        return (YOUTUBE_SEACH_LINE + youtubeAPIFInd.find(query));
                    case "картинку":
                        return "картинка";
                    default:
                        return null;
                }
            }
        }
        return null;
    }
}
