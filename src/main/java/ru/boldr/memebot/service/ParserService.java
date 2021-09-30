package ru.boldr.memebot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@Component
@Slf4j
@AllArgsConstructor
public class ParserService {

    private final static String URL = "https://2ch.hk/b/threads.json";
    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate =  new RestTemplate();
    public String getPicture() {
        URI uri = null;
        try {
             uri = new URI(URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        assert uri != null;
        Threads forObject = restTemplate.getForObject(uri, Threads.class);

        return null;
    }


}
