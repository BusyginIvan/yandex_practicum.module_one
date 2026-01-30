package ru.yandex.practicum.repository;

import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.configuration.RepositoryTestConfiguration;

@DataJdbcTest
@Import(RepositoryTestConfiguration.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
public abstract class AbstractRepositoryTest { }