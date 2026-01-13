package ru.yandex.practicum.repository.tags;

import java.util.List;
import java.util.Map;

public interface TagRepository {
    Map<Long, List<String>> findTagsByPostIds(List<Long> postIds);
    List<String> findTagsByPostId(long postId);

    void replaceTags(long postId, List<String> tags);
}
