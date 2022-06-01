package ru.boldr.memebot.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.boldr.memebot.model.CurrentThread;
import ru.boldr.memebot.model.Post;
import ru.boldr.memebot.model.PostContent;
import ru.boldr.memebot.model.Thread;
import ru.boldr.memebot.model.ThreadList;
import ru.boldr.memebot.model.Threads;
import ru.boldr.memebot.model.entity.CoolFile;
import ru.boldr.memebot.model.entity.HarKachFileHistory;
import ru.boldr.memebot.repository.CoolFileRepo;
import ru.boldr.memebot.repository.HarkachFileHistoryRepo;

@Component
@Slf4j
@AllArgsConstructor
@Transactional
public class HarkachParserService {

    private final static String MAIN_URL = "https://2ch.hk/b/threads.json";

    private final static String POLITACH = "https://2ch.hk/po/threads.json";

    private final static String THREAD_URL_RANDOM = "https://2ch.hk/b/res/";
    private final static String THREAD_URL_POLITACH = "https://2ch.hk/po/res/";

    public final static Set<String> coolSet = Set.of("проиграл", " лол ", "смешно", "орнул", "вголосину", "lol");

    private final RestTemplate restTemplate = new RestTemplate();
    private final HarkachFileHistoryRepo harkachFileHistoryRepo;
    private final CoolFileRepo coolFileRepo;

    @Retryable
    public ThreadComment getContent(String chatId) {

        List<CoolFile> coolFiles = coolFileRepo.findAll();

        List<String> history = harkachFileHistoryRepo.findAllByChatId(chatId)
                .stream().map(HarKachFileHistory::getFileName).collect(Collectors.toList());

        List<CoolFile> coolFiles1 = StreamEx.of(coolFiles).filter(cf -> !history.contains(cf.getFileName())).toList();
        Optional<CoolFile> first = coolFiles1.stream().findFirst();

        String fileName;
        String comment;
        if (first.isPresent()) {
            fileName = "https://2ch.hk" + first.get().getFileName();

            harkachFileHistoryRepo.save(
                    HarKachFileHistory.builder()
                            .chatId(chatId)
                            .fileName(first.get().getFileName())
                            .build()
            );
            comment = first.get().getMessage();
            var threadUrl = first.get().getThreadUrl();
            return new ThreadComment(fileName, comment, threadUrl);
        }
        return new ThreadComment("шутки кончились ):", "", "");
    }

    private CurrentThread getCurrentThread(Thread thread) {
        URI uri = null;
        try {
            uri = Optional.of(new URI(THREAD_URL_RANDOM + thread.num() + ".json"))
                    .orElse(new URI(THREAD_URL_POLITACH + thread.num() + ".json"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        CurrentThread forObject = null;
        try {
            forObject = restTemplate.getForObject(Objects.requireNonNull(uri), CurrentThread.class);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        return Optional.ofNullable(forObject).orElse(new CurrentThread(List.of()));
    }

    public void loadContent() {
        List<CurrentThread> currentThreads = getCurrentThreads();
        log.info("получено тредов:{}", currentThreads.size());
        List<Post> posts = getPosts(currentThreads);

        Map<Long, Post> numToPost = StreamEx.of(posts).toMap(Post::num, Function.identity());

        List<PostContent> postContents = new ArrayList<>();
        for (Post post : posts) {
            if (post.parent() == null) {
                continue;
            }

            for (String cool : coolSet) {
                if (post.comment().toLowerCase(Locale.ROOT).contains(cool)) {
                    List<PostContent> funnyFiles = getFunnyFiles(numToPost, post);

                    postContents.addAll(funnyFiles);
                }
            }
        }

        Set<PostContent> postContentSet = StreamEx.of(postContents).toSet();

        coolFileRepo.deleteAllByFileNameIn(StreamEx.of(postContentSet).map(PostContent::path).toList());

        List<CoolFile> coolFiles = StreamEx.of(postContentSet).map(this::toCoolFile).toList();

        coolFileRepo.saveAll(coolFiles);
    }

    private List<PostContent> getFunnyFiles(Map<Long, Post> numToPost, Post post) {
        Post coolPost = numToPost.get(post.parent());

        if (coolPost == null) {
            return List.of();
        }

        return StreamEx.of(coolPost.files())
                .map(p -> new PostContent(p.path(), coolPost.comment()))
                .toList();
    }

    private CoolFile toCoolFile(PostContent postContent) {

        String message = postContent.message()
                .replaceAll("&\\W+\\S+;", "")
                .replaceAll("<\\S+\\s+\\S+;", "")
                .replaceAll("<\\S+>", "")
                .replaceAll("\\s\\S+=\"\\S+\"", "")
                .replaceAll("\\^1", "");

        return CoolFile.builder()
                .fileName(postContent.path())
                .message(message)
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
        URI random = null;
        URI politach = null;
        try {
            random = new URI(MAIN_URL);
            politach = new URI(POLITACH);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        Threads randomThreads = restTemplate.getForObject(Objects.requireNonNull(random), Threads.class);
        Threads politachThreads = restTemplate.getForObject(Objects.requireNonNull(politach), Threads.class);

        List<Thread> threads = null;
        if (randomThreads != null) {
            threads = randomThreads.threads();
            threads.addAll(Objects.requireNonNull(politachThreads).threads());
        }

        return StreamEx.of(Objects.requireNonNull(threads))
                .map(this::getCurrentThread)
                .remove(t -> t.threads().isEmpty())
                .toList();
    }
}
