package ru.boldr.memebot.repository;


import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.boldr.memebot.model.entity.MediaFile;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MediaFileQueryResolver implements GraphQLQueryResolver {
    private final MediaFileRepository mediaFileRepository;

    public List<MediaFile> mediaFiles() {
        return mediaFileRepository.findAll();
    }

    public MediaFile mediaFileById(Long id) {
        return mediaFileRepository.findById(id).orElse(null);
    }
}
