package ru.yandex.practicum.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ru.yandex.practicum.api.comments.dto.CommentCreateRequest;
import ru.yandex.practicum.api.comments.dto.CommentUpdateRequest;
import ru.yandex.practicum.domain.Comment;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PostCommentsControllerTest extends AbstractApiTest {

    // ========================= GET /api/posts/{postId}/comments =========================

    @Test
    void getComments() throws Exception {
        long postId = 1L;

        List<Comment> comments = List.of(
            new Comment(10L, postId, "a"),
            new Comment(11L, postId, "b")
        );

        when(commentService.getComments(postId)).thenReturn(comments);

        mvc.perform(get("/api/posts/1/comments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))

            .andExpect(jsonPath("$[0].id", is(10)))
            .andExpect(jsonPath("$[0].text", is("a")))
            .andExpect(jsonPath("$[0].postId", is(1)))

            .andExpect(jsonPath("$[1].id", is(11)))
            .andExpect(jsonPath("$[1].text", is("b")))
            .andExpect(jsonPath("$[1].postId", is(1)));
    }

    @Test
    void getComments_postIdIsUndefined() throws Exception {
        mvc.perform(get("/api/posts/undefined/comments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));

        verifyNoInteractions(commentService);
    }

    @Test
    void getComments_postIdIsNotNumber() throws Exception {
        mvc.perform(get("/api/posts/abc/comments"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.name", is("ValidationException")));

        verifyNoInteractions(commentService);
    }

    @Test
    void getComments_postIdIsZero() throws Exception {
        mvc.perform(get("/api/posts/0/comments"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.name", is("ValidationException")));

        verifyNoInteractions(commentService);
    }

    // ========================= GET /api/posts/{postId}/comments/{commentId} =========================

    @Test
    void getComment() throws Exception {
        long postId = 1L;
        long commentId = 2L;

        Comment comment = new Comment(commentId, postId, "text");
        when(commentService.getComment(postId, commentId)).thenReturn(comment);

        mvc.perform(get("/api/posts/1/comments/2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(2)))
            .andExpect(jsonPath("$.text", is("text")))
            .andExpect(jsonPath("$.postId", is(1)));
    }

    @Test
    void getComment_invalidPostId() throws Exception {
        mvc.perform(get("/api/posts/0/comments/2"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.name", is("ConstraintViolationException")));
    }

    @Test
    void getComment_invalidCommentId() throws Exception {
        mvc.perform(get("/api/posts/1/comments/0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.name", is("ConstraintViolationException")));
    }

    // ========================= POST /api/posts/{postId}/comments =========================

    @Test
    void addComment() throws Exception {
        long postId = 1L;

        CommentCreateRequest req = new CommentCreateRequest("hello", postId);

        Comment created = new Comment(10L, postId, "hello");
        when(commentService.addComment(postId, "hello")).thenReturn(created);

        mvc.perform(post("/api/posts/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(10)))
            .andExpect(jsonPath("$.text", is("hello")))
            .andExpect(jsonPath("$.postId", is(1)));
    }

    @Test
    void addComment_invalidText() throws Exception {
        String invalidJson = """
            {"text":null,"postId":1}
            """;

        mvc.perform(post("/api/posts/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.name", is("MethodArgumentNotValidException")));
    }

    // ========================= PUT /api/posts/{postId}/comments/{commentId} =========================

    @Test
    void updateComment() throws Exception {
        long postId = 1L;
        long commentId = 2L;

        CommentUpdateRequest req = new CommentUpdateRequest(commentId, "updated", postId);

        Comment updated = new Comment(commentId, postId, "updated");
        when(commentService.updateComment(postId, commentId, "updated")).thenReturn(updated);

        mvc.perform(put("/api/posts/1/comments/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(2)))
            .andExpect(jsonPath("$.text", is("updated")))
            .andExpect(jsonPath("$.postId", is(1)));
    }

    @Test
    void updateComment_postIdMismatch() throws Exception {
        CommentUpdateRequest req = new CommentUpdateRequest(2L, "updated", 999L);

        mvc.perform(put("/api/posts/1/comments/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.name", is("IdMismatchException")));
    }

    @Test
    void updateComment_commentIdMismatch() throws Exception {
        CommentUpdateRequest req = new CommentUpdateRequest(999L, "updated", 1L);

        mvc.perform(put("/api/posts/1/comments/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.name", is("IdMismatchException")));
    }

    // ========================= DELETE /api/posts/{postId}/comments/{commentId} =========================

    @Test
    void deleteComment() throws Exception {
        doNothing().when(commentService).deleteComment(1L, 2L);

        mvc.perform(delete("/api/posts/1/comments/2"))
            .andExpect(status().isOk());
    }
}
