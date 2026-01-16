package ru.yandex.practicum.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import ru.yandex.practicum.service.CommentService;
import ru.yandex.practicum.service.PostService;

import static org.mockito.Mockito.mock;

@Configuration
@EnableWebMvc
@Import(ValidationConfiguration.class)
@ComponentScan(basePackages = "ru.yandex.practicum.api")
public class ApiTestConfiguration {

    @Bean
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public PostService postService() {
        return mock(PostService.class);
    }

    @Bean
    public CommentService commentService() {
        return mock(CommentService.class);
    }
}
