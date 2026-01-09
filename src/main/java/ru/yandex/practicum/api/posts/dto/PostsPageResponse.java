package ru.yandex.practicum.api.posts.dto;

import java.util.List;

public record PostsPageResponse(
        List<PostDto> posts,
        boolean hasPrev,
        boolean hasNext,
        int lastPage
) {}
