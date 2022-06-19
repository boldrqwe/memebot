package ru.boldr.memebot.configuration;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.locks.ReentrantLock;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.local.LocalBucket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.boldr.memebot.service.TelegramSemaphore;

@Configuration
public class HarkachConfig {

    @Bean
    public Bandwidth bandwidth() {
        //https://core.telegram.org/bots/faq#my-bot-is-hitting-limits-how-do-i-avoid-this
        return Bandwidth.simple(20, Duration.of(1, ChronoUnit.MINUTES));
    }

    @Bean
    public LocalBucket bucket() {
        return Bucket4j.builder().addLimit(bandwidth()).build();
    }

    @Bean
    public TelegramSemaphore telegramSemaphore(LocalBucket bucket) {
        return new TelegramSemaphore(bucket, new ReentrantLock());
    }
}