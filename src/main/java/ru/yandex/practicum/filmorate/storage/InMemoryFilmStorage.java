package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ExistingMovieException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long currentId = 1;

    @Override
    public Film create(Film film) {
        if (film.getId() != null && films.containsKey(film.getId())) {
            throw new ExistingMovieException("Фильм с таким ID уже существует.");
        }

        if (films.values().stream().anyMatch(existing -> existing.getName().equalsIgnoreCase(film.getName()))) {
            throw new ExistingMovieException("Фильм с таким названием уже существует.");
        }

        film.setId(currentId++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NoSuchElementException("Фильм с указанным ID не найден.");
        }

        films.put(film.getId(), film);
        return film;
    }

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }
}
