# Fitness Tracker API

## Описание

Необходимо разработать backend-приложение для трекинга фитнес-активностей, которое позволит пользователям записывать тренировки, отслеживать прогресс и анализировать показатели.

## Основные эндпоинты

1. GET /workouts – Получение списка всех тренировок пользователя. Поддержка фильтров, сортировки, пагинации.
2. GET /workouts/{id} – Получение конкретной тренировки по ID.
3. POST /workouts – Добавление новой тренировки.
4. PUT /workouts/{id} – Обновление данных о тренировке.
5. DELETE /workouts/{id} – Удаление тренировки.
6. POST /media – Загрузка фото прогресса (с хранением в файловой системе, облаке или хранить бинарные данные прямо в PostgreSQL (тип BYTEA)).

## Дополнительный функционал

1. JWT-аутентификация
   Авторизация через Bearer Token.
   Refresh Token для продления сессии.

2. Валидация данных
   Использовать javax.validation или Spring Validation.
   Примеры:
   Название тренировки: не пустое, длина ≤ 100 символов.
   Дата: прошедшая дата.
   Длительность: > 0 минут.
   Калории: > 0.

3. Расширенный поиск
   1. Фильтры:
      Тип тренировки (cardio, strength, yoga и т.д.)
      Дата (диапазон дат)
      Длительность (от/до)
   2. Сортировка:
      По дате (ASC/DESC)
      По калориям (ASC/DESC)
   3. Пагинация:
      page, size параметры.
      Ответ с totalElements, totalPages.

## Технические требования

1. RESTful API с корректными статус-кодами.
2. Глобальная обработка ошибок через @ControllerAdvice.
3. Docker контейнеризация:
   Dockerfile для приложения.
   docker-compose.yml для запуска вместе с PostgreSQL.
4. Чистый код с соблюдением SOLID, c разделением слоёв (Controller, Service, Repository).
5. Использовать DTO для запросов/ответов.
6. Swagger документация:
   Описание всех эндпоинтов. Для каждого эндпоинта:
   summary (1 строка) и короткое description
   tags
   Параметры: path, query (в т.ч. page, size, sort)
   RequestBody: схема DTO + example
   Responses: как минимум 200/201, 400 (валидация), 401/403 (безопасность), 404, 409 (конфликты), 500 — со схемой ErrorResponse и примерами
   Контент-тайпы: application/json, для загрузки — multipart/form-data
   Примеры запросов/ответов.
   Авторизация через JWT.
7. Следование GitFlow.  
8. Использование Conventional Commits.

## Тестирование

Реализовать тестирование c полным покрытием функционала приложения:

1.  Использовать JUnit 5 для модульных тестов.
2.  Минимальное покрытие: 70%+ по бизнес-логике.

## Стек технологий

1. Java 17+ – современная версия языка.
2. Spring Boot – быстрый старт и конфигурация.
3. Spring MVC – реализация REST API.
4. Hibernate + Spring Data JPA – ORM для работы с БД.
5. PostgreSQL – реляционная база данных.
6. JUnit 5 – модульное тестирование.
7. Swagger (OpenAPI) – генерация документации API.
8. Docker – контейнеризация приложения и зависимостей. Позволяет запускать приложение в изолированной среде.

**ВАЖНО!** Реализация должна находиться на **приватном** репозитории, на который необходимо добавить <code>modsen-mentor</code> аккаунт

## Полезные источники
- [Java](https://metanit.com/java/tutorial/)
- [Hibernate](https://hibernate.org/orm/documentation/7.1/)
- [PostgreSQL](https://www.postgresql.org/docs/)
- [Spring Framework](https://docs.spring.io/spring-framework/reference/index.html)
- [JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [JUnit](https://docs.junit.org/current/user-guide/)
- [Docker](https://www.docker.com/)
- [GitFlow](https://www.atlassian.com/ru/git/tutorials/comparing-workflows/gitflow-workflow)
- [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/)

