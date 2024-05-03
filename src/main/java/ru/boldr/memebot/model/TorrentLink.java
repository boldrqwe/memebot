package ru.boldr.memebot.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonSerialize
public class TorrentLink {
    private String title;
    private String pageUrl;
    private String imageUrl;
    private String downloadLink;
}
