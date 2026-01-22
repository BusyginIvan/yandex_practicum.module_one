package ru.yandex.practicum.api.posts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostCreateRequest(
    @NotNull
    @Size(max = 200)
    String title,

    @NotNull
    @Size(max = 50_000)
    String text,

    @NotNull
    @Size(max = 20)
    List<
        @NotBlank
        @Size(max = 30)
        String
    > tags
) { }
