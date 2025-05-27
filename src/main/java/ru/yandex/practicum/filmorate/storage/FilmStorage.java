package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.film.Film;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    Optional<Film> getById(long id);

    List<Film> getAll();
}
