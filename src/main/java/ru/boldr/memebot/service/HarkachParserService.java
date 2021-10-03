package ru.boldr.memebot.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.boldr.memebot.model.*;
import ru.boldr.memebot.model.Thread;
import ru.boldr.memebot.model.entity.HarKachFileHistory;
import ru.boldr.memebot.repository.HarkachFileHistoryRepo;

@Component
@Slf4j
@AllArgsConstructor
public class HarkachParserService {

    private final static String MAIN_URL = "https://2ch.hk/b/threads.json";

    private final static String THREAD_URL = "https://2ch.hk/b/res/";

    private final static String HREAF_PATTERN = "href=\\\"/b/res/\\d+.html#\\d+\\";

    private final static Set<String> funnyMap = Set.of("засмеялся", "зазмеился", "обосрался", "продристался", "WEBM",
            "ЦУИЬ",
            "ШЕБМ");

    private final RestTemplate restTemplate = new RestTemplate();
    private final HarkachFileHistoryRepo harkachFileHistoryRepo;

    public String getPicture(String chatId) {
        URI uri = null;
        try {
            uri = new URI(MAIN_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        assert uri != null;
        Threads threads = restTemplate.getForObject(uri, Threads.class);

        assert threads != null;
        List<Thread> funnyThread = StreamEx.of(threads.threads()).filter(this::findFunnyThread).toList();
        if (funnyThread.size() < 1) {
            return null;
        }

        List<CurrentThread> currentThreads = StreamEx.of(funnyThread).map(this::getCurrentThread).toList();

        for (CurrentThread thread : currentThreads) {
            List<ThreadList> threadList = thread.threads();

            if (threadList.size() < 1) {
                continue;
            }

            for (Post post : threadList.get(0).posts()) {
                Long parent = post.parent();

                if (parent == 0) {
                    continue;
                }

                for (String cool : JokeService.coolSet) {

                    if (post.comment().contains(cool)) {

                        List<ThreadFile> threadFiles = post.files();

                        if (threadFiles == null || threadFiles.size() < 1) {
                            continue;
                        }

                        ThreadFile threadFile = threadFiles.get(0);
                        String fileName = "https://2ch.hk" + threadFile.path();

                        if (harkachFileHistoryRepo.findByChatIdAndFileName(chatId, fileName).isPresent()) {
                            continue;
                        }

                        harkachFileHistoryRepo.save(
                                HarKachFileHistory.builder()
                                        .chatId(chatId)
                                        .fileName(fileName)
                                        .build()
                        );

                        return fileName;
                    }
                }

            }
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

}
