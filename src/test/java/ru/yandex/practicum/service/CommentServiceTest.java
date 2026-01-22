package ru.yandex.practicum.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.domain.Comment;
import ru.yandex.practicum.exception.not_found.CommentNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class CommentServiceTest extends AbstractServiceTest {

    @Autowired CommentService commentService;

    // ========================= getComments =========================

    @Test
    void getComments() {
        long postId = 1L;

        List<Comment> expected = List.of(
            new Comment(10L, postId, "a"),
            new Comment(11L, postId, "b")
        );

        when(commentRepository.findByPostId(postId)).thenReturn(expected);

        List<Comment> actual = commentService.getComments(postId);

        assertEquals(expected, actual);
        verify(commentRepository).findByPostId(postId);
        verifyNoMoreInteractions(commentRepository);
    }

    // ========================= getComment =========================

    @Test
    void getComment() {
        long postId = 1L;
        long commentId = 2L;

        Comment expected = new Comment(commentId, postId, "text");
        when(commentRepository.findByPostIdAndId(postId, commentId)).thenReturn(Optional.of(expected));

        Comment actual = commentService.getComment(postId, commentId);

        assertEquals(expected, actual);
        verify(commentRepository).findByPostIdAndId(postId, commentId);
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    void getComment_notFound() {
        when(commentRepository.findByPostIdAndId(1L, 2L)).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> commentService.getComment(1L, 2L));

        verify(commentRepository).findByPostIdAndId(1L, 2L);
        verifyNoMoreInteractions(commentRepository);
    }

    // ========================= addComment =========================

    @Test
    void addComment() {
        long postId = 1L;
        String text = "hello";
        long newId = 100L;

        when(commentRepository.insert(postId, text)).thenReturn(newId);

        Comment expected = new Comment(newId, postId, text);
        when(commentRepository.findByPostIdAndId(postId, newId)).thenReturn(Optional.of(expected));

        Comment actual = commentService.addComment(postId, text);

        assertEquals(expected, actual);

        verify(commentRepository).insert(postId, text);
        verify(commentRepository).findByPostIdAndId(postId, newId);
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    void addComment_insertedButCannotLoad() {
        long postId = 1L;
        long newId = 100L;

        when(commentRepository.insert(postId, "x")).thenReturn(newId);
        when(commentRepository.findByPostIdAndId(postId, newId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> commentService.addComment(postId, "x"));

        verify(commentRepository).insert(postId, "x");
        verify(commentRepository).findByPostIdAndId(postId, newId);
        verifyNoMoreInteractions(commentRepository);
    }

    // ========================= updateComment =========================

    @Test
    void updateComment() {
        long postId = 1L;
        long commentId = 2L;
        String newText = "updated";

        doNothing().when(commentRepository).update(postId, commentId, newText);

        Comment expected = new Comment(commentId, postId, newText);
        when(commentRepository.findByPostIdAndId(postId, commentId)).thenReturn(Optional.of(expected));

        Comment actual = commentService.updateComment(postId, commentId, newText);

        assertEquals(expected, actual);

        verify(commentRepository).update(postId, commentId, newText);
        verify(commentRepository).findByPostIdAndId(postId, commentId);
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    void updateComment_updatedButCannotLoad() {
        long postId = 1L;
        long commentId = 2L;

        doNothing().when(commentRepository).update(postId, commentId, "x");
        when(commentRepository.findByPostIdAndId(postId, commentId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> commentService.updateComment(postId, commentId, "x"));

        verify(commentRepository).update(postId, commentId, "x");
        verify(commentRepository).findByPostIdAndId(postId, commentId);
        verifyNoMoreInteractions(commentRepository);
    }

    // ========================= deleteComment =========================

    @Test
    void deleteComment() {
        commentService.deleteComment(1L, 2L);

        verify(commentRepository).delete(1L, 2L);
        verifyNoMoreInteractions(commentRepository);
    }
}
