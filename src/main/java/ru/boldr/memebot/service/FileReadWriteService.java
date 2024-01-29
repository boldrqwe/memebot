package ru.boldr.memebot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.boldr.memebot.model.entity.MediaFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

import static org.apache.commons.io.FileUtils.deleteDirectory;


@Service
@RequiredArgsConstructor
public class FileReadWriteService {

    private static final Path PATH = Paths.get("files", "media_files");

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();


    public Path writeFiles(Set<MediaFile> mediaFiles) throws IOException {
        readWriteLock.readLock().lock(); // Блокировка для чтения
        try {
            AtomicBoolean errorOccurred = new AtomicBoolean(false);
            if (!Files.isReadable(PATH)) {
                throw new RuntimeException("Ошибка чтения пути");
            }
            ExecutorService executorService = Executors.newWorkStealingPool(8);
            Path folder = PATH.resolve(Instant.now().toString().replaceAll(":", "-")
                    .replaceAll("Z", ""));
            CountDownLatch latch = new CountDownLatch(mediaFiles.size());
            try {
                mediaFiles.forEach(f -> writeFile(errorOccurred, folder, f, latch, executorService));
                executorService.shutdown();
                latch.await();
            } catch (Exception e) {
                deleteDirectory(new File(folder.toString()));
                try {
                    throw e; // Переброс исключения после удаления директории
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
            if (errorOccurred.get()) {
                throw new RuntimeException("Ошибка при записи");
            }
            return folder;
        } finally {
            readWriteLock.readLock().unlock(); // Разблокировка
        }
    }

    public ConcurrentLinkedQueue<MediaFileDto> readFiles(Collection<MediaFile> mediaFiles) throws IOException {

        AtomicBoolean errorOccurred = new AtomicBoolean(false);
        ExecutorService executorService = Executors.newWorkStealingPool(8);
        ConcurrentLinkedQueue<MediaFileDto> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();
        CountDownLatch latch = new CountDownLatch(mediaFiles.size());
        for (var f : mediaFiles) {
            if (errorOccurred.get()) {
                throw new RuntimeException("ошибка при чтении файлов");
            }
            executorService.submit(new FileReadTask(f, concurrentLinkedQueue, errorOccurred, latch));
        }
        executorService.shutdown();
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (errorOccurred.get()) {
            throw new RuntimeException("Ошибка при чтении");
        }
        return concurrentLinkedQueue;
    }


    private void writeFile(AtomicBoolean errorOccurred, Path folder, MediaFile m, CountDownLatch latch,
                           ExecutorService executorService) {
        if (errorOccurred.get()) {
            throw new RuntimeException("Ошибка при записи");
        }
        UUID uuid = UUID.randomUUID();

        Path filePath = folder.resolve(uuid.toString());
        executorService.submit(new FileWritingTask(errorOccurred, filePath.toString(), m, latch));
    }

    public long countFolders(Path path) {
        try (Stream<Path> files = Files.list(path)) {
            return files.count();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public int deleteFiles(Collection<MediaFile> mediaFiles) {
        readWriteLock.writeLock().lock(); // Блокировка для записи
        try {
            for (MediaFile file : mediaFiles) {
                delete(file);
            }
            return mediaFiles.size();
        } finally {
            readWriteLock.writeLock().unlock(); // Разблокировка
        }
    }

    private void delete(MediaFile file) {
        Path filePath = Paths.get(file.getFilePath());
        try {
            Files.deleteIfExists(filePath); // Удаление файла
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Проверка и удаление пустой папки
        deleteEmptyParentFolder(filePath.getParent());
    }

    private void deleteEmptyParentFolder(Path folderPath) {
        if (Files.exists(folderPath) && Files.isDirectory(folderPath)) {
            try (Stream<Path> files = Files.list(folderPath)) {
                boolean isEmpty = files.findAny().isEmpty();
                if (isEmpty) {
                    Files.deleteIfExists(folderPath);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}


