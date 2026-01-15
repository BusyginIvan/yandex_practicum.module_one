package ru.yandex.practicum.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.domain.ImagePayload;
import ru.yandex.practicum.domain.Post;
import ru.yandex.practicum.domain.PostPage;
import ru.yandex.practicum.entity.posts.PostEntity;
import ru.yandex.practicum.entity.posts.PostEntityMapper;
import ru.yandex.practicum.exception.not_found.PostNotFoundException;
import ru.yandex.practicum.exception.validation.ImageRequiredException;
import ru.yandex.practicum.exception.validation.InvalidImageContentTypeException;
import ru.yandex.practicum.repository.comments.CommentRepository;
import ru.yandex.practicum.repository.posts.PostRepository;
import ru.yandex.practicum.repository.tags.TagRepository;
import ru.yandex.practicum.service.SearchParser.SearchQuery;
import ru.yandex.practicum.storage.PostImageStorage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final TagRepository tagRepository;
    private final PostEntityMapper postEntityMapper;
    private final PostImageStorage imageStorage;

    private final ImagePayload defaultImagePayload;

    public PostService(
            PostRepository postRepository,
            CommentRepository commentRepository,
            TagRepository tagRepository,
            PostEntityMapper postEntityMapper,
            PostImageStorage imageStorage
    ) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.tagRepository = tagRepository;
        this.postEntityMapper = postEntityMapper;
        this.imageStorage = imageStorage;
        this.defaultImagePayload = loadDefaultImage();
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
        int commentsCount = commentRepository.countByPostId(id);
        List<String> tags = tagRepository.findTagsByPostId(id);
        return postEntityMapper.toPost(postEntity, commentsCount, tags);
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
        imageStorage.delete(id);
    }

    public PostPage search(String rawSearch, int pageNumber, int pageSize) {
        SearchQuery q = SearchParser.parse(rawSearch);

        long total = postRepository.countBySearch(q.titleSubstring(), q.tags());
        int lastPage = total == 0 ? 1 : (int) ((total - 1) / pageSize + 1);
        if (pageNumber > lastPage) pageNumber = lastPage;
        int offset = (pageNumber - 1) * pageSize;

        List<PostEntity> entities = postRepository.searchPage(q.titleSubstring(), q.tags(), offset, pageSize);
        List<Long> ids = entities.stream().map(PostEntity::id).toList();

        Map<Long, Integer> commentCounts = commentRepository.countByPostIds(ids);
        Map<Long, List<String>> tagsByPost = tagRepository.findTagsByPostIds(ids);

        List<Post> posts = entities.stream()
            .map(e -> postEntityMapper.toPost(
                e,
                commentCounts.getOrDefault(e.id(), 0),
                tagsByPost.getOrDefault(e.id(), List.of())
            ))
            .toList();

        return new PostPage(posts, pageNumber, pageSize, lastPage);
    }

    public int incrementLikes(long id) {
        return postRepository.incrementLikes(id);
    }

    @Transactional
    public void updatePostImage(long postId, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ImageRequiredException();
        }

        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidImageContentTypeException(contentType);
        }

        postRepository.updateImageContentType(postId, contentType);

        try {
            imageStorage.save(postId, image.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded image bytes for post " + postId, e);
        }
    }

    public ImagePayload getPostImageOrDefault(long postId) {
        Optional<String> contentType = postRepository.findImageContentType(postId);

        if (contentType.isEmpty() || !imageStorage.exists(postId)) {
            return defaultImagePayload;
        }

        byte[] bytes = imageStorage.read(postId);
        return new ImagePayload(contentType.get(), bytes);
    }

    private ImagePayload loadDefaultImage() {
        try (var in = new ClassPathResource("default-post-image.svg").getInputStream()) {
            return new ImagePayload("image/svg+xml", in.readAllBytes());
        } catch (IOException e) {
            throw new IllegalStateException("default-post-image.svg not found in classpath", e);
        }
    }
}
