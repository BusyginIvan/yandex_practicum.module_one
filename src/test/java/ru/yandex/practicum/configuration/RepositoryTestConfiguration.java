package ru.yandex.practicum.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(PostgresTestConfiguration.class)
@ComponentScan(basePackages = "ru.yandex.practicum.repository")
public class RepositoryTestConfiguration { }
