package ru.yandex.practicum.repository.posts;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.yandex.practicum.entity.posts.PostEntity;
import ru.yandex.practicum.exception.not_found.PostNotFoundException;
import ru.yandex.practicum.repository.AbstractRepositoryTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JdbcPostRepositoryTest extends AbstractRepositoryTest {

    @Autowired private NamedParameterJdbcTemplate jdbc;
    @Autowired private PostRepository postRepository;

    // ========================= insert + findById =========================

    @Test
    void insertAndFindById() {
        long id = postRepository.insert("ttl", "txt");

        Optional<PostEntity> opt = postRepository.findById(id);
        assertTrue(opt.isPresent());

        PostEntity e = opt.get();
        assertEquals(id, e.id());
        assertEquals("ttl", e.title());
        assertEquals("txt", e.text());
        assertEquals(0, e.likesCount());
    }

    @Test
    void findById_missing_returnsEmpty() {
        assertTrue(postRepository.findById(999L).isEmpty());
    }

    // ========================= update =========================

    @Test
    void update_updatesRow() {
        long id = postRepository.insert("t1", "x1");

        postRepository.update(id, "t2", "x2");

        PostEntity e = postRepository.findById(id).orElseThrow();
        assertEquals("t2", e.title());
        assertEquals("x2", e.text());
    }

    @Test
    void update_missing_throws() {
        assertThrows(PostNotFoundException.class, () -> postRepository.update(123L, "t", "x"));
    }

    // ========================= deleteById =========================

    @Test
    void deleteById_deletesRow() {
        long id = postRepository.insert("t", "x");
        postRepository.deleteById(id);
        assertTrue(postRepository.findById(id).isEmpty());
    }

    @Test
    void deleteById_missing_throws() {
        assertThrows(PostNotFoundException.class, () -> postRepository.deleteById(123L));
    }

    // ========================= incrementLikes =========================

    @Test
    void incrementLikes_incrementsAndReturnsNewValue() {
        long id = postRepository.insert("t", "x");

        int v1 = postRepository.incrementLikes(id);
        int v2 = postRepository.incrementLikes(id);

        assertEquals(1, v1);
        assertEquals(2, v2);

        PostEntity e = postRepository.findById(id).orElseThrow();
        assertEquals(2, e.likesCount());
    }

    @Test
    void incrementLikes_missing_throws() {
        assertThrows(PostNotFoundException.class, () -> postRepository.incrementLikes(999L));
    }

    // ========================= image content type =========================

    @Test
    void findImageContentType_whenNotSet_returnsEmpty() {
        long id = postRepository.insert("t", "x");
        Optional<String> ct = postRepository.findImageContentType(id);
        assertTrue(ct.isEmpty());
    }

    @Test
    void findImageContentType_missingPost_throws() {
        assertThrows(PostNotFoundException.class, () -> postRepository.findImageContentType(999L));
    }

    @Test
    void updateImageContentTypeAndFind() {
        long id = postRepository.insert("t", "x");

        postRepository.updateImageContentType(id, "image/png");

        String ct = postRepository.findImageContentType(id).orElseThrow();
        assertEquals("image/png", ct);
    }

    @Test
    void updateImageContentType_missing_throws() {
        assertThrows(PostNotFoundException.class, () -> postRepository.updateImageContentType(999L, "image/png"));
    }

    // ========================= searchPage + countBySearch =========================

    @Test
    void countBySearch_noFilters_countsAll() {
        seedPostsNoTags(
            "Java and Spring",
            "Kotlin tips",
            "Another post"
        );

        int c = postRepository.countBySearch("", List.of());

        assertEquals(3, c);
    }

    @Test
    void searchPage_noFilters_ordersByIdDesc_andPaginates() {
        long id1 = postRepository.insert("A", "x");
        long id2 = postRepository.insert("B", "x");
        long id3 = postRepository.insert("C", "x");

        List<PostEntity> page1 = postRepository.searchPage("", List.of(), 0, 2);
        assertEquals(2, page1.size());
        assertEquals(id3, page1.get(0).id());
        assertEquals(id2, page1.get(1).id());

        List<PostEntity> page2 = postRepository.searchPage("", List.of(), 2, 2);
        assertEquals(1, page2.size());
        assertEquals(id1, page2.get(0).id());
    }

    @Test
    void countBySearch_titleSubstring_isCaseInsensitiveAndMatches() {
        seedPostsNoTags(
            "Hello World",
            "HeLLo Java",
            "Other"
        );

        assertEquals(2, postRepository.countBySearch("hello", List.of()));
        assertEquals(1, postRepository.countBySearch("world", List.of()));
        assertEquals(0, postRepository.countBySearch("missing", List.of()));
    }

    @Test
    void searchPage_titleSubstring_filters() {
        long id1 = postRepository.insert("Hello World", "x");
        long id2 = postRepository.insert("HeLLo Java", "x");
        postRepository.insert("Other", "x");

        List<PostEntity> res = postRepository.searchPage("hello", List.of(), 0, 5);

        assertEquals(2, res.size());
        assertEquals(id2, res.get(0).id());
        assertEquals(id1, res.get(1).id());
    }

    @Test
    void countBySearch_tags_matchesPostsHavingAllTags() {
        long p1 = postRepository.insert("P1", "x");
        long p2 = postRepository.insert("P2", "x");
        long p3 = postRepository.insert("P3", "x");

        linkTags(p1, List.of("java", "spring"));
        linkTags(p2, List.of("java"));
        linkTags(p3, List.of("spring", "db"));

        assertEquals(2, postRepository.countBySearch("", List.of("spring")));
        assertEquals(2, postRepository.countBySearch("", List.of("java")));
        assertEquals(1, postRepository.countBySearch("", List.of("java", "spring")));
        assertEquals(0, postRepository.countBySearch("", List.of("java", "db")));
    }

    @Test
    void searchPage_tags_filtersAndRequiresAllTags() {
        long p1 = postRepository.insert("P1", "x");
        long p2 = postRepository.insert("P2", "x");
        long p3 = postRepository.insert("P3", "x");

        linkTags(p1, List.of("java", "spring"));
        linkTags(p2, List.of("java"));
        linkTags(p3, List.of("java", "spring", "db"));

        List<PostEntity> res = postRepository.searchPage("", List.of("java", "spring"), 0, 5);

        assertEquals(2, res.size());
        assertEquals(p3, res.get(0).id());
        assertEquals(p1, res.get(1).id());
    }

    @Test
    void countBySearch_titleAndTags() {
        long p1 = postRepository.insert("Hello Java", "x");
        long p2 = postRepository.insert("Hello Spring", "x");
        long p3 = postRepository.insert("Other Java", "x");

        linkTags(p1, List.of("java", "spring"));
        linkTags(p2, List.of("spring"));
        linkTags(p3, List.of("java"));

        assertEquals(1, postRepository.countBySearch("hello", List.of("java", "spring")));
        assertEquals(2, postRepository.countBySearch("hello", List.of("spring")));
        assertEquals(1, postRepository.countBySearch("hello", List.of("java")));
    }

    @Test
    void searchPage_titleAndTags() {
        long p1 = postRepository.insert("Hello Java", "x");
        long p2 = postRepository.insert("Hello Spring", "x");
        long p3 = postRepository.insert("Other Java", "x");

        linkTags(p1, List.of("java", "spring"));
        linkTags(p2, List.of("java"));
        linkTags(p3, List.of("java", "spring", "db"));

        List<PostEntity> res = postRepository.searchPage("hello", List.of("java", "spring"), 0, 5);
        assertEquals(1, res.size());
        assertEquals(p1, res.get(0).id());
    }

    // ========================= helpers =========================

    private void seedPostsNoTags(String... titles) {
        for (String t : titles) {
            postRepository.insert(t, "x");
        }
    }

    private void linkTags(long postId, List<String> tagNames) {
        for (String tag : tagNames) {
            long tagId = upsertTag(tag);
            jdbc.update(
                "INSERT INTO post_tags (post_id, tag_id) VALUES (:p, :t) ON CONFLICT DO NOTHING",
                java.util.Map.of("p", postId, "t", tagId)
            );
        }
    }

    private long upsertTag(String name) {
        Long id = jdbc.queryForObject(
            "INSERT INTO tags(name) VALUES (:n) ON CONFLICT(name) DO UPDATE SET name = EXCLUDED.name RETURNING id",
            java.util.Map.of("n", name),
            Long.class
        );
        if (id == null) throw new IllegalStateException("tag id is null");
        return id;
    }
}
