package ru.yandex.practicum.domain;

public record Comment(
        long id,
        long postId,
        String text
) {}
