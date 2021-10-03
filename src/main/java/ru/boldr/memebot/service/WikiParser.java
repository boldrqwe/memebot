package ru.boldr.memebot.service;


import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import ru.boldr.memebot.model.Command;

import java.io.*;

@Service
@RequiredArgsConstructor
public class WikiParser {

    private static final String URL = "https://ru.wikipedia.org/wiki/%D0%A1%D0%BB%D1%83%D0%B6%D0%B5%D0%B1%D0%BD%D0%B0%D1%8F:%D0%A1%D0%BB%D1%83%D1%87%D0%B0%D0%B9%D0%BD%D0%B0%D1%8F_%D1%81%D1%82%D1%80%D0%B0%D0%BD%D0%B8%D1%86%D0%B0";

    private final String KAKASHKYLES = "Какашкулес";
    private final String KAKASHKYLESOVICH = "Какашкулесович";

    public String getPage(String heroName) {

        Connection connect = Jsoup
                .connect(URL);


        Document document = null;
        try {
            document = connect.get();
        } catch (IOException e) {
            e.printStackTrace();
        }


        assert document != null;
        Element firstHeading = document.getElementById("firstHeading");

        String name = firstHeading.getElementById("firstHeading").text();

        String[] split = name.split(" ");

        String body = document.body().toString();


        String result = null;
        if ("какашкулес.html".equals(heroName)) {
            result = getKakashkules(name, split, body, KAKASHKYLES, KAKASHKYLESOVICH);
        }
        if ("бургертрах.html".equals(heroName)) {
            result = getBurgertrach(name, split, body, "Бургертрах", "Бургертрахович");
        }

        return result;
    }

    private String getBurgertrach(String name, String[] split, String body, String бургертрах, String бургертрахович) {
        return getResult(name, split, body, бургертрах, бургертрахович);
    }

    private String getKakashkules(String name, String[] split, String body, String kakashkyles, String kakashkylesovich) {
        return getResult(name, split, body, kakashkyles, kakashkylesovich);
    }

    private String getResult(String name,
                             String[] split,
                             String body,
                             String fistName,
                             String lastName) {
        String result = body.replaceAll(name, fistName);

        int count = 0;

        for (String st : split) {
            if (count == 1) {
                result = result.replaceAll(st, lastName);
            } else {
                result = result.replaceAll(st, fistName);
                count++;
            }
        }
        return result;
    }

    @Retryable
    public void toFile(String fileName) throws IOException {

        String str = getPage(fileName);

        FileOutputStream outputStream = new FileOutputStream(fileName);
        byte[] strToBytes = str.getBytes();
        outputStream.write(strToBytes);

        outputStream.close();


    }
}
