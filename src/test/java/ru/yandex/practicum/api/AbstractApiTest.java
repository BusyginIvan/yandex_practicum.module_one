package ru.yandex.practicum.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.configuration.ApiTestConfiguration;
import ru.yandex.practicum.service.CommentService;
import ru.yandex.practicum.service.PostImageService;
import ru.yandex.practicum.service.PostService;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest
@Import(ApiTestConfiguration.class)
public abstract class AbstractApiTest {

    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper objectMapper;

    @MockitoBean protected PostService postService;
    @MockitoBean protected PostImageService postImageService;
    @MockitoBean protected CommentService commentService;
}

