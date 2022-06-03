package ru.boldr.memebot;

import java.io.File;
import java.util.Locale;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.boldr.memebot.handlers.UpdateHandler;
import ru.boldr.memebot.helpers.JsonHelper;
import ru.boldr.memebot.model.Command;
import ru.boldr.memebot.model.entity.BotMessageHistory;
import ru.boldr.memebot.model.entity.HarkachModHistory;
import ru.boldr.memebot.repository.HarkachModHistoryRepo;
import ru.boldr.memebot.service.HarkachParserService;
import ru.boldr.memebot.service.MessageHistoryService;
import ru.boldr.memebot.service.SpeakService;
import ru.boldr.memebot.service.ThreadComment;
import ru.boldr.memebot.service.WikiParser;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private final static Logger logger = LoggerFactory.getLogger(TelegramBot.class);
    private final JsonHelper jsonHelper;
    private final UpdateHandler updateHandler;
    private final HarkachParserService harkachParserService;
    private final WikiParser wikiparser;
    private final MessageHistoryService messageHistoryService;
    private final HarkachModHistoryRepo harkachModHistoryRepo;
    private final TransactionTemplate transactionTemplate;
    private final SpeakService speakService;

    @Override
    public String getBotUsername() {
        return "MementosFunniestForMeBot";
    }

    @Override
    public String getBotToken() {
        return "1472867697:AAH1cY6xZPZ5SB7UgTtoW8mqhA5kRdyp0Kg";
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage()) {
            String text = update.getMessage().getText();
            if (text.toLowerCase().contains("бот скажи")) {
                SendMessage message = speakService.makeMessage(text, update);
                execute(message);
            }
        }

        if (update.hasCallbackQuery()) {
            if (update.getCallbackQuery().getData().equals("зов обратно")) {
                execute(SendPhoto.builder()
                        .chatId(update.getCallbackQuery().getMessage().getChatId().toString())
                        .photo(new InputFile(new File("data/img.png")))
                        .build());
            }
        }

        logger.info("new update: {}", jsonHelper.lineToMap(update));
        if (!update.hasMessage() || update.getMessage().getText() == null) {
            logger.warn("massage is empty");
            return;
        }

        if (update.getMessage().getText().toLowerCase(Locale.ROOT).equals(Command.HELP.getCommand())) {

            execute(new SendMessage(update.getMessage().getChatId().toString(), Command.getCommands()));

        }

        if (!updateHandler.checkWriteMessagePermission(update.getMessage())) {
            return;
        }

        String chatId = update.getMessage().getChatId().toString();

        String answer = updateHandler.saveFunnyJoke(update);
        if (answer != null) {
            execute(new SendMessage(chatId, answer));
        }

        if (update.getMessage().getText() == null) {
            logger.warn("massage is null");
            return;
        }

        execute(
                update,
                chatId,
                update.getMessage().getText().toLowerCase(Locale.ROOT)
        );

    }

    private void execute(Update update, String chatId, String command) throws TelegramApiException {
        if (command.toLowerCase(Locale.ROOT).contains(Command.MAN.getCommand())) {
            execute(new SendAnimation(chatId, new InputFile(new File("files/man.mp4"))));
            messageHistoryService.save(BotMessageHistory.builder()
                    .chatId(chatId)
                    .messageId(update.getMessage().getMessageId())
                    .build());
        }

        if (Command.MAN_REVERSE.getCommand().equals(command)) {
            execute(new SendAnimation(chatId, new InputFile(new File("files/man_reverse.mp4"))));
        }

        if (Command.KAKASHKULES.getCommand().equals(command)) {
            execute(new SendMessage(chatId, "http://34.149.170.188/какашкулес"));
        }

        if (Command.BURGERTRACH.getCommand().equals(command)) {
            execute(new SendMessage(chatId, "http://34.149.170.188/бургертрах"));
        }

        if (Command.HARKACH.getCommand().equals(command)) {
            ThreadComment threadComment = harkachParserService.getContent(chatId);

            execute(new SendMessage(chatId, threadComment.picture()));
            if (threadComment.comment() != null && !threadComment.comment().isEmpty()) {
                execute(new SendMessage(chatId, threadComment.comment()));
            }
        }

        if (Command.HARKACHBASE_UPDATE.getCommand().equals(command)) {
            harkachParserService.loadContent();
        }

        if (Command.HARKACHMOD_ON.getCommand().equals(command)) {
            harkachModHistoryRepo.save(
                    HarkachModHistory.builder()
                            .chatId(update.getMessage().getChatId().toString())
                            .build()
            );
        }

        if (Command.HARKACHMOD_OFF.getCommand().equals(command)) {

            transactionTemplate.executeWithoutResult(status ->
                    harkachModHistoryRepo.deleteByChatId(update.getMessage().getChatId().toString()));

        }

    }
}
