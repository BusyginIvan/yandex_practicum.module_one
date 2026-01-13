package ru.yandex.practicum.api.comments.dto;

public record CommentUpdateRequest(
    long id,
    String text,
    long postId
) { }
