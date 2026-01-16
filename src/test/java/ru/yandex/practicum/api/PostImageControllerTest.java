package ru.yandex.practicum.api;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import ru.yandex.practicum.domain.ImagePayload;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PostImageControllerTest extends AbstractApiTest {

    // ========================= PUT /api/posts/{id}/image =========================

    @Test
    void updateImage() throws Exception {
        byte[] bytes = "img".getBytes();
        MockMultipartFile file = new MockMultipartFile(
            "image",
            "image.png",
            "image/png",
            bytes
        );

        doNothing().when(postService).updatePostImage(1L, file);

        mvc.perform(multipart("/api/posts/1/image")
                .file(file)
                .with(req -> { req.setMethod("PUT"); return req; }))
            .andExpect(status().isOk());
    }

    @Test
    void updateImage_missingImageParam() throws Exception {
        mvc.perform(multipart("/api/posts/1/image")
                .with(req -> { req.setMethod("PUT"); return req; }))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(postService);
    }

    @Test
    void updateImage_invalidId_zero() throws Exception {
        MockMultipartFile file = new MockMultipartFile("image", "a.png", "image/png", "x".getBytes());

        mvc.perform(multipart("/api/posts/0/image")
                .file(file)
                .with(req -> { req.setMethod("PUT"); return req; }))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.name", is("ConstraintViolationException")));

        verifyNoInteractions(postService);
    }

    @Test
    void updateImage_invalidId_notANumber() throws Exception {
        MockMultipartFile file = new MockMultipartFile("image", "a.png", "image/png", "x".getBytes());

        mvc.perform(multipart("/api/posts/abc/image")
                .file(file)
                .with(req -> { req.setMethod("PUT"); return req; }))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.name", is("MethodArgumentTypeMismatchException")));

        verifyNoInteractions(postService);
    }

    // ========================= GET /api/posts/{id}/image =========================

    @Test
    void getImage() throws Exception {
        byte[] bytes = new byte[] {1, 2, 3};
        when(postService.getPostImageOrDefault(7L)).thenReturn(new ImagePayload("image/png", bytes));

        mvc.perform(get("/api/posts/7/image"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "image/png"))
            .andExpect(content().bytes(bytes));
    }

    @Test
    void getImage_invalidId_zero() throws Exception {
        mvc.perform(get("/api/posts/0/image"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.name", is("ConstraintViolationException")));

        verifyNoInteractions(postService);
    }

    @Test
    void getImage_invalidId_notANumber() throws Exception {
        mvc.perform(get("/api/posts/abc/image"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.name", is("MethodArgumentTypeMismatchException")));

        verifyNoInteractions(postService);
    }
}
