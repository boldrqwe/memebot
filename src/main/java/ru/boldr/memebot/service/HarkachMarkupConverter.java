package ru.boldr.memebot.service;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class HarkachMarkupConverter {

    public String convertToTgHtml(String input) {
        return replaceUnderlineSpan(input)
                .replace("<a href=\"/", "<a href=\"https://2ch.hk/")
                .replace("&quot;", "\"")
                .replace("class=\"spoiler\"", "class=\"tg-spoiler\"")
                .replace("<br>", """
                                                    
                                                    """);
    }


    public String replaceUnderlineSpan(String input) {
        var regex = Pattern.compile("<span class=\"u\">.*?</span>");
        var result = input;
        StringBuilder sb = new StringBuilder();
        var matcher = regex.matcher(input);
        while (matcher.find()) {
            var content = matcher.group();
            if (content == null || content.length() == 0) {
                continue;
            }

            var toReplace = String.copyValueOf(content.toCharArray(), 16, content.length() - 23);
            result = result.replaceFirst("<span class=\"u\">(" + toReplace + ")?</span>",
                    "<u>" + toReplace +"</u>");
        }

        if (regex.matcher(result).find()) {
            return replaceUnderlineSpan(result);
        }

        return result;
    }

    public String convertToHtml(String input) {
        return replaceUnderlineSpanHtml(input)
                .replace("<a href=\"/", "<a href=\"https://2ch.hk/")
                .replace("&quot;", "\"")
                .replace("<br>", "<br />"); // Использование самозакрывающегося тега <br />
    }

    public String replaceUnderlineSpanHtml(String input) {
        var regex = Pattern.compile("<span class=\"u\">.*?</span>");
        var result = input;
        var matcher = regex.matcher(input);
        while (matcher.find()) {
            var content = matcher.group();
            if (content == null || content.length() == 0) {
                continue;
            }

            var toReplace = String.copyValueOf(content.toCharArray(), 16, content.length() - 23);
            result = result.replaceFirst("<span class=\"u\">(" + toReplace + ")?</span>",
                    "<u>" + toReplace + "</u>");
        }

        if (regex.matcher(result).find()) {
            return replaceUnderlineSpan(result);
        }

        return result;
    }
}
