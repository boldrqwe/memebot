package ru.boldr.memebot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ThreadMediaFileDto {
    private Long id;
    private String fileType;
    private String fileUrl;
    // Вы можете добавить другие поля, необходимые для отображения
}
