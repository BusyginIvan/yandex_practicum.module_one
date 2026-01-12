package ru.yandex.practicum.repository.posts;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.domain.Post;
import ru.yandex.practicum.exception.PostNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcPostRepository implements PostRepository {

    private static final RowMapper<Post> POST_ROW_MAPPER = (rs, rowNum) -> new Post(
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
    public Optional<Post> findById(long id) {
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
    public List<Post> searchPage(String search, int offset, int limit) {
        String sql = """
                SELECT id, title, text, likes_count
                FROM posts
                WHERE lower(title) LIKE '%' || lower(:q) || '%'
                ORDER BY id DESC
                OFFSET :offset
                LIMIT :limit
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("q", search)
                .addValue("offset", offset)
                .addValue("limit", limit);

        return jdbc.query(sql, params, POST_ROW_MAPPER);
    }

    @Override
    public long countBySearch(String search) {
        String sql = """
                SELECT COUNT(*)
                FROM posts
                WHERE lower(title) LIKE '%' || lower(:q) || '%'
                """;
        Long count = jdbc.queryForObject(sql, Map.of("q", search), Long.class);
        return count == null ? 0 : count;
    }
}
