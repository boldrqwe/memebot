package ru.boldr.memebot.speechrecognizer;

import java.util.Locale;
import java.util.Set;

import lombok.Data;

@Data
public class PartOfSpeechRecognizer {

    private static final String REGEX_PATTERN = "[а-я]+";

    private static final Set<String> noun = Set.of();

    private static final Set<String> adjectives = Set.of("ый", "ой", "ий", "цый", "ья", "ье", "ая", "ее", "яя",
            "её", "ейший", "айшиий","ный");

    private static final Set<String> verb = Set.of();

    private static final Set<String> pretexts = Set.of("в", "без", "до", "для", "за", "через", "над", "по", "из", "у",
            "около", "под", "о", "про", "на", "к", "перед", "при", "с", "между");

    public static PartOfSpeech recognize(String word) {
        for (String pretext : pretexts) {
            if (pretext.equals(word.toLowerCase(Locale.ROOT))) {
                return PartOfSpeech.PRETEXTS;
            }
        }

        for (String adjective : adjectives) {
            String s = REGEX_PATTERN + adjective;
            if (s.equals(word.toLowerCase(Locale.ROOT))) {
                return PartOfSpeech.ADJECTIVE;
            }
        }
        return PartOfSpeech.NOUN;
    }
}
