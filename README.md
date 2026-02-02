# Blog Backend

Бэкенд веб-приложения блога, реализованный на **Java 21** с использованием **Spring Boot 4.x**.  
Проект предоставляет REST API для управления постами, комментариями, лайками и изображениями постов.

## Стек технологий

- Java 21
- Spring Boot 4.x
    - Spring Web (MVC)
    - Spring JDBC
    - Spring Validation
    - Spring Test
- PostgreSQL 16
- Gradle
- MapStruct
- Jackson
- JUnit 5
- Mockito
- Testcontainers (PostgreSQL)
- Docker / Docker Compose

## Архитектура проекта

Проект реализован в виде слоистой архитектуры с явным разделением ответственности между слоями.

### Web / API слой (`api`)

- REST-контроллеры
- DTO и контракт API
- Валидация входных данных
- Маппинг API ↔ доменная модель
- Централизованная обработка ошибок и формирование HTTP-ответов

### Слой бизнес-логики (`domain`, `service`)

- Доменные модели (`Post`, `Comment`, `PostPage`, и др.)
- Прикладные сервисы (`PostService`, `CommentService`)
- Бизнес-инварианты и правила
- Границы транзакций
- Оркестрация работы с хранилищами данных

### Слой доступа к данным (`repository`, `storage`)

- Репозитории для работы с БД через Spring JDBC
- Явные SQL-запросы без использования ORM
- Работа с файловой системой для хранения изображений постов
- Скрытие деталей хранения от доменного слоя

### Конфигурация приложения

- Автоконфигурация Spring Boot
- Явные Java-конфигурации там, где это необходимо
- Использование `application.yaml`
- Профили Spring (`test`, default)

## Профили

- `default` — файловое хранилище изображений
- `test` — in-memory хранилище изображений

## Структура базы данных

Основные таблицы:

- `posts` — посты
- `comments` — комментарии к постам
- `tags` — теги
- `post_tags` — связь many-to-many между постами и тегами

Схема инициализируется автоматически при старте приложения из `schema.sql`.

## REST API

Бэкенд запускается на `http://localhost:8080`.

### Посты

- **GET** `/api/posts?search=...&pageNumber=1&pageSize=5`  
  Получение страницы постов с поиском и пагинацией.

- **GET** `/api/posts/{id}`  
  Получение полного поста.

- **POST** `/api/posts`  
  Создание поста.

- **PUT** `/api/posts/{id}`  
  Редактирование поста.

- **DELETE** `/api/posts/{id}`  
  Удаление поста (вместе с комментариями и изображением).

- **POST** `/api/posts/{id}/likes`  
  Инкремент лайков поста.

### Изображения постов

- **PUT** `/api/posts/{id}/image`  
  Загрузка/обновление изображения поста (`multipart/form-data`).

- **GET** `/api/posts/{id}/image`  
  Получение изображения поста (или дефолтного, если не задано).

### Комментарии

- **GET** `/api/posts/{postId}/comments`  
  Получение всех комментариев поста.

- **GET** `/api/posts/{postId}/comments/{commentId}`  
  Получение конкретного комментария.

- **POST** `/api/posts/{postId}/comments`  
  Добавление комментария.

- **PUT** `/api/posts/{postId}/comments/{commentId}`  
  Редактирование комментария.

- **DELETE** `/api/posts/{postId}/comments/{commentId}`  
  Удаление комментария.

## Обработка ошибок

- Используется `@RestControllerAdvice`
- Корректные HTTP-статусы:
    - `400 Bad Request` — ошибки валидации
    - `404 Not Found` — сущности не найдены
    - `500 Internal Server Error` — непредвиденные ошибки
- Единый формат JSON-ответа для ошибок

## Валидация

- Jakarta Bean Validation
- Валидация:
    - path-параметров
    - query-параметров
    - JSON-тел запросов
- Дополнительные доменные проверки (например, совпадение id в path и body)

## Тестирование

В проекте используются разные типы тестов с чётким разделением ответственности:

### Service-тесты (сервисный слой)

- JUnit 5 + Mockito (моки как Spring-бины)
- Spring Test Context: `@SpringJUnitConfig(ServiceTestConfiguration.class)`
- Тестирование бизнес-логики сервисов
- Без Spring Boot (не поднимается автоконфигурация, веб и БД)

### Repository-тесты

- `@DataJdbcTest`
- PostgreSQL через Testcontainers
- Реальные SQL-запросы
- Embedded-БД отключена: `@AutoConfigureTestDatabase(replace = Replace.NONE)`

### API-тесты

- `@WebMvcTest`
- MockMvc
- Моки сервисов через `@MockitoBean`

### End-to-End тесты

- `@SpringBootTest` + `@AutoConfigureMockMvc`
- PostgreSQL через Testcontainers
- Проверка работы приложения целиком (web → service → repository → db)

## Сборка и запуск через Docker

### Первый запуск

1. **Создать файл окружения**

Создайте файл `.env` на основе `.env.example`:

```bash
cp .env.example .env
```

2. **Запустить PostgreSQL**

```bash
docker compose up -d db
```

3. **Инициализировать схему базы данных**

```bash
docker compose exec db psql -U blog_user -d blog -f /schema.sql
```

4. **Запустить приложение**

```bash
docker compose up --build -d web
```

После запуска:

- PostgreSQL доступен на `localhost:5432`
- Приложение доступно на `http://localhost:8080`

Приложение собирается в **executable JAR** и запускается со встроенным веб-сервером Spring Boot.
Изображения постов сохраняются в Docker volume.

### Повторный запуск

Для повторного запуска, пересборки приложения:

```bash
docker compose up --build -d
```

## Особенности реализации

- Spring Boot приложение со встроенным веб-сервером
- Executable JAR
- Явные SQL-запросы без ORM
- MapStruct для маппинга DTO ↔ domain ↔ entity
- Файловое хранилище изображений с альтернативной in-memory реализацией для тестов
- Использование профилей Spring для тестовой конфигурации
