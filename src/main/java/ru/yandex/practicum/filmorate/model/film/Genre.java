package ru.yandex.practicum.filmorate.model.film;

import lombok.Getter;

@Getter
public enum Genre {
    COMEDY(1, "Комедия"),
    DRAMA(2, "Драма"),
    ANIMATION(3, "Мультфильм"),
    THRILLER(4, "Триллер"),
    DOCUMENTARY(5, "Документальный"),
    ACTION(6, "Боевик");

    private final long id;
    private final String displayName;

    Genre(long id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

}
