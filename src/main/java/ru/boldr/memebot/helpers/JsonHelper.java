package ru.boldr.memebot.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class JsonHelper {


    private final ObjectMapper objectMapper;

    public JsonHelper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String lineToMap(Object object) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    public String videoId(Object object) {
        String[] tokens = null;
        try {
            tokens = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object).split(" ");

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].contains("videoId")) {
                return tokens[i + 2];
            }
        }
        return null;
    }
}
