package ru.yandex.practicum.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import ru.yandex.practicum.configuration.E2eTestConfiguration;
import ru.yandex.practicum.storage.InMemoryPostImageStorage;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@WebAppConfiguration
@SpringJUnitConfig(classes = E2eTestConfiguration.class)
public class AbstractE2eTest {

    @Autowired protected WebApplicationContext wac;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected NamedParameterJdbcTemplate jdbc;
    @Autowired protected InMemoryPostImageStorage postImageStorage;

    protected MockMvc mvc;

    @BeforeEach
    void beforeEach() {
        this.mvc = webAppContextSetup(wac).build();

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
