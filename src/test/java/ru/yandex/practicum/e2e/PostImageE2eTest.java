package ru.yandex.practicum.e2e;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import ru.yandex.practicum.api.posts.dto.PostCreateRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PostImageE2eTest extends AbstractE2eTest {

    @Test
    void updateImage_thenGetImage_returnsSameBytesAndContentType() throws Exception {
        int postId = createPost("p", "x", List.of());

        byte[] bytes = new byte[] {1, 2, 3, 4, 5};
        MockMultipartFile file = new MockMultipartFile(
            "image",
            "image.png",
            "image/png",
            bytes
        );

        mvc.perform(multipart("/api/posts/" + postId + "/image")
                .file(file)
                .with(req -> { req.setMethod("PUT"); return req; }))
            .andExpect(status().isOk());

        mvc.perform(get("/api/posts/" + postId + "/image"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "image/png"))
            .andExpect(content().bytes(bytes));
    }

    @Test
    void getImage_withoutUpload_returnsDefaultSvg() throws Exception {
        int postId = createPost("p", "x", List.of());

        byte[] body = mvc.perform(get("/api/posts/" + postId + "/image"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "image/svg+xml"))
            .andReturn()
            .getResponse()
            .getContentAsByteArray();

        assertTrue(body.length > 10);
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
