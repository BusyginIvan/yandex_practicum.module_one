package ru.yandex.practicum.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.repository.comments.CommentRepository;
import ru.yandex.practicum.repository.posts.PostRepository;
import ru.yandex.practicum.repository.tags.TagRepository;
import ru.yandex.practicum.storage.PostImageStorage;

import static org.mockito.Mockito.mock;

@Configuration
@ComponentScan(
    basePackages = {
        "ru.yandex.practicum.service",
        "ru.yandex.practicum.entity"
    }
)
public class ServiceTestConfiguration {

    @Bean
    public PostRepository postRepository() {
        return mock(PostRepository.class);
    }

    @Bean
    public CommentRepository commentRepository() {
        return mock(CommentRepository.class);
    }

    @Bean
    public TagRepository tagRepository() {
        return mock(TagRepository.class);
    }

    @Bean
    public PostImageStorage postImageStorage() {
        return mock(PostImageStorage.class);
    }
}
