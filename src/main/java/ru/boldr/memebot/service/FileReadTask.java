package ru.boldr.memebot.service;

import lombok.AllArgsConstructor;
import ru.boldr.memebot.model.entity.MediaFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@AllArgsConstructor
public class FileReadTask implements Runnable {
    private MediaFile mediaFile;
    private ConcurrentLinkedQueue<MediaFileDto> concurrentLinkedQueue;
    private AtomicBoolean errorOccurred;
    private CountDownLatch latch;


    @Override
    public void run() {
        try (var out = new FileInputStream(mediaFile.getFilePath())) {
            byte[] bytes = out.readAllBytes();
            concurrentLinkedQueue.add(new MediaFileDto(bytes, mediaFile.getFileType(),
                    mediaFile.getComment(), mediaFile.getFileUrl(), mediaFile.getIsTreadDownloaded()));
        } catch (IOException e) {
            errorOccurred.set(true); // Установка флага в случае ошибки
            throw new RuntimeException("ошибка чтения");
        } finally {
            latch.countDown();
        }
    }
}
