package ru.yandex.practicum.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    DatabaseTestConfiguration.class,
    JdbcConfiguration.class
})
@ComponentScan(basePackages = "ru.yandex.practicum.repository")
public class RepositoryTestConfiguration { }
