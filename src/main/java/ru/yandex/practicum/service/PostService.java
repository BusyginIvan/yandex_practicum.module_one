package ru.yandex.practicum.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.domain.Post;
import ru.yandex.practicum.domain.PostPage;
import ru.yandex.practicum.entity.posts.PostEntity;
import ru.yandex.practicum.exception.not_found.PostNotFoundException;
import ru.yandex.practicum.repository.posts.PostRepository;
import ru.yandex.practicum.repository.tags.TagRepository;
import ru.yandex.practicum.service.SearchParser.SearchQuery;

import java.util.List;
import java.util.function.Supplier;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final PostImageService postImageService;
    private final PostAssembler postAssembler;

    public PostService(
        PostRepository postRepository,
        TagRepository tagRepository,
        PostImageService postImageService,
        PostAssembler postAssembler
    ) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.postImageService = postImageService;
        this.postAssembler = postAssembler;
    }

    public Post getPost(long id) {
        return getPost(id, () -> new PostNotFoundException(id));
    }

    private Post getAfterWrite(long id) {
        return getPost(id, () -> new IllegalStateException(
            "Post disappeared after write: id=" + id
        ));
    }

    private Post getPost(long id, Supplier<? extends RuntimeException> onMissing) {
        PostEntity postEntity = postRepository.findById(id).orElseThrow(onMissing);
        return postAssembler.toPost(postEntity);
    }

    @Transactional
    public Post create(String title, String text, List<String> tags) {
        long id = postRepository.insert(title, text);
        tagRepository.replaceTags(id, tags);
        return getAfterWrite(id);
    }

    @Transactional
    public Post update(long id, String title, String text, List<String> tags) {
        postRepository.update(id, title, text);
        tagRepository.replaceTags(id, tags);
        return getAfterWrite(id);
    }

    @Transactional
    public void delete(long id) {
        postRepository.deleteById(id);
        postImageService.delete(id);
    }

    public PostPage search(String rawSearch, int pageNumber, int pageSize) {
        SearchQuery q = SearchParser.parse(rawSearch);

        long total = postRepository.countBySearch(q.titleSubstring(), q.tags());
        int lastPage = total == 0 ? 1 : (int) ((total - 1) / pageSize + 1);
        if (pageNumber > lastPage) pageNumber = lastPage;
        int offset = (pageNumber - 1) * pageSize;

        List<PostEntity> entities = postRepository.searchPage(q.titleSubstring(), q.tags(), offset, pageSize);
        List<Post> posts = postAssembler.toPosts(entities);

        return new PostPage(posts, pageNumber, pageSize, lastPage);
    }

    public int incrementLikes(long id) {
        return postRepository.incrementLikes(id);
    }
}