package ru.yandex.practicum.api.comments;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.comments.dto.CommentCreateRequest;
import ru.yandex.practicum.api.comments.dto.CommentDto;
import ru.yandex.practicum.api.comments.dto.CommentUpdateRequest;
import ru.yandex.practicum.domain.Comment;
import ru.yandex.practicum.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class PostCommentsController {

    private final CommentService commentService;
    private final CommentApiMapper mapper;

    public PostCommentsController(CommentService commentService, CommentApiMapper mapper) {
        this.commentService = commentService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<CommentDto> getComments(@PathVariable("postId") String rawPostId) {
        Long postId = parsePostIdOrNull(rawPostId);
        if (postId == null) return List.of();
        return commentService.getComments(postId).stream()
            .map(mapper::toDto)
            .toList();
    }

    @GetMapping("/{commentId}")
    public CommentDto getComment(@PathVariable long postId, @PathVariable long commentId) {
        Comment c = commentService.getComment(postId, commentId);
        return mapper.toDto(c);
    }

    @PostMapping
    public CommentDto addComment(@PathVariable long postId, @RequestBody CommentCreateRequest request) {
        Comment c = commentService.addComment(postId, request.text());
        return mapper.toDto(c);
    }

    @PutMapping("/{commentId}")
    public CommentDto updateComment(
        @PathVariable long postId,
        @PathVariable long commentId,
        @RequestBody CommentUpdateRequest request
    ) {
        Comment c = commentService.updateComment(postId, commentId, request.text());
        return mapper.toDto(c);
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable long postId, @PathVariable long commentId) {
        commentService.deleteComment(postId, commentId);
    }

    private static Long parsePostIdOrNull(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty() || s.equalsIgnoreCase("undefined")) return null;
        try {
            long v = Long.parseLong(s);
            return v > 0 ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
