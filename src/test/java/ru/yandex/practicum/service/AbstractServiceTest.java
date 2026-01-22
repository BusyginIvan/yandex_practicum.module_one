package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.yandex.practicum.configuration.ServiceTestConfiguration;
import ru.yandex.practicum.repository.comments.CommentRepository;
import ru.yandex.practicum.repository.posts.PostRepository;
import ru.yandex.practicum.repository.tags.TagRepository;
import ru.yandex.practicum.storage.PostImageStorage;

import static org.mockito.Mockito.reset;

@SpringJUnitConfig(classes = ServiceTestConfiguration.class)
public abstract class AbstractServiceTest {

    @Autowired protected PostRepository postRepository;
    @Autowired protected CommentRepository commentRepository;
    @Autowired protected TagRepository tagRepository;
    @Autowired protected PostImageStorage postImageStorage;

    @BeforeEach
    void resetMocks() {
        reset(
            postRepository,
            commentRepository,
            tagRepository,
            postImageStorage
        );
    }
}
