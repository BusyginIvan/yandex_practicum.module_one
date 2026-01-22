package ru.yandex.practicum.entity.posts;

public record PostEntity(
    long id,
    String title,
    String text,
    int likesCount
) { }
