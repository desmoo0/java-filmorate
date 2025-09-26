package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    List<Film> findAll();

    Optional<Film> findById(Long id);

    boolean containsKey(Long id);

    void addLike(long filmId, long userId);

    void removeLike(long filmId, long userId);

    List<Film> findPopular(int count);
}
