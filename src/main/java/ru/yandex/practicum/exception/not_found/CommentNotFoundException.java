package ru.yandex.practicum.exception.not_found;

public class CommentNotFoundException extends NotFoundException {
    public CommentNotFoundException(long postId, long commentId) {
        super("Comment not found: postId=" + postId + ", commentId=" + commentId);
    }
}
