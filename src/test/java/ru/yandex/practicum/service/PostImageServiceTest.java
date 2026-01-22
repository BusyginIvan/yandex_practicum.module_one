package ru.yandex.practicum.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import ru.yandex.practicum.domain.ImagePayload;
import ru.yandex.practicum.exception.validation.ImageRequiredException;
import ru.yandex.practicum.exception.validation.InvalidImageContentTypeException;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class PostImageServiceTest extends AbstractServiceTest {
    
    @Autowired PostImageService service;

    // ========================= update =========================

    @Test
    void update() throws Exception {
        byte[] bytes = "img".getBytes(StandardCharsets.UTF_8);
        var file = new MockMultipartFile("image", "a.png", "image/png", bytes);

        service.update(10L, file);

        verify(postRepository).updateImageContentType(10L, "image/png");
        verify(postImageStorage).save(10L, bytes);
        verifyNoInteractions(commentRepository, tagRepository);
    }

    @Test
    void update_nullImage() {
        assertThrows(ImageRequiredException.class, () -> service.update(1L, null));
        verifyNoInteractions(postRepository, commentRepository, tagRepository, postImageStorage);
    }

    @Test
    void update_emptyImage() {
        var file = new MockMultipartFile("image", "a.png", "image/png", new byte[0]);

        assertThrows(ImageRequiredException.class, () -> service.update(1L, file));

        verifyNoInteractions(postRepository, commentRepository, tagRepository, postImageStorage);
    }

    @Test
    void update_nonImageContentType() {
        var file = new MockMultipartFile("image", "a.txt", "text/plain", "x".getBytes(StandardCharsets.UTF_8));

        assertThrows(InvalidImageContentTypeException.class, () -> service.update(1L, file));

        verifyNoInteractions(postRepository, commentRepository, tagRepository, postImageStorage);
    }

    // ========================= getOrDefault =========================

    @Test
    void getOrDefault_returnsStoredImage() {
        byte[] bytes = new byte[] {1, 2, 3};

        when(postRepository.findImageContentType(3L)).thenReturn(Optional.of("image/png"));
        when(postImageStorage.exists(3L)).thenReturn(true);
        when(postImageStorage.read(3L)).thenReturn(bytes);

        ImagePayload p = service.getOrDefault(3L);

        assertEquals("image/png", p.contentType());
        assertArrayEquals(bytes, p.bytes());

        verify(postRepository).findImageContentType(3L);
        verify(postImageStorage).exists(3L);
        verify(postImageStorage).read(3L);
        verifyNoMoreInteractions(postImageStorage);
    }

    @Test
    void getOrDefault_contentTypeIsNull() {
        when(postRepository.findImageContentType(1L)).thenReturn(Optional.empty());

        ImagePayload p = service.getOrDefault(1L);

        assertEquals("image/svg+xml", p.contentType());
        assertNotNull(p.bytes());
        assertTrue(p.bytes().length > 0);

        verify(postRepository).findImageContentType(1L);
        verifyNoInteractions(postImageStorage);
    }

    @Test
    void getOrDefault_fileIsMissing() {
        when(postRepository.findImageContentType(2L)).thenReturn(Optional.of("image/png"));
        when(postImageStorage.exists(2L)).thenReturn(false);

        ImagePayload p = service.getOrDefault(2L);

        assertEquals("image/svg+xml", p.contentType());
        assertTrue(p.bytes().length > 0);

        verify(postRepository).findImageContentType(2L);
        verify(postImageStorage).exists(2L);
        verifyNoMoreInteractions(postImageStorage);
    }

    // ========================= delete =========================

    @Test
    void delete() {
        service.delete(5L);
        verify(postImageStorage).delete(5L);
    }
}
