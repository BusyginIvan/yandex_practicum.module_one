package ru.yandex.practicum.configuration;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

@Configuration
public class DatabaseTestConfiguration {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public PostgreSQLContainer<?> postgres() {
        return new PostgreSQLContainer<>("postgres:16");
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public static BeanFactoryPostProcessor dbProps(PostgreSQLContainer<?> pg) {
        return bf -> {
            ConfigurableEnvironment env = bf.getBean(ConfigurableEnvironment.class);

            Map<String, Object> props = Map.of(
                "db.url", pg.getJdbcUrl(),
                "db.username", pg.getUsername(),
                "db.password", pg.getPassword()
            );

            env.getPropertySources().addFirst(new MapPropertySource("testcontainers", props));
        };
    }
}