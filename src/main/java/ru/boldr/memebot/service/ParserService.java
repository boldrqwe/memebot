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
import ru.boldr.memebot.model.Thread;
import ru.boldr.memebot.model.Threads;

@Component
@Slf4j
@AllArgsConstructor
public class ParserService {

    private final static String URL = "https://2ch.hk/b/threads.json";

    private final static Set<String> funnyMap = Set.of("засмеялся", "зазмеился", "обосрался", "продристался", "WEBM",
            "ЦУИЬ",
            "ШЕБМ");

    private final RestTemplate restTemplate = new RestTemplate();

    public String getPicture() {
        URI uri = null;
        try {
            uri = new URI(URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        assert uri != null;
        Threads threads = restTemplate.getForObject(uri, Threads.class);

        assert threads != null;
        List<Thread> funnyThread = StreamEx.of(threads.threads()).filter(this::findFunnyThread).toList();

        return null;
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
