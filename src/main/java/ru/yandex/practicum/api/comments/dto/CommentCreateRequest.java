package ru.yandex.practicum.api.comments.dto;

public record CommentCreateRequest(
        String text,
        long postId
) {}
