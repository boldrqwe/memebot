package ru.boldr.memebot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.boldr.memebot.service.HarkachMarkupConverter;

@SpringBootTest
public class HarkachMarkupConverterTest {

    @Autowired
    private HarkachMarkupConverter harkachMarkupConverter;

    @Test
    @DisplayName("Конвертация <span class=\"u\"></span> в <u></u>")
    public void shouldConvertUnderlinedSpan() {
        var result = harkachMarkupConverter.convertToTgHtml("<span class=\"u\"><span class=\"u\">Высшая " +
                "школа</span><span class=\"u\">Высшая школа</span>Высшая школа</span>");

        Assertions.assertEquals("<u><u>Высшая школа</u><u>Высшая школа</u>Высшая школа</u>", result);
    }

    @Test
    @DisplayName("Конвертация <span class=\"spoiler\"></span> в <span class=\"tg-spoiler\"></span>")
    public void shouldConvertSpoilerSpan() {
        var result = harkachMarkupConverter.convertToTgHtml("тикток тред с красивыми милыми девушками, которые " +
                "классно пританцовывают<br><span class=\"spoiler\">жаль только саки мальчик уже</span>");

        Assertions.assertEquals("тикток тред с красивыми милыми девушками, которые классно пританцовывают\n<span " +
                "class=\"tg-spoiler\">жаль только саки мальчик уже</span>", result);
    }
}
