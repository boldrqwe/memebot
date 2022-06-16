package ru.boldr.memebot.service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.transaction.Transactional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;
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
    private final static String DVACH = "https://2ch.hk";

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
            return new ThreadComment(fileName, comment);
        }
        return new ThreadComment("шутки кончились ):", "");
    }

    public Map<String, List<InputMedia>> getContentMap(String chatId) {

        List<CoolFile> coolFiles = coolFileRepo.findAll();

        List<String> history = harkachFileHistoryRepo.findAllByChatId(chatId)
                .stream().map(HarKachFileHistory::getFileName).collect(Collectors.toList());

        List<CoolFile> coolFiles1 = StreamEx.of(coolFiles).filter(cf -> !history.contains(cf.getFileName())).toList();
        Map<String, List<CoolFile>> groupedFiles = StreamEx.of(coolFiles1).groupingBy(CoolFile::getMessage);

        List<InputMedia> inputMedias = new ArrayList<>();

        Optional<Map<String, List<CoolFile>>> first = StreamEx.of(groupedFiles).findFirst();
        if (first.isPresent()) {
            Map<String, List<CoolFile>> files = first.get();

            files.forEach((message, fileList) -> {
                inputMedias.addAll(StreamEx.of(fileList).map(this::toInputMedia).collect(Collectors.toList()));
                saveHarkachHistory(chatId, fileList);
            });

        }
        String comment = first.get().keySet().stream().findFirst().orElse("");
        return Map.of(comment, inputMedias);
    }

    private void saveHarkachHistory(String chatId, List<CoolFile> fileList) {
        var harKachFileHistories = StreamEx.of(fileList)
                .map(f -> toHarKachFileHistory(f, chatId))
                .toList();
        harkachFileHistoryRepo.saveAll(harKachFileHistories);
    }

    private HarKachFileHistory toHarKachFileHistory(CoolFile file, String chatId) {
        return HarKachFileHistory.builder()
                .chatId(chatId)
                .fileName(file.getFileName())
                .build();
    }

    @Nullable
    private InputMedia toInputMedia(CoolFile f) {
        String extension = getExtension(f.getFileName());
        URL url = null;
        try {
            url = new URL("https://2ch.hk" + f.getFileName());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (url != null) {
            return getInputMedia(url, extension);
        }
        return null;
    }

    public CurrentThread getCurrentThread(Thread thread) {
        Long num = thread.num();
        CurrentThread currentThread = getCurrentThread(num);
        return Optional.ofNullable(currentThread).orElse(new CurrentThread(List.of()));
    }

    private CurrentThread getCurrentThread(Long num) {
        URI uri = null;
        try {
            uri = Optional.of(new URI(THREAD_URL_RANDOM + num + ".json"))
                    .orElse(new URI(THREAD_URL_POLITACH + num + ".json"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        CurrentThread forObject = null;
        try {
            forObject = restTemplate.getForObject(Objects.requireNonNull(uri), CurrentThread.class);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        return forObject;
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

        List<CoolFile> coolFiles = StreamEx.of(postContentSet).map(this::toCoolFile).toList();

        List<String> all = coolFileRepo.findAll().stream().map(CoolFile::getFileName).toList();

        List<CoolFile> toSave =
                coolFiles.stream().filter(coolFile -> !all.contains(coolFile.getMessage())).toList();

        coolFileRepo.saveAll(toSave);
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
        return CoolFile.builder()
                .fileName(postContent.path())
                .message(postContent.message())
                .build();
    }

    private List<Post> getPosts(List<CurrentThread> currentThreads) {
        List<Post> posts = new ArrayList<>();
        currentThreads.forEach(ct -> ct.threads().forEach(ctt -> posts.addAll(ctt.posts())));
        return posts;
    }

    public List<Post> getPosts() {
        return getPosts(getCurrentThreads());
    }

    public List<CurrentThread> getCurrentThreads() {
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

    public List<InputMedia> getContentFromCurrentThread(String threadUrl, String chatId) {
        String[] split = threadUrl.split("/");
        List<InputMedia> inputMedias = new ArrayList<>();

        String stringNumWithHtml = split[5];

        String[] split1 = stringNumWithHtml.split("\\.");

        String stringNum = split1[0];
        Long num = Long.valueOf(stringNum);

        CurrentThread currentThread = getCurrentThread(num);

        List<ThreadList> threads = currentThread.threads();
        List<Post> postList = new ArrayList<>();

        threads.forEach(t -> postList.addAll(t.posts()));

        postList.forEach(post -> {
            inputMedias.addAll(createInputMedia(post));
        });

        List<CoolFile> coolFiles = StreamEx.of(postList).flatMap(p -> toCoolFiles(p).stream()).toList();

        var history = harkachFileHistoryRepo.findAll().stream()
                .map(HarKachFileHistory::getFileName)
                .toList();

        var coolFiles1 = StreamEx.of(coolFiles).filter(c -> !history.contains(c.getFileName())).toList();

        if (coolFiles1.isEmpty()) {
            return List.of();
        }

        saveHarkachHistory(chatId, coolFiles);

        return inputMedias;
    }

    private List<CoolFile> toCoolFiles(Post post) {
        return post.files().stream().map(this::toCoolFile).toList();
    }

    private List<InputMedia> createInputMedia(Post post) {
        List<PostContent> files = post.files();

        List<InputMedia> result = new ArrayList<>();
        files.forEach(f -> {
            String path = f.path();

            URL url = null;
            try {
                url = new URL(DVACH + path);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            String extension = getExtension(path);

            InputMedia inputMedia = getInputMedia(url, extension);
            if (inputMedia != null) {
                result.add(inputMedia);
            }
        });

        return result;
    }

    private InputMedia getInputMedia(URL url, String extension) {
        InputMedia inputMedia = null;
        switch (extension) {
            case ("jpg"), ("png") -> inputMedia = InputMediaPhoto.builder()
                    .media(url.toString())
                    .parseMode(ParseMode.HTML)
                    .build();

            case ("mp4") -> inputMedia = InputMediaVideo.builder()
                    .media(url.toString())
                    .parseMode(ParseMode.HTML)
                    .build();

            default -> {
            }
        }
        return inputMedia;
    }

    public String getExtension(String path) {
        String[] split = path.split("\\.");
        return split[split.length - 1];
    }
}
