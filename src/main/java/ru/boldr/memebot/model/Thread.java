package ru.boldr.memebot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record Thread(
        @JsonProperty("comment")
        String comment,
        @JsonProperty("lastHit")
        Long lastHit,
        @JsonProperty("num")
        Long num,
        @JsonProperty("postsCount")
        Integer postsCount,
        @JsonProperty("score")
        Integer score,
        @JsonProperty("subject")
        String subject,
        @JsonProperty("timestamp")
        Long timestamp,
        @JsonProperty("views")
        Integer views) {
}
