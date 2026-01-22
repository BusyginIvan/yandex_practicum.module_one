package ru.yandex.practicum.repository.tags;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.yandex.practicum.repository.AbstractRepositoryTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JdbcTagRepositoryTest extends AbstractRepositoryTest {

    @Autowired private NamedParameterJdbcTemplate jdbc;
    @Autowired private TagRepository tagRepository;

    // ========================= findTagsByPostId / findTagsByPostIds =========================

    @Test
    void findTagsByPostId() {
        long postId = insertPost("p");
        linkTags(postId, List.of("z", "a", "m"));
        assertEquals(List.of("a", "m", "z"), tagRepository.findTagsByPostId(postId));
    }

    @Test
    void findTagsByPostId_whenNone_returnsEmptyList() {
        long postId = insertPost("p");
        assertEquals(List.of(), tagRepository.findTagsByPostId(postId));
    }

    @Test
    void findTagsByPostIds_emptyInput_returnsEmptyMap() {
        assertEquals(Map.of(), tagRepository.findTagsByPostIds(List.of()));
        assertEquals(Map.of(), tagRepository.findTagsByPostIds(null));
    }

    @Test
    void findTagsByPostIds_returnsSortedByNameAndGroupedByPost() {
        long p1 = insertPost("p1");
        long p2 = insertPost("p2");

        linkTags(p1, List.of("z", "a", "m"));
        linkTags(p2, List.of("b"));

        Map<Long, List<String>> m = tagRepository.findTagsByPostIds(List.of(p1, p2));

        assertEquals(List.of("a", "m", "z"), m.get(p1));
        assertEquals(List.of("b"), m.get(p2));
    }

    @Test
    void findTagsByPostIds_doesNotCreateEntryForPostsWithoutTags() {
        long p1 = insertPost("p1");
        long p2 = insertPost("p2");

        linkTags(p1, List.of("a"));

        Map<Long, List<String>> m = tagRepository.findTagsByPostIds(List.of(p1, p2));

        assertEquals(1, m.size());
        assertEquals(List.of("a"), m.get(p1));
    }

    // ========================= replaceTags =========================

    @Test
    void replaceTags_normalizesAndSetsTags() {
        long postId = insertPost("p");

        tagRepository.replaceTags(postId, Arrays.asList(
            "  Java  ",
            "spring",
            "JAVA",
            "",
            "  ",
            null,
            "db"
        ));

        assertEquals(List.of("db", "java", "spring"), tagRepository.findTagsByPostId(postId));
    }

    @Test
    void replaceTags_replacesOldTags() {
        long postId = insertPost("p");

        tagRepository.replaceTags(postId, List.of("a", "b"));
        assertEquals(List.of("a", "b"), tagRepository.findTagsByPostId(postId));

        tagRepository.replaceTags(postId, List.of("b", "c"));
        assertEquals(List.of("b", "c"), tagRepository.findTagsByPostId(postId));
    }

    @Test
    void replaceTags_emptyList_clearsTags() {
        long postId = insertPost("p");

        tagRepository.replaceTags(postId, List.of("a"));
        assertEquals(List.of("a"), tagRepository.findTagsByPostId(postId));

        tagRepository.replaceTags(postId, List.of());
        assertEquals(List.of(), tagRepository.findTagsByPostId(postId));
    }

    @Test
    void replaceTags_nullList_clearsTags() {
        long postId = insertPost("p");

        tagRepository.replaceTags(postId, List.of("a"));
        assertEquals(List.of("a"), tagRepository.findTagsByPostId(postId));

        tagRepository.replaceTags(postId, null);
        assertEquals(List.of(), tagRepository.findTagsByPostId(postId));
    }

    @Test
    void replaceTags_existingTagsReused_onConflictDoNothing() {
        long p1 = insertPost("p1");
        long p2 = insertPost("p2");

        tagRepository.replaceTags(p1, List.of("java", "spring"));
        assertEquals(2, countTags());

        tagRepository.replaceTags(p2, List.of("spring", "java"));
        assertEquals(2, countTags());

        assertEquals(List.of("java", "spring"), tagRepository.findTagsByPostId(p1));
        assertEquals(List.of("java", "spring"), tagRepository.findTagsByPostId(p2));
    }

    @Test
    void replaceTags_noEffectOnOtherPosts() {
        long p1 = insertPost("p1");
        long p2 = insertPost("p2");

        tagRepository.replaceTags(p1, List.of("a", "b"));
        tagRepository.replaceTags(p2, List.of("a", "c"));

        tagRepository.replaceTags(p1, List.of("b", "d"));

        assertEquals(List.of("b", "d"), tagRepository.findTagsByPostId(p1));
        assertEquals(List.of("a", "c"), tagRepository.findTagsByPostId(p2));
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

    private void linkTags(long postId, List<String> tagNames) {
        for (String tag : tagNames) {
            long tagId = upsertTag(tag);
            jdbc.update(
                "INSERT INTO post_tags (post_id, tag_id) VALUES (:p, :t) ON CONFLICT DO NOTHING",
                Map.of("p", postId, "t", tagId)
            );
        }
    }

    private long upsertTag(String name) {
        Long id = jdbc.queryForObject(
            "INSERT INTO tags(name) VALUES (:n) ON CONFLICT(name) DO UPDATE SET name = EXCLUDED.name RETURNING id",
            Map.of("n", name),
            Long.class
        );
        if (id == null) throw new IllegalStateException("tag id is null");
        return id;
    }

    private int countTags() {
        String sql = "SELECT COUNT(*) FROM tags";
        Integer c = jdbc.getJdbcTemplate().queryForObject(sql, Integer.class);
        return c == null ? 0 : c;
    }
}
