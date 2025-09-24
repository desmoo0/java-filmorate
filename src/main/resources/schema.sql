-- Вариант без очистки, чтобы не терять данные.
-- Если нужно пересоздавать схему на каждом запуске — добавь DROP TABLE IF EXISTS в обратном порядке зависимостей.

-- ===== Словари =====
CREATE TABLE mpa (
    id   INTEGER PRIMARY KEY,
    name VARCHAR(64) NOT NULL
);

CREATE TABLE genre (
    id   INTEGER PRIMARY KEY,
    name VARCHAR(64) NOT NULL
);

-- ===== Пользователи =====
CREATE TABLE users (
    id        BIGSERIAL PRIMARY KEY,
    email     VARCHAR(255) NOT NULL,
    login     VARCHAR(64)  NOT NULL,
    name      VARCHAR(255),
    birthday  DATE         NOT NULL,
    CONSTRAINT uq_users_email UNIQUE (email)
);

-- ===== Фильмы =====
CREATE TABLE films (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    description   VARCHAR(200),
    release_date  DATE         NOT NULL,
    duration      INTEGER      NOT NULL,
    mpa_id        INTEGER      NOT NULL,
    CONSTRAINT chk_films_duration_positive CHECK (duration > 0),
    CONSTRAINT fk_films_mpa FOREIGN KEY (mpa_id) REFERENCES mpa (id)
);

CREATE INDEX idx_films_mpa_id ON films (mpa_id);

-- ===== Связь фильмов с жанрами (многие-ко-многим) =====
CREATE TABLE film_genres (
    film_id  BIGINT  NOT NULL,
    genre_id INTEGER NOT NULL,
    CONSTRAINT pk_film_genres PRIMARY KEY (film_id, genre_id),
    CONSTRAINT fk_film_genres_film  FOREIGN KEY (film_id)  REFERENCES films (id) ON DELETE CASCADE,
    CONSTRAINT fk_film_genres_genre FOREIGN KEY (genre_id) REFERENCES genre (id) ON DELETE CASCADE
);

CREATE INDEX idx_film_genres_film_id  ON film_genres (film_id);
CREATE INDEX idx_film_genres_genre_id ON film_genres (genre_id);

-- ===== Лайки фильмов пользователями =====
CREATE TABLE film_likes (
    film_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT pk_film_likes PRIMARY KEY (film_id, user_id),
    CONSTRAINT fk_film_likes_film FOREIGN KEY (film_id) REFERENCES films (id) ON DELETE CASCADE,
    CONSTRAINT fk_film_likes_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_film_likes_film_id ON film_likes (film_id);
CREATE INDEX idx_film_likes_user_id ON film_likes (user_id);

-- ===== Дружба (односторонняя) =====
CREATE TABLE friendships (
    user_id   BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    CONSTRAINT pk_friendships PRIMARY KEY (user_id, friend_id),
    CONSTRAINT fk_friendships_user   FOREIGN KEY (user_id)   REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_friendships_friend FOREIGN KEY (friend_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_friendships_self CHECK (user_id <> friend_id)
);

CREATE INDEX idx_friendships_user_id   ON friendships (user_id);
CREATE INDEX idx_friendships_friend_id ON friendships (friend_id);
