package ru.yandex.practicum.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import ru.yandex.practicum.storage.InMemoryPostImageStorage;

@Configuration
@EnableWebMvc
@Import({
    ValidationConfiguration.class,
    DatabaseTestConfiguration.class,
    JdbcConfiguration.class
})
@ComponentScan(
    basePackages = {
        "ru.yandex.practicum.api",
        "ru.yandex.practicum.service",
        "ru.yandex.practicum.entity",
        "ru.yandex.practicum.repository"
    }
)
public class E2eTestConfiguration {

    @Bean
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public InMemoryPostImageStorage postImageStorage() {
        return new InMemoryPostImageStorage();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
