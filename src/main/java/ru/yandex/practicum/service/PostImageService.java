package ru.yandex.practicum.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.domain.ImagePayload;
import ru.yandex.practicum.exception.validation.ImageRequiredException;
import ru.yandex.practicum.exception.validation.InvalidImageContentTypeException;
import ru.yandex.practicum.repository.posts.PostRepository;
import ru.yandex.practicum.storage.PostImageStorage;

import java.io.IOException;
import java.util.Optional;

@Service
public class PostImageService {

    private final PostRepository postRepository;
    private final PostImageStorage imageStorage;

    private final ImagePayload defaultImagePayload;

    public PostImageService(
        PostRepository postRepository,
        PostImageStorage imageStorage
    ) {
        this.postRepository = postRepository;
        this.imageStorage = imageStorage;
        this.defaultImagePayload = loadDefaultImage();
    }

    public ImagePayload getOrDefault(long postId) {
        Optional<String> contentType = postRepository.findImageContentType(postId);

        if (contentType.isEmpty() || !imageStorage.exists(postId)) {
            return defaultImagePayload;
        }

        byte[] bytes = imageStorage.read(postId);
        return new ImagePayload(contentType.get(), bytes);
    }

    @Transactional
    public void update(long postId, MultipartFile image) {
        if (image == null || image.isEmpty()) throw new ImageRequiredException();

        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidImageContentTypeException(contentType);
        }

        byte[] bytes = readImageBytes(image);
        postRepository.updateImageContentType(postId, contentType);
        imageStorage.save(postId, bytes);
    }

    public void delete(long postId) {
        imageStorage.delete(postId);
    }

    private byte[] readImageBytes(MultipartFile image) {
        try {
            return image.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded image bytes", e);
        }
    }

    private ImagePayload loadDefaultImage() {
        try (var in = new ClassPathResource("default-post-image.svg").getInputStream()) {
            return new ImagePayload("image/svg+xml", in.readAllBytes());
        } catch (IOException e) {
            throw new IllegalStateException("default-post-image.svg not found in classpath", e);
        }
    }
}
