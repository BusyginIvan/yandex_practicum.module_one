package ru.yandex.practicum.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.domain.Post;
import ru.yandex.practicum.domain.PostPage;
import ru.yandex.practicum.entity.posts.PostEntity;
import ru.yandex.practicum.entity.posts.PostEntityMapper;
import ru.yandex.practicum.exception.PostNotFoundException;
import ru.yandex.practicum.repository.comments.CommentRepository;
import ru.yandex.practicum.repository.posts.PostRepository;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostEntityMapper postEntityMapper;

    public PostService(
            PostRepository postRepository,
            CommentRepository commentRepository,
            PostEntityMapper postEntityMapper
    ) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.postEntityMapper = postEntityMapper;
    }

    public Post getById(long id) {
        PostEntity postEntity = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        int commentsCount = commentRepository.countByPostId(id);
        return postEntityMapper.toPost(postEntity, commentsCount);
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
        List<PostEntity> postEntities = postRepository.searchPage(search, offset, pageSize);

        List<Long> postIds = postEntities.stream().map(PostEntity::id).toList();
        var commentCounts = commentRepository.countByPostIds(postIds);

        List<Post> posts = postEntities.stream()
                .map(postEntity -> postEntityMapper.toPost(
                        postEntity,
                        commentCounts.getOrDefault(postEntity.id(), 0))
                )
                .toList();

        // TODO: подтягивать tags для списка

        return new PostPage(posts, pageNumber, pageSize, lastPage);
    }
}
