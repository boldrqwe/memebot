package ru.boldr.memebot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.boldr.memebot.model.entity.MediaFile;
import ru.boldr.memebot.repository.FileUrl;
import ru.boldr.memebot.repository.MediaFileRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaFileService {

    private final MediaFileRepository mediaFileRepository;
    private final FileReadWriteService fileReadWriteService;


    public Collection<MediaFile> getAllMediaFiles() {
        return mediaFileRepository.findAll();
    }

    public MediaFile getMediaFileById(Long id) {
        return mediaFileRepository.findById(id).orElse(null);
    }

    public MediaFile createMediaFile(MediaFile mediaFile) {
        // Добавьте дополнительную логику для создания файла, если это необходимо
        return mediaFileRepository.save(mediaFile);
    }

    public Collection<MediaFile> saveAll(Collection<MediaFile> mediaFiles) {
        // Добавьте дополнительную логику для создания файла, если это необходимо
        return mediaFileRepository.saveAll(mediaFiles);
    }

    @Transactional
    public void deleteMediaFile(Collection<MediaFile> mediaFiles) {
        if (mediaFiles.isEmpty()) {
            return;
        }
        fileReadWriteService.deleteFiles(mediaFiles);
        mediaFileRepository.deleteAll(mediaFiles);
    }


    public Page<MediaFileDto> findMediaFilesWithFileData(Pageable pageable) {
        Page<MediaFile> mediaFiles = mediaFileRepository.findAllByCommentNotNullOrderByCreatedAtDesc(pageable);
        return getMediaFiles(mediaFiles, pageable);
    }

    private Page<MediaFileDto> getMediaFiles(Page<MediaFile> mediaFiles, Pageable pageable) {
        try {
            ConcurrentLinkedQueue<MediaFileDto> mediaFileDtos = fileReadWriteService.readFiles(mediaFiles.getContent());
            return new PageImpl<>(mediaFileDtos.stream().toList(), pageable, mediaFiles.getTotalElements());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public List<FileUrl> findAllWithOutData() {
        return mediaFileRepository.findAllWithoutData();
    }

    public List<MediaFile> findAllByFileUrlIn(Collection<String> fileUrls) {
        if (fileUrls.isEmpty()) {
            return List.of();
        }
        return mediaFileRepository.findAllByFileUrlIn(fileUrls);
    }

    public List<MediaFile> findByFileUrl(String message) {
        return mediaFileRepository.findAllByFileUrl(message);
    }

    public void saveMediaFiles(Set<MediaFile> mediaFiles) {
        Optional<Path> path = writeFiles(mediaFiles);
        path.ifPresent(folder -> log.info("записано %d".formatted(fileReadWriteService.countFolders(folder))));
        if (path.isEmpty()) {
            log.info("mediaFiles is empty");
            return;
        }
        saveMedia(mediaFiles, path);
    }

    private void saveMedia(Set<MediaFile> mediaFiles, Optional<Path> path) {
        try {
            Collection<MediaFile> saved = saveAll(mediaFiles);
            log.info("saved %d".formatted(saved.size()));
        } catch (Exception e) {
            path.map(Objects::toString).map(File::new).ifPresent(i -> {
                try {
                    FileUtils.deleteDirectory(i);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
            throw e;
        }
    }

    private Optional<Path> writeFiles(Set<MediaFile> mediaFiles) {
        try {
            if (!mediaFiles.isEmpty()) {
                return Optional.of(fileReadWriteService.writeFiles(mediaFiles));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public void save(MediaFile mediaFile) {
        mediaFileRepository.save(mediaFile);
    }

    public Collection<MediaFile> findByParentIdIn(List<Long> parentIds) {
        return mediaFileRepository.findAllByParentIdIn(parentIds);
    }

    public Collection<MediaFileDto> findMediaDtoByParentIdIn(List<Long> id) {
        Collection<MediaFile> allByParentIdIn = mediaFileRepository.findAllByParentIdIn(id);
        try {
            return fileReadWriteService.readFiles(allByParentIdIn);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

