package ru.yandex.practicum.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.configuration.PostgresTestConfiguration;
import ru.yandex.practicum.storage.InMemoryPostImageStorage;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(PostgresTestConfiguration.class)
public abstract class AbstractE2eTest {

    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected NamedParameterJdbcTemplate jdbc;
    @Autowired protected InMemoryPostImageStorage postImageStorage;

    @BeforeEach
    void beforeEach() {
        jdbc.getJdbcTemplate().execute("""
            TRUNCATE TABLE
                post_tags,
                comments,
                tags,
                posts
            RESTART IDENTITY CASCADE
            """);

        postImageStorage.clear();
    }
}
