package ru.yandex.practicum.repository.tags;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.SqlArrayValue;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class JdbcTagRepository implements TagRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcTagRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<String> findTagsByPostId(long postId) {
        return findTagsByPostIds(List.of(postId)).getOrDefault(postId, List.of());
    }

    @Override
    public Map<Long, List<String>> findTagsByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) return Map.of();

        String sql = """
            SELECT pt.post_id, t.name
            FROM post_tags pt
            JOIN tags t ON t.id = pt.tag_id
            WHERE pt.post_id IN (:ids)
            ORDER BY pt.post_id, t.name
            """;

        MapSqlParameterSource params = new MapSqlParameterSource("ids", postIds);

        return jdbc.query(sql, params, rs -> {
            Map<Long, List<String>> result = new HashMap<>();
            while (rs.next()) {
                long postId = rs.getLong("post_id");
                String name = rs.getString("name");
                result.computeIfAbsent(postId, k -> new ArrayList<>()).add(name);
            }
            return result;
        });
    }

    @Override
    public void replaceTags(long postId, List<String> tags) {
        tags = normalizeTags(tags);

        jdbc.update(
                "DELETE FROM post_tags WHERE post_id = :postId",
                Map.of("postId", postId)
        );

        if (tags.isEmpty()) return;

        SqlArrayValue tagsSqlArray = new SqlArrayValue("text", tags.toArray());
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("postId", postId)
                .addValue("names", tags)
                .addValue("namesArray", tagsSqlArray);

        String upsertTagsSql = """
            INSERT INTO tags (name)
            SELECT unnest(:namesArray) AS name
            ON CONFLICT (name) DO NOTHING
            """;
        jdbc.update(upsertTagsSql, params);

        String linkSql = """
            INSERT INTO post_tags (post_id, tag_id)
            SELECT :postId, t.id
            FROM tags t
            WHERE t.name IN (:names)
            ON CONFLICT DO NOTHING
            """;
        jdbc.update(linkSql, params);
    }

    private static List<String> normalizeTags(List<String> tags) {
        if (tags == null) return List.of();
        return tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toLowerCase)
                .distinct()
                .sorted()
                .toList();
    }
}
