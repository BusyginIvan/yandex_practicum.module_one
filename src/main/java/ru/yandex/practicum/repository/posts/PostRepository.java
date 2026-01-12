package ru.yandex.practicum.repository.posts;

import ru.yandex.practicum.domain.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository {
    long insert(String title, String text);
    Optional<Post> findById(long id);
    void update(long id, String title, String text);
    void deleteById(long id);

    List<Post> searchPage(String search, int offset, int limit);
    long countBySearch(String search);
}
