package ru.boldr.memebot.repository;


import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import org.springframework.stereotype.Component;
import ru.boldr.memebot.model.entity.MediaFile;

import java.time.Instant;

@Component
public class MediaFileMutationResolver implements GraphQLMutationResolver {

    private final MediaFileRepository mediaFileRepository;

    public MediaFileMutationResolver(MediaFileRepository mediaFileRepository) {
        this.mediaFileRepository = mediaFileRepository;
    }

    public MediaFile createMediaFile(String filePath, String fileType, String comment, Boolean isTreadDownloaded, Long parentId, String fileUrl, Instant createdAt) {
        MediaFile mediaFile = new MediaFile();

        // Заполните поля mediaFile
        return mediaFileRepository.save(mediaFile);
    }
}
