package ru.yandex.practicum.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(basePackages = "ru.yandex.practicum.api")
public class ApiTestConfiguration { }
