package ru.boldr.memebot.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.boldr.memebot.model.entity.ChatPermission;
import ru.boldr.memebot.model.entity.FunnyJoke;
import ru.boldr.memebot.repository.ChatPermissionRepo;
import ru.boldr.memebot.repository.FunnyJokeRepo;

import java.util.*;

@Service
@AllArgsConstructor
@Slf4j
public class JokeService {


    private final static Set<String> coolSet = Set.of("збс", "заебис", "хорошо", "охуено", "отлично", "замечательно",
            "офигено", "прекрасно", "проиграл", "зачет", "аха", "аза", "прикольно", "лол", "смешно", "+");
    private final FunnyJokeRepo funnyJokeRepo;
    private final ChatPermissionRepo chatPermissionRepo;


    public boolean checkWriteMessagePermission(Long chatId) {
        Optional<ChatPermission> optionalChatPermission = chatPermissionRepo.findByChatId(chatId);
        if (!optionalChatPermission.isPresent()) {
            return false;
        }
        ChatPermission chatPermission = optionalChatPermission.get();

        return chatPermission.getPermission();
    }

    public void savePermission(Message message, boolean permission) {
        Optional<ChatPermission> optionalChatPermission = chatPermissionRepo.findByChatId(message.getChatId());
        Long id = null;
        if (optionalChatPermission.isPresent()) {
            id = optionalChatPermission.get().getId();
        }
        chatPermissionRepo.save(ChatPermission.builder()
                .id(id)
                .chatId(message.getChatId())
                .permission(permission)
                .build());
    }

    public FunnyJoke saveFunnyJoke(Update update) {
        Message replyToMessage = update.getMessage().getReplyToMessage();
        if (replyToMessage == null) {
            return null;
        }

        if (update.getMessage().getFrom().getId().equals(replyToMessage.getFrom().getId())) {
            return null;
        }

        log.info("reply is {}", replyToMessage.getText());
        boolean isJoke = isJoke(update.getMessage().getText());
        if (!isJoke) {
            return null;
        }

        FunnyJoke funnyJoke = FunnyJoke.builder()
                .userId(replyToMessage.getFrom().getId())
                .chatId(replyToMessage.getChatId())
                .messageId(replyToMessage.getMessageId())
                .username(replyToMessage.getFrom().getUserName())
                .text(replyToMessage.getText())
                .build();

        return funnyJokeRepo.save(funnyJoke);

    }

    public String getStats(Long chatId) {
        List<FunnyJoke> funnyJokes = funnyJokeRepo.findAllByChatId(chatId);

        Map<String, Long> usernameToScore = new HashMap<>();

        for (FunnyJoke joke : funnyJokes) {
            String username = joke.getUsername();
            if (usernameToScore.get(username) == null) {
                usernameToScore.put(username, 1L);
            } else {
                Long score = usernameToScore.get(username);
                usernameToScore.put(username, score + 1L);
            }
        }

        StringBuilder scores = new StringBuilder();

        scores.append("scores: \n");

        for (String username : usernameToScore.keySet()) {
            scores.append(username).append(": ").append(usernameToScore.get(username)).append("\n");
        }

        return scores.toString();
    }

    private boolean isJoke(String text) {
        text = text.replace(",", "");
        text = text.replace(".", "");
        text = text.replace("!", "");
        text = text.replace("?", "");
        String[] words = text.split(" ");
        for (String word : words) {
            word = word.toLowerCase();
            if (coolWord(word)) {
                return true;
            }
        }
        return false;
    }

    private boolean coolWord(String word) {
        for (String cool : coolSet) {
            if (word.contains(cool)) {
                return true;
            }
        }
        return false;
    }


}
