package ru.boldr.memebot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Objects;

@JsonSerialize
public record ThreadFile(

        @JsonProperty("path")
        String path,
        @JsonProperty("md5")
        String md5,
        @JsonProperty("size")
        String size,
        @JsonProperty("height")
        String height




) {
        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                ThreadFile that = (ThreadFile) o;
                return Objects.equals(md5, that.md5) && Objects.equals(size, that.size) &&
                        Objects.equals(height, that.height);
        }

        @Override
        public int hashCode() {
                return Objects.hash(md5, size, height);
        }
}
