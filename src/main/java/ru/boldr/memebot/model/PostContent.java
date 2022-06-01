package ru.boldr.memebot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record PostContent(

        @JsonProperty("path")
        String path,
        @JsonProperty("message")
        String message
) {
}
