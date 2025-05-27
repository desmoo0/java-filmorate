package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.film.Genre;

import java.util.List;
import java.util.Optional;

public interface GenreStorage {
    Optional<Genre> getById(int id);

    List<Genre> getAll();
}
