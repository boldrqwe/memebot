package ru.boldr.memebot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.boldr.memebot.service.WikiParser;

@RestController
@RequiredArgsConstructor
public class WikipediaController {

    private final WikiParser wikiParser;

    @GetMapping("/{name}")
    public String getWikiPage(
            @PathVariable("name") String name
    ) {
        return wikiParser.getPage(name);
    }

}
