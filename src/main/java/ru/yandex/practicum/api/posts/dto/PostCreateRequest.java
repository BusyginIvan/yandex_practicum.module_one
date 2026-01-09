package ru.yandex.practicum.api.posts.dto;

import java.util.List;

public record PostCreateRequest(
        String title,
        String text,
        List<String> tags
) {}
