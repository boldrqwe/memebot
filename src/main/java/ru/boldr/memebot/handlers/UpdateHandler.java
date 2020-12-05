package ru.boldr.memebot.handlers;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.boldr.memebot.YoutubeAPIFind;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class UpdateHandler {
    private final String YOUTUBE_SEACH_LINE = "https://www.youtube.com/watch?v=";
    private  String userName = "1";
    private  String video = "1";
    private  String music = "1";
    private  String picture = "1";

    private  Map<String, String> COMMANDS = Map.of(
            "привет", String.format("привет %s", userName),
            "видео", video + " ",
            "музыку", music + " ",
            "картинку", picture + " ");


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getMusic() {
        return music;
    }

    public void setMusic(String music) {
        this.music = music;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public YoutubeAPIFind getYoutubeAPIFInd() {
        return youtubeAPIFInd;
    }

    public void setYoutubeAPIFInd(YoutubeAPIFind youtubeAPIFInd) {
        this.youtubeAPIFInd = youtubeAPIFInd;
    }

    public Map<String, String> getCOMMANDS() {
        return COMMANDS;
    }

    public void setCOMMANDS(Map<String, String> COMMANDS) {
        this.COMMANDS = COMMANDS;
    }

    private YoutubeAPIFind youtubeAPIFInd;

    public UpdateHandler(YoutubeAPIFind youtubeAPIFInd) {
        this.youtubeAPIFInd = youtubeAPIFInd;
    }

    public String answer(Update update) {
        return prepareAnswerText(parse(update));
    }

    public String find(String[] tokens) {
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equals("найди")) {
                String query = Arrays.toString(tokens);
                query = query.substring(query.indexOf("("), query.indexOf(")"));
                query = query.replaceAll("\\(","");
                switch  (tokens[i + 1]) {
                    case "видео":
                      return (YOUTUBE_SEACH_LINE+youtubeAPIFInd.find(query));
                    case "музыку":
                       return (YOUTUBE_SEACH_LINE+youtubeAPIFInd.find(query));
                    case "картинку":
                       return "картинка";
                    default:
                       return null;

                }
            }
        }

        return null;
    }

    private String[] parse(Update update) {
        userName = update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName();
        String command = update.getMessage().getText().toLowerCase().replaceAll(",", "");
        return command.split(" ");
    }

    private String prepareAnswerText(String[] tokens) {
       String findQ = find(tokens);
        List<String> commandsList = new LinkedList<>();
        if(!findQ.equals(null)){
            commandsList.add(findQ);
        }
        return String.join("", commandsList);
    }

    public static void main(String[] args) {

    }
}
