package ru.yandex.practicum.api.comments;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.IdChecks;
import ru.yandex.practicum.api.comments.dto.CommentCreateRequest;
import ru.yandex.practicum.api.comments.dto.CommentDto;
import ru.yandex.practicum.api.comments.dto.CommentUpdateRequest;
import ru.yandex.practicum.domain.Comment;
import ru.yandex.practicum.exception.validation.ValidationException;
import ru.yandex.practicum.service.CommentService;

import java.util.List;

@Validated
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
    public List<CommentDto> getComments(@PathVariable("postId") @NotNull String rawPostId) {
        if (rawPostId.trim().equalsIgnoreCase("undefined")) {
            return List.of();
        }

        long postId;
        try {
            postId = Long.parseLong(rawPostId);
        } catch (NumberFormatException e) {
            throw new ValidationException("postId must be a positive number", e);
        }

        if (postId <= 0) {
            throw new ValidationException("postId must be positive");
        }

        return commentService.getComments(postId).stream()
            .map(mapper::toDto)
            .toList();
    }

    @GetMapping("/{commentId}")
    public CommentDto getComment(
        @PathVariable @Positive long postId,
        @PathVariable @Positive long commentId
    ) {
        Comment c = commentService.getComment(postId, commentId);
        return mapper.toDto(c);
    }

    @PostMapping
    public CommentDto addComment(
        @PathVariable @Positive long postId,
        @Valid @RequestBody CommentCreateRequest request
    ) {
        Comment c = commentService.addComment(postId, request.text());
        return mapper.toDto(c);
    }

    @PutMapping("/{commentId}")
    public CommentDto updateComment(
        @PathVariable @Positive long postId,
        @PathVariable @Positive long commentId,
        @Valid @RequestBody CommentUpdateRequest request
    ) {
        IdChecks.requireMatch("post", postId, request.postId());
        IdChecks.requireMatch("comment", commentId, request.id());
        Comment c = commentService.updateComment(postId, commentId, request.text());
        return mapper.toDto(c);
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(
        @PathVariable @Positive long postId,
        @PathVariable @Positive long commentId
    ) {
        commentService.deleteComment(postId, commentId);
    }
}
