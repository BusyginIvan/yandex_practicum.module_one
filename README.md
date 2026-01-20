# Blog Backend

Бэкенд веб-приложения блога, реализованный на **Java 21** с использованием **Spring Framework 6.x** (без Spring Boot).  
Проект предоставляет REST API для управления постами, комментариями, лайками и изображениями постов.

## Стек технологий

- Java 21
- Spring Framework 6.x (Web MVC, JDBC, Validation)
- Apache Tomcat 10.1
- PostgreSQL 16
- Maven
- MapStruct
- Jackson
- JUnit 5, Spring Test Framework, Mockito
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

### Конфигурация приложения (`configuration`)

- Web MVC
- JDBC и транзакции
- Валидация
- CORS
- Загрузка и разрешение property-файлов

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

- JUnit 5
- Spring Test Framework
- MockMvc (MVC-тесты)
- Mockito
- Testcontainers (PostgreSQL)

Покрыты:
- REST-контроллеры
- Сервисы
- DAO (репозитории)

Контексты тестов кешируются для ускорения прогона.

## Сборка и запуск через Docker

1. Создать файл `.env` на основе `.env.example`
2. Запустить:

```bash
docker compose up --build
```

* PostgreSQL будет доступен на `localhost:5432`
* Приложение — на `http://localhost:8080`

Изображения постов сохраняются в Docker volume.

## Особенности реализации

* Spring Framework **без Spring Boot**
* Явная конфигурация через Java Config и `web.xml`
* Явные SQL-запросы без ORM
* MapStruct для маппинга
* Файловое хранилище изображений
