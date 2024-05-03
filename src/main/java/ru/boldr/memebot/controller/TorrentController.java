package ru.boldr.memebot.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.boldr.memebot.model.TorrentLink;
import ru.boldr.memebot.service.TorrentScraperService;

import java.util.List;

@RestController
@RequestMapping("/api/torrents")
public class TorrentController {

    private final TorrentScraperService scraperService;

    public TorrentController(TorrentScraperService scraperService) {
        this.scraperService = scraperService;
    }

    @GetMapping(value = "/search", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<TorrentLink> searchTorrents(@RequestParam List<String> tags) {
        return scraperService.scrapeLinks(tags, 40, 400);
    }
}