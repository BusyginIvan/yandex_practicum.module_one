package ru.yandex.practicum.api.posts.dto;

import java.util.List;

public record PostUpdateRequest(
        long id,
        String title,
        String text,
        List<String> tags
) {}
