package ru.yandex.practicum.api.comments.dto;

public record CommentDto(
    long id,
    String text,
    long postId
) { }
