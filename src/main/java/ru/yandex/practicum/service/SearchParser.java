package ru.yandex.practicum.service;

import java.util.ArrayList;
import java.util.List;

public final class SearchParser {

    private SearchParser() {}

    public static SearchQuery parse(String raw) {
        if (raw == null) raw = "";

        String[] words = raw.split("\\s+");

        List<String> tags = new ArrayList<>();
        List<String> titleWords = new ArrayList<>();

        for (String w : words) {
            if (w.isEmpty()) continue;
            if (w.startsWith("#")) {
                if (w.length() == 1) continue;
                tags.add(w.substring(1));
            } else {
                titleWords.add(w);
            }
        }

        String titleSubstring = String.join(" ", titleWords);
        List<String> normalizedTags = tags.stream()
                .map(String::toLowerCase)
                .distinct()
                .sorted()
                .toList();

        return new SearchQuery(titleSubstring, normalizedTags);
    }

    public record SearchQuery(String titleSubstring, List<String> tags) {}
}

