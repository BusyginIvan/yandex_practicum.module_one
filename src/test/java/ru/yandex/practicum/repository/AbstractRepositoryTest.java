package ru.yandex.practicum.repository;

import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.configuration.RepositoryTestConfiguration;

@Transactional
@SpringJUnitConfig(classes = RepositoryTestConfiguration.class)
public abstract class AbstractRepositoryTest { }
