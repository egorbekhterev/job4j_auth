# job4j_auth

## Описание проекта

* Проект для ознакомления с принципами работы REST архитектуры. Для контроллера реализованы основные HTTP методы.
* Добавлена авторизация с помощью JSON WEB TOKEN. Продемонстрированы основные способы обработки исключений.
* Отражены основные способы построения ответов HTTP запроса, работа с DTO, валидация.

## Стек технологий

- **Java 17.0.2**
- **Spring Boot 2.7.8**
- **Spring Data JPA 2.7.8**
- **Spring Security 5.7.6**
- **JWT 3.4.0**
- **Lombok 1.18.24**
- **PostgreSQL 15**
- **Liquibase 4.15.0**
- **H2DB 2.1.214**
- **Junit 5.8.2**
- **Mockito 4.5.1**
- **Maven 3.8.1**

## Требования к окружению

- **Java 17.0.2**
- **Maven 3.8.1**
- **PostgresSQL 15**
- **Postman**

## Сборка и запуск

- **Создать БД**

``` 
create database fullstack_auth;
```

- **Запустить проект по команде**

``` 
mvn spring-boot:run -Pproduction
```

- **Использовать Postman или другой API для выполнения запросов.**

``` 
http://localhost:8080/
```

## Контакты для связи
telegram: <a href="https://t.me/bekhterev_egor" target="blank">@bekhterev_egor</a>
