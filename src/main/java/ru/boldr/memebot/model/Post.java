package ru.boldr.memebot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

@JsonSerialize
public record Post(
        @JsonProperty("comment")
        String comment,
        @JsonProperty("files")
        List<ThreadFile> files,
        @JsonProperty("parent")
        Long parent,
        @JsonProperty("num")
        Long num
) {
}
