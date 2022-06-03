package ru.boldr.memebot.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import lombok.RequiredArgsConstructor;
import one.util.streamex.StreamEx;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.boldr.memebot.model.Post;

@Service
@RequiredArgsConstructor
public class SpeakService {

    private final HarkachParserService harkachParserService;

    public String makeAnswer(String text) {

        String trim = text.replace("бот скажи", "").trim();
        String[] split = trim.split(" ");

        List<Post> posts = harkachParserService.getPosts();

        Map<Long, Post> numToPost = StreamEx.of(posts).toMap(Post::num, Function.identity());

        AtomicInteger maxMatch = new AtomicInteger();

        AtomicReference<Long> commentNum = new AtomicReference<>();

        for (Post post1 : posts) {

                AtomicInteger matchCount = new AtomicInteger();
                String comment = post1.comment();

                for (String st : split) {
                    if (comment.contains(st)) {
                        matchCount.getAndIncrement();
                    }
                }

                if (matchCount.get() > maxMatch.get()) {
                    maxMatch.addAndGet(matchCount.get());
                    commentNum.set(post1.num());
                }

        }
        Post post = numToPost.get(numToPost.get(commentNum.get()).parent());

        return Optional.ofNullable(post.comment()).orElse("не знаю я ");
    }

    public SendMessage makeMessage(String text, Update update) {
        String answer = makeAnswer(text)
                .replaceAll("<[\\s\\S]+>", "")
                .replaceAll("<br>", "");

        return SendMessage.builder()
                .replyToMessageId(update.getMessage().getMessageId())
                .text(answer)
                .chatId(update.getMessage().getChatId().toString())
                .build();
    }
}
