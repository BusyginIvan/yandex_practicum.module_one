package ru.yandex.practicum.e2e;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ru.yandex.practicum.api.posts.dto.PostCreateRequest;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PostLikesE2eTest extends AbstractE2eTest {

    @Test
    void incrementLikes_incrementsAndPersists() throws Exception {
        int postId = createPost("p", "x", List.of());

        mvc.perform(get("/api/posts/" + postId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.likesCount", is(0)));

        mvc.perform(post("/api/posts/" + postId + "/likes"))
            .andExpect(status().isOk())
            .andExpect(content().string("1"));

        mvc.perform(post("/api/posts/" + postId + "/likes"))
            .andExpect(status().isOk())
            .andExpect(content().string("2"));

        mvc.perform(get("/api/posts/" + postId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.likesCount", is(2)));
    }

    @Test
    void incrementLikes_missingPost_returns404() throws Exception {
        mvc.perform(post("/api/posts/999/likes"))
            .andExpect(status().isNotFound());
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
}
