package ru.boldr.memebot.service;


import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class WikiParser {

    private static final String URL = "https://ru.wikipedia.org/wiki/%D0%A1%D0%BB%D1%83%D0%B6%D0%B5%D0%B1%D0%BD%D0%B0%D1%8F:%D0%A1%D0%BB%D1%83%D1%87%D0%B0%D0%B9%D0%BD%D0%B0%D1%8F_%D1%81%D1%82%D1%80%D0%B0%D0%BD%D0%B8%D1%86%D0%B0";

    private final String KAKASHKYLES = "Какашкулес";
    private final String KAKASHKYLESOVICH = "Какашкулесович";

    @Retryable
    public String getPage(String heroName) throws IOException {

        Connection.Response connect = Jsoup
                .connect(URL)
                .parser(Parser.htmlParser())
                .headers(Map.of(

                        "path", "/wiki/%D0%90%D1%80%D0%BE%D0%BD%D0%B8,_%D0%A1%D1%8D%D0%BC%D1%8E%D1%8D%D0%BB"
                        ,
                        "Referer",
                        "https://ru.wikipedia.org/wiki/%D0%90%D0%BF%D0%BE%D1%81%D1%82%D0%BE%D0%BB%D1%8C%D1%81%D0%BA%D0%B8%D0%B9_%D0%B2%D0%B8%D0%BA%D0%B0%D1%80%D0%B8%D0%B0%D1%82_%D0%AD%D1%81%D0%BC%D0%B5%D1%80%D0%B0%D0%BB%D1%8C%D0%B4%D0%B0%D1%81%D0%B0"
                        ,
                        "link", "</static/images/project-logos/ruwiki.png>;rel=preload;as=image;media=not all and (min-resolution: 1.5dppx),</static/images/project-logos/ruwiki-1.5x.png>;rel=preload;as=image;media=(min-resolution: 1.5dppx) and (max-resolution: 1.999999dppx),</static/images/project-logos/ruwiki-2x.png>;rel=preload;as=image;media=(min-resolution: 2dppx)"
                        ,
                        "referer", "https://ru.wikipedia.org/wiki/%D0%90%D0%BF%D0%BE%D1%81%D1%82%D0%BE%D0%BB%D1%8C%D1%81%D0%BA%D0%B8%D0%B9_%D0%B2%D0%B8%D0%BA%D0%B0%D1%80%D0%B8%D0%B0%D1%82_%D0%AD%D1%81%D0%BC%D0%B5%D1%80%D0%B0%D0%BB%D1%8C%D0%B4%D0%B0%D1%81%D0%B0"
                        ,
                        "accept-encoding", " gzip, deflate, br",
                        "authority", "ru.wikipedia.org",
                        "accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"

                ))
                .cookie("cookie:", "WMF-Last-Access=09-Oct-2021; WMF-Last-Access-Global=09-Oct-2021; GeoIP=CA:BC:Vancouver:49.28:-123.13:v4; ruwikimwuser-sessionId=b84df8c081b7053d7e0f; ruwikiel-sessionId=a4a7bee0b4fb52b6ff2f; ruwikiwmE-sessionTickLastTickTime=1633817536177; ruwikiwmE-sessionTickTickCount=45")
                .maxBodySize(500000)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.71 Safari/537.36")
                .execute();


        Document document = connect.parse();

        Element firstHeading = document.getElementById("firstHeading");

        assert firstHeading != null;
        String name = Objects.requireNonNull(firstHeading.getElementById("firstHeading")).text();

        String[] split = name.split(" ");

        String body = document.html();


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
