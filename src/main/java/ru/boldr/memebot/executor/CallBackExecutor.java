package ru.boldr.memebot.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.boldr.memebot.TelegramBot;
import ru.boldr.memebot.repository.HarkachModHistoryRepo;
import ru.boldr.memebot.service.HarkachParserService;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallBackExecutor {

    private final TelegramBot telegramBot;

    private final HarkachModHistoryRepo harkachModHistoryRepo;

    private final HarkachParserService harkachParserService;
}
