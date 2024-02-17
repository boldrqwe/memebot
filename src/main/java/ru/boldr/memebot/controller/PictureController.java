package ru.boldr.memebot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.boldr.memebot.model.entity.MediaFile;
import ru.boldr.memebot.service.MediaFileDto;
import ru.boldr.memebot.service.MediaFileService;
import ru.boldr.memebot.service.PictureService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/pictures")
public class PictureController {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final PictureService pictureService;
    private final MediaFileService mediaFileService;

    @GetMapping
    public String listPictures(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Получите страницу медиа-файлов с данными файла
        Page<MediaFileDto> mediaFilesPage = mediaFileService.findMediaFilesWithFileData(PageRequest.of(page, size));
        model.addAttribute("mediaFilesPage", mediaFilesPage);
        model.addAttribute("pictureService", pictureService);

        return "pictures";
    }

    @PostMapping("/start-thread-processing")
    public ResponseEntity<?> startThreadProcessing(@RequestBody Map<String, String> payload) {
        System.out.println("бонговик");
        String fileUrl = payload.get("fileUrl").replaceAll("-", "/");
        List<MediaFile> fileUrl1 = mediaFileService.findByFileUrl(fileUrl);
        if (fileUrl1.isEmpty()) {
            log.info("не найден фал при формированиии");
            return ResponseEntity.ok().build();
        }
        if (fileUrl1.get(0).getIsTreadDownloaded()) {
            return ResponseEntity.ok().build();
        }

        // Отправка сообщения в Kafka для асинхронной обработки
        kafkaTemplate.send("thread-requests-topic", fileUrl);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/thread/{fileUrl}")
    public String viewThread(
            @PathVariable String fileUrl,
            Model model) {
        log.info("бонгарняк");

        // Найти родительский MediaFile по fileUrl
        List<MediaFile> optionalMediaFile = mediaFileService.findByFileUrl(fileUrl.replaceAll("-", "/"));
        if (optionalMediaFile.isEmpty()) {
            throw new RuntimeException("file not fount");
        }
        List<Long> ids = optionalMediaFile.stream().map(MediaFile::getId).toList();
        // Получить список дочерних медиафайлов
        Collection<MediaFileDto> childFiles = mediaFileService.findMediaDtoByParentIdIn(ids);


        model.addAttribute("childFiles", childFiles);
        model.addAttribute("parentFile", optionalMediaFile.get(0));
        model.addAttribute("pictureService", pictureService);

        return "threadView"; // Имя шаблона Thymeleaf для страницы треда
    }
}

