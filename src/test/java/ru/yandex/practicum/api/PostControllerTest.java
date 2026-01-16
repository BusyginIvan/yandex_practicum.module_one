package ru.yandex.practicum.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ru.yandex.practicum.api.posts.dto.PostCreateRequest;
import ru.yandex.practicum.api.posts.dto.PostUpdateRequest;
import ru.yandex.practicum.domain.Post;
import ru.yandex.practicum.domain.PostPage;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PostControllerTest extends AbstractApiTest {

    // ========================= GET /api/posts =========================

    @Test
    void getPosts() throws Exception {
        Post p1 = new Post(1L, "title1", "text1", 5, 1, List.of("tag"));
        Post p2 = new Post(2L, "title2", "text2", 0, 0, List.of());
        PostPage page = new PostPage(List.of(p1, p2), 2, 5, 3);

        when(postService.search("title", 2, 5)).thenReturn(page);

        mvc.perform(get("/api/posts")
                .param("search", "title")
                .param("pageNumber", "2")
                .param("pageSize", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.posts", hasSize(2)))
            .andExpect(jsonPath("$.hasPrev", is(true)))
            .andExpect(jsonPath("$.hasNext", is(true)))
            .andExpect(jsonPath("$.lastPage", is(3)))

            .andExpect(jsonPath("$.posts[0].id", is(1)))
            .andExpect(jsonPath("$.posts[0].title", is("title1")))
            .andExpect(jsonPath("$.posts[0].text", is("text1")))
            .andExpect(jsonPath("$.posts[0].tags[0]", is("tag")))
            .andExpect(jsonPath("$.posts[0].likesCount", is(5)))
            .andExpect(jsonPath("$.posts[0].commentsCount", is(1)))

            .andExpect(jsonPath("$.posts[1].id", is(2)))
            .andExpect(jsonPath("$.posts[1].tags", hasSize(0)));
    }

    // ========================= GET /api/posts/{id} =========================

    @Test
    void getPost() throws Exception {
        Post post = new Post(
            1L,
            "title",
            "txt",
            7,
            3,
            List.of("a", "b")
        );
        when(postService.getPost(1L)).thenReturn(post);

        mvc.perform(get("/api/posts/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.title", is("title")))
            .andExpect(jsonPath("$.text", is("txt")))
            .andExpect(jsonPath("$.tags", hasSize(2)))
            .andExpect(jsonPath("$.tags[0]", is("a")))
            .andExpect(jsonPath("$.tags[1]", is("b")))
            .andExpect(jsonPath("$.likesCount", is(7)))
            .andExpect(jsonPath("$.commentsCount", is(3)));
    }

    // ========================= POST /api/posts =========================

    @Test
    void createPost() throws Exception {
        PostCreateRequest req = new PostCreateRequest("new", "txt", List.of("a", "b"));

        Post created = new Post(10L, "new", "txt", 0, 0, List.of("a", "b"));
        when(postService.create("new", "txt", List.of("a", "b"))).thenReturn(created);

        mvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(10)))
            .andExpect(jsonPath("$.title", is("new")))
            .andExpect(jsonPath("$.text", is("txt")))
            .andExpect(jsonPath("$.tags", hasSize(2)))
            .andExpect(jsonPath("$.likesCount", is(0)))
            .andExpect(jsonPath("$.commentsCount", is(0)));
    }

    // ========================= PUT /api/posts/{id} =========================

    @Test
    void updatePost() throws Exception {
        PostUpdateRequest req = new PostUpdateRequest(5L, "upd", "txt", List.of("tag"));

        Post updated = new Post(5L, "upd", "txt", 2, 1, List.of("tag"));
        when(postService.update(5L, "upd", "txt", List.of("tag"))).thenReturn(updated);

        mvc.perform(put("/api/posts/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(5)))
            .andExpect(jsonPath("$.title", is("upd")))
            .andExpect(jsonPath("$.text", is("txt")))
            .andExpect(jsonPath("$.tags", hasSize(1)))
            .andExpect(jsonPath("$.tags[0]", is("tag")))
            .andExpect(jsonPath("$.likesCount", is(2)))
            .andExpect(jsonPath("$.commentsCount", is(1)));
    }

    @Test
    void updatePost_idMismatch() throws Exception {
        PostUpdateRequest req = new PostUpdateRequest(6L, "upd", "body", List.of("tag"));

        mvc.perform(put("/api/posts/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.name", is("IdMismatchException")));
    }

    // ========================= DELETE /api/posts/{id} =========================

    @Test
    void deletePost() throws Exception {
        doNothing().when(postService).delete(9L);

        mvc.perform(delete("/api/posts/9"))
            .andExpect(status().isOk());
    }

    // ========================= validation (smoke tests) =========================

    @Test
    void getPosts_invalidPageNumber() throws Exception {
        mvc.perform(get("/api/posts")
                .param("search", "x")
                .param("pageNumber", "0")
                .param("pageSize", "5"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.name", is("ConstraintViolationException")));
    }

    @Test
    void createPost_invalidTitle() throws Exception {
        String invalidJson = """
            {"title":null,"text":"txt","tags":[]}
            """;

        mvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.name", is("MethodArgumentNotValidException")));
    }
}
