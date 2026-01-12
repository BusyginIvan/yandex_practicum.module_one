package ru.yandex.practicum.domain;

public record Post(
        long id,
        String title,
        String text,
        int likesCount,
        int commentsCount
) {}
