package ru.boldr.memebot.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.boldr.memebot.model.entity.MediaFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@AllArgsConstructor
public class FileWritingTask implements Runnable {

    private AtomicBoolean errorOccurred;
    private String path;
    private MediaFile mediaFile;
    private CountDownLatch latch;


    @Override
    public void run() {
        File file = new File(path);
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs(); // Создать родительские директории, если они не существуют
        }
        try (var out = new FileOutputStream(path)) {
            out.write(mediaFile.getFileData());
            mediaFile.setFilePath(path);
        } catch (IOException e) {
            e.printStackTrace();
            errorOccurred.set(true); // Установка флага в случае ошибки
        } finally {
            latch.countDown();
        }
    }
}



