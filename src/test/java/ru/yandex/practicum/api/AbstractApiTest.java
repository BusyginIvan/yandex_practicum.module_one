package ru.yandex.practicum.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import ru.yandex.practicum.configuration.ApiTestConfiguration;
import ru.yandex.practicum.service.CommentService;
import ru.yandex.practicum.service.PostImageService;
import ru.yandex.practicum.service.PostService;

import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@WebAppConfiguration
@SpringJUnitConfig(classes = ApiTestConfiguration.class)
public abstract class AbstractApiTest {

    @Autowired protected WebApplicationContext wac;

    @Autowired protected PostService postService;
    @Autowired protected PostImageService postImageService;
    @Autowired protected CommentService commentService;

    @Autowired protected ObjectMapper objectMapper;

    protected MockMvc mvc;

    @BeforeEach
    void setUpMockMvcAndResetMocks() {
        reset(postService, postImageService, commentService);
        this.mvc = webAppContextSetup(wac).build();
    }
}