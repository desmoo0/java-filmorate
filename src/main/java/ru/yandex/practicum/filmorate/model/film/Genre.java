package ru.yandex.practicum.filmorate.model.film;

import lombok.Getter;

import java.util.List;
import java.util.Objects;

@Getter
public class Genre {
    private final int id;
    private final String name;

    public Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static final Genre COMEDY       = new Genre(1, "Комедия");
    public static final Genre DRAMA        = new Genre(2, "Драма");
    public static final Genre ANIMATION    = new Genre(3, "Мультфильм");
    public static final Genre THRILLER     = new Genre(4, "Триллер");
    public static final Genre DOCUMENTARY  = new Genre(5, "Документальный");
    public static final Genre ACTION       = new Genre(6, "Боевик");

    public static List<Genre> values() {
        return List.of(COMEDY, DRAMA, ANIMATION, THRILLER, DOCUMENTARY, ACTION);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Genre)) return false;
        Genre genre = (Genre) o;
        return id == genre.id && Objects.equals(name, genre.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
