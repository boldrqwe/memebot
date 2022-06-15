package ru.boldr.memebot;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.boldr.memebot.handlers.UpdateHandler;
import ru.boldr.memebot.helpers.JsonHelper;
import ru.boldr.memebot.model.Command;
import ru.boldr.memebot.model.entity.BotMessageHistory;
import ru.boldr.memebot.model.entity.HarkachModHistory;
import ru.boldr.memebot.repository.HarkachModHistoryRepo;
import ru.boldr.memebot.service.HarkachParserService;
import ru.boldr.memebot.service.HatGameService;
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
    private final HatGameService hatGameService;

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

        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            if (data.toLowerCase().contains("callback")) {
                var split = data.split(",");
                var threadUrl = split[0];
                var chatId = update.getCallbackQuery().getMessage().getChatId().toString();
                var inputMedias = harkachParserService.getContentFromCurrentThread(threadUrl, chatId);
                var partition = Lists.partition(inputMedias, 10);
                partition.forEach(part ->
                        sendMediaGroup(chatId, part)
                );
            }

            if (update.hasMessage()) {

                var message = update.getMessage();
                var chatId = message.getChatId().toString();
                var text = update.getMessage().getText();

                if (text.toLowerCase().contains("бот скажи")) {
                    SendMessage sendMessage = speakService.makeMessage(text, update);
                    execute(sendMessage);
                }

                if (text.equalsIgnoreCase("/шляпа")) {
                    hatGameService.process(update);
                }

                logger.info("new update: {}", jsonHelper.lineToMap(update));
                if (!update.hasMessage() || message.getText() == null) {
                    logger.warn("massage is empty");
                    return;
                }

                if (text.toLowerCase(Locale.ROOT).equals(Command.HELP.getCommand())) {

                    execute(new SendMessage(chatId, Command.getCommands()));

                }

                if (!updateHandler.checkWriteMessagePermission(message)) {
                    return;
                }

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
                        text.toLowerCase(Locale.ROOT)
                );
            }
        }
    }

    private void sendMediaGroup(String chatId, List<InputMedia> part) {
        try {
            TimeUnit.MINUTES.sleep(1L);
            execute(SendMediaGroup.builder()
                    .chatId(chatId)
                    .medias(part)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            execute(new SendMessage(chatId, "http://51.250.107.78/какашкулес"));
        }

        if (Command.BURGERTRACH.getCommand().equals(command)) {
            execute(new SendMessage(chatId, "http://51.250.107.78/бургертрах"));
        }

        if (Command.HARKACH.getCommand().equals(command)) {
            ThreadComment threadComment = harkachParserService.getContent(chatId);

            execute(new SendMessage(chatId, threadComment.picture()));
            if (threadComment.comment() != null && !threadComment.comment().isEmpty()) {
                execute(new SendMessage(chatId, threadComment.comment()));
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
}
