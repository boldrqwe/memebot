package ru.boldr.memebot.controller;

import java.io.IOException;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.boldr.memebot.service.WikiParser;

@RestController
@RequiredArgsConstructor
public class WikipediaController {

    private final WikiParser wikiParser;

    @GetMapping()
    public String getWikiPage(
    ) {

        try {
            return wikiParser.getPage("какашкулес.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}
