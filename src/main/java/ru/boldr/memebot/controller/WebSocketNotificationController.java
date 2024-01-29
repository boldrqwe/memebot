package ru.boldr.memebot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketNotificationController {

    private final SimpMessagingTemplate template;


    public void sendThreadCompletedNotification(String threadId) {
        // Построение сообщения
        String message = "Тред " + threadId + " сформирован";
        // Отправка сообщения
        this.template.convertAndSend("/topic/threadStatus", message);
    }
}
