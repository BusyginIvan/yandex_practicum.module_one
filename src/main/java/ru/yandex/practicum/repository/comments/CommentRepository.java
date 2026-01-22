package ru.yandex.practicum.repository.comments;

import ru.yandex.practicum.domain.Comment;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CommentRepository {
    List<Comment> findByPostId(long postId);
    Optional<Comment> findByPostIdAndId(long postId, long commentId);

    long insert(long postId, String text);
    void update(long postId, long commentId, String text);
    void delete(long postId, long commentId);

    int countByPostId(long postId);
    Map<Long, Integer> countByPostIds(List<Long> postIds);
}
