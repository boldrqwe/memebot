package ru.boldr.memebot.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MediaFileDto {
    private byte[] fileData;
    private String fileType;
    private String comment;
    private String fileUrl;
    private boolean isThreadDownloaded;
}
