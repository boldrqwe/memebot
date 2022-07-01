package ru.boldr.memebot.configuration;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.locks.ReentrantLock;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.local.LocalBucket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.boldr.memebot.TelegramBot;
import ru.boldr.memebot.service.TelegramSemaphore;

@Configuration
public class HarkachConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBot telegramBot) {
        TelegramBotsApi telegramBotsApi = null;
        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(telegramBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return telegramBotsApi;
    }

    @Bean
    public Bandwidth bandwidth() {
        //https://core.telegram.org/bots/faq#my-bot-is-hitting-limits-how-do-i-avoid-this
        return Bandwidth.simple(6, Duration.of(1, ChronoUnit.MINUTES));
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