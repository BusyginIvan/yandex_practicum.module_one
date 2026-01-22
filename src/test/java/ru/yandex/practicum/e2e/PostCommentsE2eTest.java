package ru.yandex.practicum.e2e;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ru.yandex.practicum.api.comments.dto.CommentCreateRequest;
import ru.yandex.practicum.api.comments.dto.CommentUpdateRequest;
import ru.yandex.practicum.api.posts.dto.PostCreateRequest;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PostCommentsE2eTest extends AbstractE2eTest {

    @Test
    void addListGetUpdateDelete() throws Exception {
        int postId = createPost("p", "x", List.of());

        int c1 = addComment(postId, "a");
        int c2 = addComment(postId, "b");

        // list
        mvc.perform(get("/api/posts/" + postId + "/comments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(c1)))
            .andExpect(jsonPath("$[0].postId", is(postId)))
            .andExpect(jsonPath("$[0].text", is("a")))
            .andExpect(jsonPath("$[1].id", is(c2)))
            .andExpect(jsonPath("$[1].postId", is(postId)))
            .andExpect(jsonPath("$[1].text", is("b")));

        // get one
        mvc.perform(get("/api/posts/" + postId + "/comments/" + c2))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(c2)))
            .andExpect(jsonPath("$.postId", is(postId)))
            .andExpect(jsonPath("$.text", is("b")));

        // update
        CommentUpdateRequest upd = new CommentUpdateRequest(c2, "updated", postId);
        mvc.perform(put("/api/posts/" + postId + "/comments/" + c2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(upd)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(c2)))
            .andExpect(jsonPath("$.postId", is(postId)))
            .andExpect(jsonPath("$.text", is("updated")));

        // verify updated via get one
        mvc.perform(get("/api/posts/" + postId + "/comments/" + c2))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(c2)))
            .andExpect(jsonPath("$.postId", is(postId)))
            .andExpect(jsonPath("$.text", is("updated")));

        // delete
        mvc.perform(delete("/api/posts/" + postId + "/comments/" + c1))
            .andExpect(status().isOk());

        // list after delete => only c2 remains
        mvc.perform(get("/api/posts/" + postId + "/comments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(c2)))
            .andExpect(jsonPath("$[0].postId", is(postId)))
            .andExpect(jsonPath("$[0].text", is("updated")));
    }

    @Test
    void addComment_andPostCommentsCountChanges() throws Exception {
        int postId = createPost("p", "x", List.of());

        mvc.perform(get("/api/posts/" + postId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.commentsCount", is(0)));

        int c1 = addComment(postId, "a");
        int c2 = addComment(postId, "b");

        mvc.perform(get("/api/posts/" + postId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.commentsCount", is(2)));

        mvc.perform(delete("/api/posts/" + postId + "/comments/" + c1))
            .andExpect(status().isOk());

        mvc.perform(get("/api/posts/" + postId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.commentsCount", is(1)));

        mvc.perform(delete("/api/posts/" + postId + "/comments/" + c2))
            .andExpect(status().isOk());

        mvc.perform(get("/api/posts/" + postId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.commentsCount", is(0)));
    }

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

    private int addComment(int postId, String text) throws Exception {
        CommentCreateRequest req = new CommentCreateRequest(text, postId);

        String json = mvc.perform(post("/api/posts/" + postId + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.postId", is(postId)))
            .andExpect(jsonPath("$.text", is(text)))
            .andReturn()
            .getResponse()
            .getContentAsString();

        return JsonPath.read(json, "$.id");
    }
}
