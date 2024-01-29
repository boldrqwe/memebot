package ru.boldr.memebot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.boldr.memebot.model.entity.CoolFile;
import ru.boldr.memebot.model.entity.MediaFile;
import ru.boldr.memebot.repository.CoolFileRepo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.Base64;

import static ru.boldr.memebot.service.HarkachParserService.getDvachUrl;
import static ru.boldr.memebot.service.HarkachParserService.getExtension;

@Service
@RequiredArgsConstructor
public class PictureService {

    private final CoolFileRepo coolFileRepo;
    private final HarkachMarkupConverter harkachMarkupConverter;


    public Page<CoolFile> listAllByPage(Pageable pageable) {
        return coolFileRepo.findAll(pageable);
    }

    public String formatComment(String comment) {
        return harkachMarkupConverter.convertToHtml(comment);
    }

    public String getMediaType(String fileName) {
        return switch (fileName) {
            case "png", "jpg", "jpeg", "gif", "webp" -> "image";
            case "mp4", "avi", "mov", "webm" -> "video";
            default -> throw new RuntimeException("wrong type");
        };
    }

    public String encodeBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public MediaFile downloadIfAvailable(CoolFile coolFile, Instant now, @Nullable Long parentId) {
        String fileName = coolFile.getFileName();
        URL dvachUrl = getDvachUrl(fileName);

        try {
            InputStream inputStream = dvachUrl.openStream();
            byte[] fileData = inputStream.readAllBytes();
            inputStream.close();

            int fileSize = fileData.length;

            if (fileSize > 0) {
                MediaFile mediaFile = new MediaFile();
                mediaFile.setFileData(fileData);
                mediaFile.setFileType(getExtension(coolFile.getFileName()));
                mediaFile.setComment(coolFile.getMessage());
                mediaFile.setCreatedAt(now);
                mediaFile.setFileUrl(coolFile.getFileName());
                mediaFile.setParentId(parentId);

                // Сохраняем MediaFile в репозитории
                return mediaFile;
            }
        } catch (IOException e) {
            return null;
        }

        return null; // В случае ошибки или если файл недоступен, возвращаем null
    }

}

