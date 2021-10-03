package ru.boldr.memebot.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.boldr.memebot.model.*;
import ru.boldr.memebot.model.Thread;
import ru.boldr.memebot.model.entity.CoolFile;
import ru.boldr.memebot.model.entity.HarKachFileHistory;
import ru.boldr.memebot.repository.CoolPostRepo;
import ru.boldr.memebot.repository.HarkachFileHistoryRepo;

@Component
@Slf4j
@AllArgsConstructor
public class HarkachParserService {

    private final static String MAIN_URL = "https://2ch.hk/b/threads.json";

    private final static String THREAD_URL = "https://2ch.hk/b/res/";

    private final static String HREAF_PATTERN = "href=\\\"/b/res/\\d+.html#\\d+\\\\";

    private final static String HREAF_PATTERN2 = "<a \\w+=\\\\\"/b/res/\\d+.html#\\d+\\\\\" class=\\\\\"post-reply-link\\\\\" data-thread=\\\\\"\\d+\\\\\" data-num=\\\\\"\\d+\\\\\">>>\\d+ ";

    private final static Set<String> funnyMap = Set.of("засмеялся", "зазмеился", "обосрался", "продристался", "WEBM",
            "ЦУИЬ",
            "ШЕБМ");

    public final static Set<String> coolSet = Set.of("збс", "заебис", "проиграл", "зачет", "прикольно", "лол", "смешно", "орнул", "вголосину");


    private final RestTemplate restTemplate = new RestTemplate();
    private final HarkachFileHistoryRepo harkachFileHistoryRepo;
    private final CoolPostRepo coolPostRepo;


    @Retryable
    public String getPicture(String chatId) {

        List<CoolFile> coolFiles = coolPostRepo.findAll();

        List<String> history = harkachFileHistoryRepo.findAllByChatId(chatId)
                .stream().map(HarKachFileHistory::getFileName).collect(Collectors.toList());


        List<CoolFile> coolFiles1 = StreamEx.of(coolFiles).filter(cf ->!history.contains(cf.getFileName())).toList();
        Optional<CoolFile> first = coolFiles1.stream().findFirst();

        String fileName;
        if (first.isPresent()) {
            fileName = "https://2ch.hk" + first.get().getFileName();

            harkachFileHistoryRepo.save(
                    HarKachFileHistory.builder()
                            .chatId(chatId)
                            .fileName(first.get().getFileName())
                            .build()
            );

            return fileName;
        }
        return "шутки кончились ):";
    }

    private CurrentThread getCurrentThread(Thread thread) {
        URI uri = null;
        try {
            uri = new URI(THREAD_URL + thread.num() + ".json");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        assert uri != null;
        return restTemplate.getForObject(uri, CurrentThread.class);
    }

    private boolean findFunnyThread(Thread thread) {
        for (String st : funnyMap) {
            if (thread.comment().contains(st)) {
                return true;
            }
        }
        return false;
    }

    public void getPictures() {
        List<CurrentThread> currentThreads = getCurrentThreads();
        List<Post> posts = getPosts(currentThreads);

        Map<Long, Post> numToPost = StreamEx.of(posts).toMap(Post::num, Function.identity());

        List<ThreadFile> threadFiles = new ArrayList<>();
        for (Post post : posts) {
            if (post.parent() == null) {
                continue;
            }

            for (String cool : coolSet) {
                if (post.comment().contains(cool)) {
                    threadFiles.addAll(getFunnyFiles(numToPost, post));
                }
            }
        }

        coolPostRepo.deleteAllByFileNameIn(StreamEx.of(threadFiles).map(ThreadFile::path).toList());

        coolPostRepo.saveAll(StreamEx.of(threadFiles).map(this::toCoolFile).toList());
    }

    private List<ThreadFile> getFunnyFiles(Map<Long, Post> numToPost, Post post) {
        Post coolPost = numToPost.get(post.parent());

        if (coolPost == null) {
            return List.of();
        }

        return coolPost.files();
    }

    private CoolFile toCoolFile(ThreadFile tf) {
        return CoolFile.builder()
                .fileName(tf.path())
                .build();
    }

    private List<Post> getPosts(List<CurrentThread> currentThreads) {
        List<Post> threadPosts = new ArrayList<>();
        for (CurrentThread thread : currentThreads) {
            List<ThreadList> threads = thread.threads();
            for (ThreadList threadList : threads) {
                List<Post> posts = threadList.posts();
                threadPosts.addAll(posts);
            }
        }
        return threadPosts;
    }

    private List<CurrentThread> getCurrentThreads() {
        URI uri = null;
        try {
            uri = new URI(MAIN_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        assert uri != null;
        Threads threads = restTemplate.getForObject(uri, Threads.class);


        assert threads != null;
        return (List<CurrentThread>) StreamEx.of(threads.threads()).map(this::getCurrentThread).toList();
    }
}
