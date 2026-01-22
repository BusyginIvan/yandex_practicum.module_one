package ru.yandex.practicum.repository.posts;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.entity.posts.PostEntity;
import ru.yandex.practicum.exception.not_found.PostNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcPostRepository implements PostRepository {

    private static final RowMapper<PostEntity> POST_ROW_MAPPER =
        (rs, rowNum) -> new PostEntity(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("text"),
            rs.getInt("likes_count")
        );

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcPostRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public long insert(String title, String text) {
        String sql = """
            INSERT INTO posts (title, text)
            VALUES (:title, :text)
            RETURNING id
            """;

        Long id = jdbc.queryForObject(sql, Map.of("title", title, "text", text), Long.class);
        if (id == null) throw new IllegalStateException("Failed to insert post: id is null");
        return id;
    }

    @Override
    public Optional<PostEntity> findById(long id) {
        String sql = """
            SELECT id, title, text, likes_count
            FROM posts
            WHERE id = :id
            """;

        return jdbc.query(sql, Map.of("id", id), POST_ROW_MAPPER)
            .stream()
            .findFirst();
    }

    @Override
    public void update(long id, String title, String text) {
        String sql = """
            UPDATE posts
            SET title = :title,
                text = :text,
                updated_at = now()
            WHERE id = :id
            """;
        int updated = jdbc.update(sql, Map.of("id", id, "title", title, "text", text));
        if (updated == 0) throw new PostNotFoundException(id);
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM posts WHERE id = :id";
        int updated = jdbc.update(sql, Map.of("id", id));
        if (updated == 0) throw new PostNotFoundException(id);
    }

    @Override
    public List<PostEntity> searchPage(String titleSubstring, List<String> tags, int offset, int limit) {
        titleSubstring = titleSubstring == null || titleSubstring.isBlank() ? "" : titleSubstring;

        return tags == null || tags.isEmpty()
            ? searchByTitle(titleSubstring, offset, limit)
            : searchByTitleAndTags(titleSubstring, tags, offset, limit);
    }

    private List<PostEntity> searchByTitle(String ts, int offset, int limit) {
        String sql = """
            SELECT p.id, p.title, p.text, p.likes_count
            FROM posts p
            WHERE (:ts = '' OR lower(p.title) LIKE '%' || lower(:ts) || '%')
            ORDER BY p.id DESC
            OFFSET :offset
            LIMIT :limit
            """;

        var params = new MapSqlParameterSource()
            .addValue("ts", ts)
            .addValue("offset", offset)
            .addValue("limit", limit);

        return jdbc.query(sql, params, POST_ROW_MAPPER);
    }

    private List<PostEntity> searchByTitleAndTags(String ts, List<String> tags, int offset, int limit) {
        String sql = """
            SELECT p.id, p.title, p.text, p.likes_count
            FROM posts p
            JOIN post_tags pt ON pt.post_id = p.id
            JOIN tags t ON t.id = pt.tag_id
            WHERE (:ts = '' OR lower(p.title) LIKE '%' || lower(:ts) || '%')
              AND t.name IN (:tags)
            GROUP BY p.id
            HAVING COUNT(*) = :tagCount
            ORDER BY p.id DESC
            OFFSET :offset
            LIMIT :limit
            """;

        var params = new MapSqlParameterSource()
            .addValue("ts", ts)
            .addValue("tags", tags)
            .addValue("tagCount", tags.size())
            .addValue("offset", offset)
            .addValue("limit", limit);

        return jdbc.query(sql, params, POST_ROW_MAPPER);
    }

    @Override
    public int countBySearch(String titleSubstring, List<String> tags) {
        titleSubstring = titleSubstring == null || titleSubstring.isBlank() ? "" : titleSubstring;

        return tags == null || tags.isEmpty()
            ? countByTitle(titleSubstring)
            : countByTitleAndTags(titleSubstring, tags);
    }

    private int countByTitle(String ts) {
        String sql = """
            SELECT COUNT(*)
            FROM posts p
            WHERE (:ts = '' OR lower(p.title) LIKE '%' || lower(:ts) || '%')
            """;

        Integer c = jdbc.queryForObject(sql, Map.of("ts", ts), Integer.class);
        return c == null ? 0 : c;
    }

    private int countByTitleAndTags(String ts, List<String> tags) {
        String sql = """
            SELECT COUNT(*)
            FROM (
                SELECT p.id
                FROM posts p
                JOIN post_tags pt ON pt.post_id = p.id
                JOIN tags t ON t.id = pt.tag_id
                WHERE (:ts = '' OR lower(p.title) LIKE '%' || lower(:ts) || '%')
                  AND t.name IN (:tags)
                GROUP BY p.id
                HAVING COUNT(*) = :tagCount
            ) x
            """;

        var params = new MapSqlParameterSource()
            .addValue("ts", ts)
            .addValue("tags", tags)
            .addValue("tagCount", tags.stream().distinct().count());

        Integer c = jdbc.queryForObject(sql, params, Integer.class);
        return c == null ? 0 : c;
    }

    @Override
    public int incrementLikes(long id) {
        String sql = """
            UPDATE posts
            SET likes_count = likes_count + 1,
                updated_at = now()
            WHERE id = :id
            RETURNING likes_count
            """;

        try {
            Integer newCount = jdbc.queryForObject(sql, Map.of("id", id), Integer.class);
            if (newCount == null) throw new IllegalStateException("Failed to increment likes: likes_count is null");
            return newCount;
        } catch (EmptyResultDataAccessException e) {
            throw new PostNotFoundException(id);
        }
    }

    @Override
    public Optional<String> findImageContentType(long id) {
        List<String> result = jdbc.query(
            "SELECT image_content_type FROM posts WHERE id = :id",
            Map.of("id", id),
            (rs, rowNum) -> rs.getString("image_content_type")
        );
        if (result.isEmpty()) throw new PostNotFoundException(id);
        return Optional.ofNullable(result.getFirst());
    }

    @Override
    public void updateImageContentType(long id, String type) {
        String sql = """
            UPDATE posts
            SET image_content_type = :type,
                updated_at = now()
            WHERE id = :id
            """;
        int updated = jdbc.update(sql, Map.of("id", id, "type", type));
        if (updated == 0) throw new PostNotFoundException(id);
    }
}
