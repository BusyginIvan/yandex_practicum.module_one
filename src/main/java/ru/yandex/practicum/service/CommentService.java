package ru.yandex.practicum.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.domain.Comment;
import ru.yandex.practicum.exception.not_found.CommentNotFoundException;
import ru.yandex.practicum.repository.comments.CommentRepository;

import java.util.List;
import java.util.function.Supplier;

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
        return getComment(postId, commentId, () -> new CommentNotFoundException(postId, commentId));
    }

    private Comment getAfterWrite(long postId, long commentId) {
        return getComment(postId, commentId, () -> new IllegalStateException(
            "Comment disappeared after write: postId=" + postId + ", commentId=" + commentId
        ));
    }

    private Comment getComment(long postId, long commentId, Supplier<? extends RuntimeException> onMissing) {
        return comments.findByPostIdAndId(postId, commentId).orElseThrow(onMissing);
    }

    @Transactional
    public Comment addComment(long postId, String text) {
        long id = comments.insert(postId, text);
        return getAfterWrite(postId, id);
    }

    @Transactional
    public Comment updateComment(long postId, long commentId, String text) {
        comments.update(postId, commentId, text);
        return getAfterWrite(postId, commentId);
    }

    public void deleteComment(long postId, long commentId) {
        comments.delete(postId, commentId);
    }
}
