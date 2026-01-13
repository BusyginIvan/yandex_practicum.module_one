package ru.yandex.practicum.domain;

import java.util.List;

public record Post(
    long id,
    String title,
    String text,
    int likesCount,
    int commentsCount,
    List<String> tags
) { }
