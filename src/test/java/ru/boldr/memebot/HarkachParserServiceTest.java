package ru.boldr.memebot;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import ru.boldr.memebot.annotation.DbUnitDataSet;
import ru.boldr.memebot.model.entity.CoolFile;
import ru.boldr.memebot.model.entity.HarKachFileHistory;
import ru.boldr.memebot.repository.CoolFileRepo;
import ru.boldr.memebot.service.HarkachParserService;
import ru.boldr.memebot.service.ThreadComment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = TestConfig.class)
class HarkachParserServiceTest {

    @Mock
    private CoolFileRepo coolFileRepo;

    @InjectMocks
    private HarkachParserService harkachParserService;

    private List<CoolFile> coolFiles;
    private List<HarKachFileHistory> fileHistory;

    @BeforeEach
    void setUp() {
        coolFiles = Arrays.asList(
                CoolFile.builder()
                        .fileName("/file2")
                        .message("message2")
                        .build()
        );

        fileHistory = Arrays.asList(
                HarKachFileHistory.builder()
                        .chatId("chatId")
                        .fileName("file1")
                        .build()
        );
    }

    @Test
    @DbUnitDataSet(before = "src/test/resources/dataSets/before.csv", after = "src/test/resources/dataSets/after.csv")
    void getContent() {
        String chatId = "chatId";

        when(coolFileRepo.findAll()).thenReturn(coolFiles);
        ThreadComment result = harkachParserService.getContent(chatId);

        verify(coolFileRepo, times(1)).findAll();

        assertEquals("https://2ch.hk/file2", result.picture());
        assertEquals("message2", result.comment());
    }
}
