package ru.yandex.practicum.repository.comments;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.yandex.practicum.domain.Comment;
import ru.yandex.practicum.exception.not_found.CommentNotFoundException;
import ru.yandex.practicum.repository.AbstractRepositoryTest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JdbcCommentRepositoryTest extends AbstractRepositoryTest {

    @Autowired private NamedParameterJdbcTemplate jdbc;
    @Autowired private CommentRepository commentRepository;

    // ========================= findByPostId =========================

    @Test
    void findByPostId_ordersByIdAsc_filtersByPost() {
        long p1 = insertPost("p1");
        long p2 = insertPost("p2");

        long c1 = commentRepository.insert(p1, "a");
        long c2 = commentRepository.insert(p1, "b");
        commentRepository.insert(p2, "x");

        List<Comment> res = commentRepository.findByPostId(p1);

        assertEquals(2, res.size());

        assertEquals(c1, res.get(0).id());
        assertEquals(p1, res.get(0).postId());
        assertEquals("a", res.get(0).text());

        assertEquals(c2, res.get(1).id());
        assertEquals(p1, res.get(1).postId());
        assertEquals("b", res.get(1).text());
    }

    @Test
    void findByPostId_whenNoComments_returnsEmptyList() {
        long p1 = insertPost("p1");
        List<Comment> res = commentRepository.findByPostId(p1);
        assertTrue(res.isEmpty());
    }

    // ========================= findByPostIdAndId =========================

    @Test
    void findByPostIdAndId_returnsComment() {
        long postId = insertPost("p");
        long id = commentRepository.insert(postId, "hello");

        Optional<Comment> opt = commentRepository.findByPostIdAndId(postId, id);

        assertTrue(opt.isPresent());
        Comment c = opt.get();
        assertEquals(id, c.id());
        assertEquals(postId, c.postId());
        assertEquals("hello", c.text());
    }

    @Test
    void findByPostIdAndId_wrongPost_returnsEmpty() {
        long p1 = insertPost("p1");
        long p2 = insertPost("p2");
        long id = commentRepository.insert(p1, "hello");

        assertTrue(commentRepository.findByPostIdAndId(p2, id).isEmpty());
    }

    @Test
    void findByPostIdAndId_missing_returnsEmpty() {
        long postId = insertPost("p");

        assertTrue(commentRepository.findByPostIdAndId(postId, 999L).isEmpty());
    }

    // ========================= insert =========================

    @Test
    void insert_insertsRowAndReturnsId() {
        long postId = insertPost("p");

        long id = commentRepository.insert(postId, "txt");

        Comment c = commentRepository.findByPostIdAndId(postId, id).orElseThrow();
        assertEquals(id, c.id());
        assertEquals(postId, c.postId());
        assertEquals("txt", c.text());
    }

    // ========================= update =========================

    @Test
    void update_updatesText() {
        long postId = insertPost("p");
        long id = commentRepository.insert(postId, "old");

        commentRepository.update(postId, id, "new");

        Comment c = commentRepository.findByPostIdAndId(postId, id).orElseThrow();
        assertEquals("new", c.text());
    }

    @Test
    void update_missing_throws() {
        long postId = insertPost("p");
        assertThrows(CommentNotFoundException.class, () -> commentRepository.update(postId, 999L, "x"));
    }

    @Test
    void update_wrongPost_throws() {
        long p1 = insertPost("p1");
        long p2 = insertPost("p2");
        long id = commentRepository.insert(p1, "old");

        assertThrows(CommentNotFoundException.class, () -> commentRepository.update(p2, id, "new"));
    }

    // ========================= delete =========================

    @Test
    void delete_deletesRow() {
        long postId = insertPost("p");
        long id = commentRepository.insert(postId, "txt");

        commentRepository.delete(postId, id);

        assertTrue(commentRepository.findByPostIdAndId(postId, id).isEmpty());
        assertTrue(commentRepository.findByPostId(postId).isEmpty());
    }

    @Test
    void delete_missing_throws() {
        long postId = insertPost("p");
        assertThrows(CommentNotFoundException.class, () -> commentRepository.delete(postId, 999L));
    }

    @Test
    void delete_wrongPost_throws() {
        long p1 = insertPost("p1");
        long p2 = insertPost("p2");
        long id = commentRepository.insert(p1, "txt");

        assertThrows(CommentNotFoundException.class, () -> commentRepository.delete(p2, id));
    }

    // ========================= countByPostId =========================

    @Test
    void countByPostId_countsOnlyThatPost() {
        long p1 = insertPost("p1");
        long p2 = insertPost("p2");

        commentRepository.insert(p1, "a");
        commentRepository.insert(p1, "b");
        commentRepository.insert(p2, "x");

        assertEquals(2, commentRepository.countByPostId(p1));
        assertEquals(1, commentRepository.countByPostId(p2));
    }

    @Test
    void countByPostId_whenNone_returnsZero() {
        long postId = insertPost("p");
        assertEquals(0, commentRepository.countByPostId(postId));
    }

    // ========================= countByPostIds =========================

    @Test
    void countByPostIds_returnsMapOnlyForIdsWithComments() {
        long p1 = insertPost("p1");
        long p2 = insertPost("p2");
        long p3 = insertPost("p3");

        commentRepository.insert(p1, "a");
        commentRepository.insert(p1, "b");
        commentRepository.insert(p2, "x");

        Map<Long, Integer> m = commentRepository.countByPostIds(List.of(p1, p2, p3));

        assertEquals(2, m.size());
        assertEquals(2, m.get(p1));
        assertEquals(1, m.get(p2));
        assertFalse(m.containsKey(p3));
    }

    @Test
    void countByPostIds_emptyInput_returnsEmptyMap() {
        assertEquals(Map.of(), commentRepository.countByPostIds(List.of()));
        assertEquals(Map.of(), commentRepository.countByPostIds(null));
    }

    // ========================= helpers =========================

    private long insertPost(String title) {
        Long id = jdbc.queryForObject(
            "INSERT INTO posts(title, text) VALUES (:t, 'x') RETURNING id",
            Map.of("t", title),
            Long.class
        );
        if (id == null) throw new IllegalStateException("post id is null");
        return id;
    }
}