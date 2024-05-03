package ru.boldr.memebot.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.boldr.memebot.model.entity.ChatPermission;
import ru.boldr.memebot.repository.ChatPermissionRepo;

import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
@Slf4j
public class JokeService {


    public final static Set<String> coolSet = Set.of("збс", "заебис", "хорошо", "охуено", "отлично", "замечательно",
            "офигено", "прекрасно", "проиграл", "зачет", "аха", "аза", "прикольно", "лол", "смешно", "+");
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
