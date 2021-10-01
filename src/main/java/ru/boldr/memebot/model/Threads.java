package ru.boldr.memebot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;
@JsonSerialize
public record Threads(
        @JsonProperty("board")
        String board,
        @JsonProperty("threads")
        List<Thread> threads
) {
}
