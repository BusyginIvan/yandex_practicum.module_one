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

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class PostCommentsController {

    @GetMapping
    public List<CommentDto> getComments(@PathVariable String postId) {
        // TODO: implement
        return List.of();
    }

    @GetMapping("/{commentId}")
    public CommentDto getComment(@PathVariable long postId, @PathVariable long commentId) {
        // TODO: implement
        return new CommentDto(commentId, "", postId);
    }

    @PostMapping
    public CommentDto addComment(@PathVariable long postId, @RequestBody CommentCreateRequest request) {
        // TODO: implement
        return new CommentDto(0L, request.text(), postId);
    }

    @PutMapping("/{commentId}")
    public CommentDto updateComment(
            @PathVariable long postId,
            @PathVariable long commentId,
            @RequestBody CommentUpdateRequest request
    ) {
        // TODO: implement
        return new CommentDto(commentId, request.text(), postId);
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable long postId, @PathVariable long commentId) {
        // TODO: implement
    }
}
