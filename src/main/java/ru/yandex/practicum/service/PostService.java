package ru.yandex.practicum.service;

import ru.yandex.practicum.domain.Post;
import ru.yandex.practicum.domain.PostPage;
import ru.yandex.practicum.exception.PostNotFoundException;
import ru.yandex.practicum.repository.posts.PostRepository;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post getById(long id) {
        return postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
    }

    public Post create(String title, String text) {
        long id = postRepository.insert(title, text);
        // TODO: сохранить теги (tags/post_tags) когда появится PostTagRepository
        return getById(id);
    }

    public Post update(long id, String title, String text) {
        postRepository.update(id, title, text);
        // TODO: обновить теги когда появится PostTagRepository
        return getById(id);
    }

    public void delete(long id) {
        // TODO: если нужно — удалять изображение с диска вместе с постом
        postRepository.deleteById(id);
    }

    public PostPage search(String search, int pageNumber, int pageSize) {
        long total = postRepository.countBySearch(search);
        int lastPage = total == 0 ? 1 : (int) ((total - 1) / pageSize + 1);

        if (pageNumber > lastPage) pageNumber = lastPage;
        int offset = (pageNumber - 1) * pageSize;
        List<Post> page = postRepository.searchPage(search, offset, pageSize);

        // TODO: подтягивать tags для списка
        // TODO: подтягивать commentsCount для списка (агрегацией по comments)

        return new PostPage(page, pageNumber, pageSize, lastPage);
    }
}
