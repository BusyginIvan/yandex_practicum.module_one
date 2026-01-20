package ru.yandex.practicum.e2e;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ru.yandex.practicum.api.posts.dto.PostCreateRequest;
import ru.yandex.practicum.api.posts.dto.PostUpdateRequest;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PostE2eTest extends AbstractE2eTest {

    // ========================= POST -> GET -> PUT -> GET =========================

    @Test
    void createGetUpdateGet() throws Exception {
        // create
        PostCreateRequest create = new PostCreateRequest(
            "Hello World",
            "Body",
            List.of("  Java  ", "spring", "JAVA")
        );

        String createJson = mvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(create)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.title", is("Hello World")))
            .andExpect(jsonPath("$.text", is("Body")))
            .andExpect(jsonPath("$.likesCount", is(0)))
            .andExpect(jsonPath("$.commentsCount", is(0)))
            .andExpect(jsonPath("$.tags", hasSize(2)))
            .andExpect(jsonPath("$.tags[0]", is("java")))
            .andExpect(jsonPath("$.tags[1]", is("spring")))
            .andReturn()
            .getResponse()
            .getContentAsString();

        int id = JsonPath.read(createJson, "$.id");

        // get
        mvc.perform(get("/api/posts/" + id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(id)))
            .andExpect(jsonPath("$.title", is("Hello World")))
            .andExpect(jsonPath("$.text", is("Body")))
            .andExpect(jsonPath("$.likesCount", is(0)))
            .andExpect(jsonPath("$.commentsCount", is(0)))
            .andExpect(jsonPath("$.tags", hasSize(2)))
            .andExpect(jsonPath("$.tags[0]", is("java")))
            .andExpect(jsonPath("$.tags[1]", is("spring")));

        // update
        PostUpdateRequest update = new PostUpdateRequest(
            id,
            "Updated",
            "Updated body",
            List.of("db", "SPRING", "  db  ")
        );

        mvc.perform(put("/api/posts/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(id)))
            .andExpect(jsonPath("$.title", is("Updated")))
            .andExpect(jsonPath("$.text", is("Updated body")))
            .andExpect(jsonPath("$.tags", hasSize(2)))
            .andExpect(jsonPath("$.tags[0]", is("db")))
            .andExpect(jsonPath("$.tags[1]", is("spring")));

        // get again
        mvc.perform(get("/api/posts/" + id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(id)))
            .andExpect(jsonPath("$.title", is("Updated")))
            .andExpect(jsonPath("$.text", is("Updated body")))
            .andExpect(jsonPath("$.likesCount", is(0)))
            .andExpect(jsonPath("$.commentsCount", is(0)))
            .andExpect(jsonPath("$.tags", hasSize(2)))
            .andExpect(jsonPath("$.tags[0]", is("db")))
            .andExpect(jsonPath("$.tags[1]", is("spring")));
    }

    // ========================= GET /api/posts (search + tags + paging) =========================

    @Test
    void searchByTitleAndTags_andPagination() throws Exception {
        int p1 = createPost("Hello Java", "x", List.of("java", "spring"));
        int p2 = createPost("Hello Spring", "x", List.of("spring"));
        int p3 = createPost("Other Java", "x", List.of("java"));

        // title
        mvc.perform(get("/api/posts")
                .param("search", "heLLo")
                .param("pageNumber", "1")
                .param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.posts", hasSize(2)))
            .andExpect(jsonPath("$.posts[0].id", is(Math.max(p1, p2))))
            .andExpect(jsonPath("$.posts[1].id", is(Math.min(p1, p2))))
            .andExpect(jsonPath("$.hasPrev", is(false)))
            .andExpect(jsonPath("$.hasNext", is(false)))
            .andExpect(jsonPath("$.lastPage", is(1)));

        // title + tags
        mvc.perform(get("/api/posts")
                .param("search", "hello #java #spring")
                .param("pageNumber", "1")
                .param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.posts", hasSize(1)))
            .andExpect(jsonPath("$.posts[0].id", is(p1)))
            .andExpect(jsonPath("$.hasPrev", is(false)))
            .andExpect(jsonPath("$.hasNext", is(false)))
            .andExpect(jsonPath("$.lastPage", is(1)));

        // without filters
        mvc.perform(get("/api/posts")
                .param("search", "")
                .param("pageNumber", "1")
                .param("pageSize", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.posts", hasSize(2)))
            .andExpect(jsonPath("$.posts[0].id", is(p3)))
            .andExpect(jsonPath("$.hasPrev", is(false)))
            .andExpect(jsonPath("$.hasNext", is(true)))
            .andExpect(jsonPath("$.lastPage", is(2)));

        mvc.perform(get("/api/posts")
                .param("search", "")
                .param("pageNumber", "2")
                .param("pageSize", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.posts", hasSize(1)))
            .andExpect(jsonPath("$.hasPrev", is(true)))
            .andExpect(jsonPath("$.hasNext", is(false)))
            .andExpect(jsonPath("$.lastPage", is(2)));
    }

    // ========================= helpers =========================

    private int createPost(String title, String text, List<String> tags) throws Exception {
        PostCreateRequest req = new PostCreateRequest(title, text, tags);

        String json = mvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return JsonPath.read(json, "$.id");
    }
}
