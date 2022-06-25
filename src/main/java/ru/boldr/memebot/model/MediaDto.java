package ru.boldr.memebot.model;

import java.util.ArrayList;

import org.telegram.telegrambots.meta.api.objects.media.InputMedia;

public record MediaDto(ArrayList<InputMedia> inputMedia, ArrayList<String> webmPaths) {
}
