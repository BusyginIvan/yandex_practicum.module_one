package ru.yandex.practicum.api.comments.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CommentUpdateRequest(
    @Positive
    long id,

    @NotNull
    @Size(max = 10_000)
    String text,

    @Positive
    long postId
) { }
