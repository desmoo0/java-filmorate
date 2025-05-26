# Filmorate

## Структура проекта

* **model**: Java-классы сущностей (`Film`, `User`, `Genre`, `MpaRating`, `Friendship`, `FriendshipStatus`)
* **storage**: интерфейсы и их реализации для работы с данными
* **service**: реализация бизнес-логики (лайки, подтверждение дружбы, топ-фильмы и т.п.)
* **controller**: REST-контроллеры для взаимодействия с API

## Схема базы данных

![Untitled](https://github.com/user-attachments/assets/ad64f252-dda3-4174-b3e0-98b766737c0e)

## Основные таблицы и связи

* **users**: хранит пользователей приложения
* **films**: хранит информацию о фильмах, включая `rating_id` (MPA)
* **mpa\_ratings**: справочник возрастных рейтингов (G, PG, PG-13, R, NC-17)
* **genres**: справочник жанров фильмов
* **film\_genre**: связующая таблица многих-ко-многим между фильмами и жанрами
* **likes**: хранит лайки пользователей к фильмам
* **friends**: хранит связи дружбы между пользователями с полем `status` (`unconfirmed` или `confirmed`)

## Примеры SQL-запросов

### Получение списка всех фильмов

```sql
SELECT *
FROM films;
```

### Топ 10 популярных фильмов

```sql
SELECT f.*, COUNT(l.user_id) AS likes_count
  FROM films AS f
  LEFT JOIN likes AS l ON f.film_id = l.film_id
 GROUP BY f.film_id
 ORDER BY likes_count DESC
 LIMIT 10;
```

### Список друзей пользователя

```sql
SELECT *
FROM users
WHERE user_id IN (SELECT friend_id
                   FROM friends
                   WHERE user_id = /* подставить айди */
                   AND status = 'confirmed'
                 );
```
