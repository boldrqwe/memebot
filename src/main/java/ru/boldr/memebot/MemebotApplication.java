package ru.boldr.memebot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MemebotApplication {
    public static void main(String[] args) {
        SpringApplication.run(MemebotApplication.class, args);
    }
}

