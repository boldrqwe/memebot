package ru.boldr.memebot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.boldr.memebot.configuration.HarkachConfig;

@SpringBootApplication
@EnableScheduling
@Import(HarkachConfig.class)
public class MemebotApplication {
    public static void main(String[] args) {
        SpringApplication.run(MemebotApplication.class, args);
    }
}