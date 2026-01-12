package ru.yandex.practicum.domain;

import java.util.List;

public record PostPage(
        List<Post> posts,
        int pageNumber,
        int pageSize,
        int lastPage
) {
    public boolean hasPrev() { return pageNumber > 1; }
    public boolean hasNext() { return pageNumber < lastPage; }
}
