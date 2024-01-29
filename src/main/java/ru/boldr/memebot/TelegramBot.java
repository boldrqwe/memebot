package ru.boldr.memebot;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.boldr.memebot.handlers.UpdateHandler;
import ru.boldr.memebot.helpers.JsonHelper;
import ru.boldr.memebot.model.Command;
import ru.boldr.memebot.model.entity.HarkachModHistory;
import ru.boldr.memebot.repository.HarkachModHistoryRepo;
import ru.boldr.memebot.service.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private final static String PAGING = "%d часть из %d";

    private final static Logger logger = LoggerFactory.getLogger(TelegramBot.class);
    private final JsonHelper jsonHelper;
    private final UpdateHandler updateHandler;
    private final HarkachParserService harkachParserService;
    private final HarkachModHistoryRepo harkachModHistoryRepo;
    private final TransactionTemplate transactionTemplate;
    private final SpeakService speakService;
    private final TelegramSemaphore telegramSemaphore;
    public static final String MAN_FILE_NAME = "files/man.mp4";
    public static final String REVERSE_MAN_FILE_NAME = "files/man_reverse.mp4";
    public static final String REAL_MAN_FILE_NAME = "static/real_man.mp4";
    public static final String ROMPOMPOM_FILE_NAME = "static/rompompom.mp4";

    @Override
    public String getBotUsername() {
        return "MementosFunniestForMeBot";
    }

    @Value("${bot_token}")
    private String BOT_TOKEN;

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasCallbackQuery()) {

            String data = update.getCallbackQuery().getData();
            var chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            File file = new File("files/webmfiles/");
            File[] listFiles = file.listFiles();
            if (data.toLowerCase().contains("callback")) {

                if (listFiles != null) {
                    StreamEx.of(listFiles).forEach(File::delete);
                }

                sendAllMedia(data, chatId);
            }
            if (listFiles != null) {
                StreamEx.of(listFiles).forEach(File::delete);
            }
        }

        if (update.hasMessage() && Objects.nonNull(update.getMessage().getText())) {
            logger.info("new update: {}", jsonHelper.lineToMap(update));

            var message = update.getMessage();
            var chatId = message.getChatId().toString();
            var text = update.getMessage().getText();

            if (text.toLowerCase().contains("бот скажи")) {
                SendMessage sendMessage = speakService.makeMessage(text, update);
                execute(sendMessage);
            }

            if (!updateHandler.checkWriteMessagePermission(message)) {
                return;
            }


            if (update.getMessage().getText() == null) {
                logger.warn("massage is null");
                return;
            }

            if (text.toLowerCase(Locale.ROOT).contains(Command.MAN.getCommand())) {
                manReaction(update, chatId);
            }

            chooseAndExecuteCommand(chatId, update, text);
        }
    }

    private void manReaction(Update update, String chatId) {
        sendAnimation(new SendAnimation(chatId, new InputFile(new File(MAN_FILE_NAME))));

    }

    private void chooseAndExecuteCommand(String chatId, Update update, String text) throws TelegramApiException, IOException {
        Command command = Command.checkThisSheet(text.toLowerCase());
        switch (command) {
            case HELP -> execute(new SendMessage(chatId, Command.getCommands()));
            case MAN -> manReaction(update, chatId);
            case REAL_MAN -> sendAnimation(chatId, REAL_MAN_FILE_NAME);
            case ROMPOMPOM -> sendAnimation(chatId, ROMPOMPOM_FILE_NAME);
            case MAN_REVERSE ->
                    sendAnimation(new SendAnimation(chatId, new InputFile(new File(REVERSE_MAN_FILE_NAME))));
            case KAKASHKULES -> sendMessage(new SendMessage(chatId, "http://51.250.107.78:8082/"));
            case BURGERTRACH -> sendMessage(new SendMessage(chatId, "http://51.250.107.78:8082/"));
            case HARKACH -> {
                ThreadComment threadComment = harkachParserService.getContent(chatId);
                sendMessage(new SendMessage(chatId, threadComment.picture()));
                if (threadComment.comment() != null && !threadComment.comment().isEmpty()) {
                    sendMessage(new SendMessage(chatId, threadComment.comment()));
                }
            }
            case HARKACHBASE_UPDATE -> harkachParserService.loadContent();
            case HARKACHMOD_ON -> harkachModHistoryRepo.save(
                    HarkachModHistory.builder()
                            .chatId(update.getMessage().getChatId().toString())
                            .build()
            );
            case HARKACHMOD_OFF -> transactionTemplate.executeWithoutResult(status ->
                    harkachModHistoryRepo.deleteByChatId(update.getMessage().getChatId().toString()));
            case HUITA -> sendMessage(new SendMessage(chatId, "Не пиши хуйню, додик!"));
        }
    }

    private void sendAnimation(String chatId, String fileName) throws IOException {
        InputStream inputStream = new ClassPathResource(fileName).getInputStream();
        sendAnimation(new SendAnimation(chatId, new InputFile(inputStream, fileName)));
    }

    public void sendAllMedia(String data, String chatId) {

        var threadUrl = data.split(",")[0];
        var mediaDto = harkachParserService.getContentFromCurrentThread(threadUrl, chatId);

        int inputMediaSize = mediaDto.inputMedia().size();
        int webmSize = mediaDto.webmPaths().size();

        telegramSemaphore.executeInLock(() -> getMessageCompletableFuture(chatId, inputMediaSize, webmSize), 1);

        log.info("find %d files, webm - %d  other %d".formatted(inputMediaSize + webmSize, webmSize, inputMediaSize));

        var inputMedia = mediaDto.inputMedia();

        if (inputMediaSize == 1) {
            sendOneFile(chatId, inputMedia);
            return;
        }

        partitionAndSend(chatId, inputMedia);
//        var webmPaths = mediaDto.webmPaths();
//        log.info("start process webms");
//        var processedWebmPaths = harkachParserService.processWebm(webmPaths);
//        var fileIds = getfileIds(processedWebmPaths);
//        var webms = getWebms(fileIds);
//        log.info("start send webms");
//        partitionAndSend(chatId, webms);
    }

    private void getMessageCompletableFuture(String chatId, int inputMediaSize, int webmSize) {
        try {
            executeAsync(SendMessage.builder()
                    .chatId(chatId)
                    .text("Найдено %d файлов\n скачивание скоро начнется".formatted(inputMediaSize + webmSize))
                    .build());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private List<InputMedia> getWebms(ArrayList<String> fileIds) {
        List<InputMedia> webms = fileIds.stream().map(path -> {
            InputMedia media;
            media = new InputMediaVideo(path);
            return media;
        }).toList();
        return webms;
    }

    private ArrayList<String> getfileIds(List<String> webmPaths) {
        var fileIds = new ArrayList<String>();
        webmPaths.forEach(path -> telegramSemaphore.executeInLock(() -> {
            try {
                log.info("webm - {}", path);
                var message = this.execute(SendVideo.builder()
                        .chatId("-618520976")
                        .video(new InputFile(new File(path)))
                        .build());
                fileIds.add(message.getVideo().getFileId());
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }

        }, 1L));
        return fileIds;
    }

    private void partitionAndSend(String chatId, List<InputMedia> medias) {
        var extensionToInputMedia =
                StreamEx.of(medias).nonNull().groupingBy(m -> Files.getFileExtension(m.getMedia()));

        extensionToInputMedia.keySet().forEach(key -> {
            var inputMedias = extensionToInputMedia.get(key);
            var partition = Lists.partition(inputMedias, 6);
            var size = partition.size();
            var page = new AtomicInteger();

            for (var part : partition) {
                log.info("path {} from {}", page, size);
                part.forEach(p -> log.info(p.getMedia()));
                if (part.size() > 1) {
                    sendMediaGroup(chatId, part, page.get() + 1, size);
//                    sendFormData(chatId, part);
                } else {
                    var integer = page.get();
                    sendOneFile(chatId, part, integer + 1, size);
                }
                page.getAndIncrement();
            }
        });
    }

    @SneakyThrows
    private void sendFormData(String chatId, List<InputMedia> part) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost uploadFile = new HttpPost("https://api.telegram.org/bot" + getBotToken() + "/sendDocument");
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("chat_id", chatId);
        builder.addPart(chatId, new StringBody(chatId, ContentType.TEXT_PLAIN));

// This attaches the file to the POST:
        part.forEach(inputMedia -> {
            var media = inputMedia.getMedia();
            var url = getUrl(media);
            var inputStream = getInputStream(url);
            builder.addBinaryBody(
                    "file",
                    inputStream,
                    ContentType.APPLICATION_OCTET_STREAM,
                    media
            );
        });

        HttpEntity multipart = builder.build();
        uploadFile.setEntity(multipart);
        CloseableHttpResponse response = httpClient.execute(uploadFile);
        HttpEntity responseEntity = response.getEntity();
        System.out.println("");
    }

    private static InputStream getInputStream(URL url) {
        InputStream inputStream;
        try {
            inputStream = url.openStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return inputStream;
    }

    private URL getUrl(@NonNull String inputMedia) {
        try {
            return new URL(inputMedia);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendOneFile(String chatId, ArrayList<InputMedia> inputMedias) {
        sendOneFile(chatId, inputMedias, null, null);
    }

    private void sendOneFile(String chatId, List<InputMedia> inputMedias, Integer page, Integer size) {
        InputMedia inputMedia = inputMedias.stream().findFirst().orElse(null);
        InputStream newMediaStream = inputMedia.getNewMediaStream();
        String type = inputMedia.getType();
        switch (type) {
            case ("photo") -> {
                SendPhoto file = SendPhoto.builder()
                        .chatId(chatId)
                        .photo(new InputFile(newMediaStream, "file"))
                        .parseMode(ParseMode.HTML)
                        .build();

                if (page != null) {
                    file.setCaption(PAGING.formatted(page, size));
                }

                telegramSemaphore.executeInLock(() -> executeAsync(file), 1L);
            }

            case ("video") -> {
                SendVideo file = SendVideo.builder()
                        .chatId(chatId)
                        .video(new InputFile(newMediaStream, "file"))
                        .parseMode(ParseMode.HTML)
                        .caption(PAGING.formatted(page, size))
                        .build();

                if (page != null) {
                    file.setCaption(PAGING.formatted(page, size));
                }

                telegramSemaphore.executeInLock(() -> executeAsync(file), 1L);
            }

            default -> throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    private void sendMediaGroup(String chatId, List<InputMedia> part, int page, int size) {
        part.forEach(p -> p.setCaption(PAGING.formatted(page, size)));

        telegramSemaphore.executeInLock(() -> {
            try {
                execute(SendMediaGroup.builder()
                        .chatId(chatId)
                        .medias(part)
                        .build());
            } catch (TelegramApiException ex) {
                ex.printStackTrace();
            }
        }, part.size());
    }

    public void sendMessage(SendMessage message) {
        telegramSemaphore.executeInLock(() -> {
            try {
                execute(message);
            } catch (TelegramApiException ex) {
                ex.printStackTrace();
            }
        }, 1L);
    }

    public void sendAnimation(SendAnimation message) {
        telegramSemaphore.executeInLock(() -> {
            try {
                execute(message);
            } catch (TelegramApiException ex) {
                ex.printStackTrace();
            }
        }, 1L);
    }

    @Deprecated
    private void execute(Update update, String chatId, String command) throws TelegramApiException {
        if (command.toLowerCase(Locale.ROOT).contains(Command.MAN.getCommand())) {
            sendAnimation(new SendAnimation(chatId, new InputFile(new File("files/man.mp4"))));
        }

        if (Command.MAN_REVERSE.getCommand().equals(command)) {
            sendAnimation(new SendAnimation(chatId, new InputFile(new File(REVERSE_MAN_FILE_NAME))));
        }

        if (Command.KAKASHKULES.getCommand().equals(command)) {
            sendMessage(new SendMessage(chatId, "http://51.250.107.78:8082/"));
        }

        if (Command.BURGERTRACH.getCommand().equals(command)) {
            sendMessage(new SendMessage(chatId, "http://51.250.107.78:8082/"));
        }

        if (Command.HARKACH.getCommand().equals(command)) {
            ThreadComment threadComment = harkachParserService.getContent(chatId);
            sendMessage(new SendMessage(chatId, threadComment.picture()));
            if (threadComment.comment() != null && !threadComment.comment().isEmpty()) {
                sendMessage(new SendMessage(chatId, threadComment.comment()));
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
