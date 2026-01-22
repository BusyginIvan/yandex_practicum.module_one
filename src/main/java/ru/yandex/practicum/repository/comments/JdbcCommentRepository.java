package ru.yandex.practicum.repository.comments;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.domain.Comment;
import ru.yandex.practicum.exception.not_found.CommentNotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcCommentRepository implements CommentRepository {

    private static final RowMapper<Comment> ROW_MAPPER = (rs, rowNum) ->
        new Comment(
            rs.getLong("id"),
            rs.getLong("post_id"),
            rs.getString("text")
        );

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcCommentRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Comment> findByPostId(long postId) {
        String sql = """
            SELECT id, post_id, text
            FROM comments
            WHERE post_id = :postId
            ORDER BY id ASC
            """;
        return jdbc.query(sql, Map.of("postId", postId), ROW_MAPPER);
    }

    @Override
    public Optional<Comment> findByPostIdAndId(long postId, long commentId) {
        String sql = """
            SELECT id, post_id, text
            FROM comments
            WHERE post_id = :postId AND id = :commentId
            """;
        return jdbc.query(sql, Map.of("postId", postId, "commentId", commentId), ROW_MAPPER)
            .stream()
            .findFirst();
    }

    @Override
    public long insert(long postId, String text) {
        String sql = """
            INSERT INTO comments (post_id, text)
            VALUES (:postId, :text)
            RETURNING id
            """;
        Long id = jdbc.queryForObject(sql, Map.of("postId", postId, "text", text), Long.class);
        if (id == null) throw new IllegalStateException("Failed to insert comment: id is null");
        return id;
    }

    @Override
    public void update(long postId, long commentId, String text) {
        String sql = """
            UPDATE comments
            SET text = :text,
                updated_at = now()
            WHERE post_id = :postId AND id = :commentId
            """;
        int updated = jdbc.update(sql, Map.of("postId", postId, "commentId", commentId, "text", text));
        if (updated == 0) throw new CommentNotFoundException(postId, commentId);
    }

    @Override
    public void delete(long postId, long commentId) {
        String sql = """
            DELETE FROM comments
            WHERE post_id = :postId AND id = :commentId
            """;
        int updated = jdbc.update(sql, Map.of("postId", postId, "commentId", commentId));
        if (updated == 0) throw new CommentNotFoundException(postId, commentId);
    }

    @Override
    public int countByPostId(long postId) {
        String sql = "SELECT COUNT(*) FROM comments WHERE post_id = :postId";
        Integer count = jdbc.queryForObject(sql, Map.of("postId", postId), Integer.class);
        return count == null ? 0 : count;
    }

    @Override
    public Map<Long, Integer> countByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) return Map.of();

        String sql = """
            SELECT post_id, COUNT(*) AS cnt
            FROM comments
            WHERE post_id IN (:ids)
            GROUP BY post_id
            """;

        MapSqlParameterSource params = new MapSqlParameterSource("ids", postIds);

        return jdbc.query(sql, params, rs -> {
            Map<Long, Integer> m = new HashMap<>();
            while (rs.next()) m.put(
                rs.getLong("post_id"),
                rs.getInt("cnt")
            );
            return m;
        });
    }
}
