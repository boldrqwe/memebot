package ru.boldr.memebot.service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import io.github.bucket4j.local.LocalBucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TelegramSemaphore {

    private final LocalBucket bucket;
    private final ReentrantLock lock;

    public void executeInLock(Runnable function, long count) {
        lock.lock();
        try {
            var probe = bucket.estimateAbilityToConsume(count);
            TimeUnit.NANOSECONDS.sleep(probe.getNanosToWaitForRefill());
            bucket.tryConsume(count);
            function.run();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}