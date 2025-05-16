package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ExistingMovieException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        if (films.containsKey(film.getId())) {
            throw new ExistingMovieException("Фильм с таким ID уже существует.");
        } else {
            Long id = films.size() + 1L;
            film.setId(id);
        }
        if (films.values().stream()
                .anyMatch(existingFilm -> existingFilm.getName().equalsIgnoreCase(film.getName()))) {
            throw new ExistingMovieException("Фильм с таким названием уже существует.");
        }
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        if (!films.containsKey(film.getId())) {
            throw new ExistingMovieException("Фильм с указанным ID не найден.");
        }
        films.put(film.getId(), film);
        return film;
    }

    @GetMapping
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }
}
