package ru.yandex.practicum.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.domain.ImagePayload;
import ru.yandex.practicum.domain.Post;
import ru.yandex.practicum.domain.PostPage;
import ru.yandex.practicum.entity.posts.PostEntity;
import ru.yandex.practicum.entity.posts.PostEntityMapper;
import ru.yandex.practicum.exception.ImageRequiredException;
import ru.yandex.practicum.exception.InvalidImageContentTypeException;
import ru.yandex.practicum.exception.PostNotFoundException;
import ru.yandex.practicum.repository.comments.CommentRepository;
import ru.yandex.practicum.repository.posts.PostRepository;
import ru.yandex.practicum.storage.PostImageStorage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostEntityMapper postEntityMapper;
    private final PostImageStorage imageStorage;

    private final ImagePayload defaultImagePayload;

    public PostService(
            PostRepository postRepository,
            CommentRepository commentRepository,
            PostEntityMapper postEntityMapper,
            PostImageStorage imageStorage
    ) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.postEntityMapper = postEntityMapper;
        this.imageStorage = imageStorage;
        this.defaultImagePayload = loadDefaultImage();
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
        imageStorage.delete(id);
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

    public int incrementLikes(long id) {
        return postRepository.incrementLikes(id);
    }

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
