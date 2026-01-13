package ru.yandex.practicum.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.domain.Comment;
import ru.yandex.practicum.exception.CommentNotFoundException;
import ru.yandex.practicum.repository.comments.CommentRepository;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository comments;

    public CommentService(CommentRepository comments) {
        this.comments = comments;
    }

    public List<Comment> getComments(Long postId) {
        return comments.findByPostId(postId);
    }

    public Comment getComment(long postId, long commentId) {
        return comments.findByPostIdAndId(postId, commentId)
            .orElseThrow(() -> new CommentNotFoundException(postId, commentId));
    }

    public Comment addComment(long postId, String text) {
        long id = comments.insert(postId, text);
        return getComment(postId, id);
    }

    public Comment updateComment(long postId, long commentId, String text) {
        comments.update(postId, commentId, text);
        return getComment(postId, commentId);
    }

    public void deleteComment(long postId, long commentId) {
        comments.delete(postId, commentId);
    }
}
