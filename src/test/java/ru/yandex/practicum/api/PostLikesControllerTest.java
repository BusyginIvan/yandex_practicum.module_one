package ru.yandex.practicum.api;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PostLikesControllerTest extends AbstractApiTest {

    @Test
    void incrementLikes() throws Exception {
        when(postService.incrementLikes(1L)).thenReturn(123);

        mvc.perform(post("/api/posts/1/likes"))
            .andExpect(status().isOk())
            .andExpect(content().string("123"));
    }

    @Test
    void incrementLikes_invalidId_zero() throws Exception {
        mvc.perform(post("/api/posts/0/likes"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.name", is("ConstraintViolationException")));

        verifyNoInteractions(postService);
    }

    @Test
    void incrementLikes_invalidId_negative() throws Exception {
        mvc.perform(post("/api/posts/-1/likes"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.name", is("ConstraintViolationException")));

        verifyNoInteractions(postService);
    }

    @Test
    void incrementLikes_invalidId_notANumber() throws Exception {
        mvc.perform(post("/api/posts/abc/likes"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.name", is("MethodArgumentTypeMismatchException")));

        verifyNoInteractions(postService);
    }
}
