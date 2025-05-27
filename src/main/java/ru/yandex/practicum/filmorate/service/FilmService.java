package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ExistingMovieException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public void addLike(Long filmId, Long userId) {
        Film film = findById(filmId);
        userService.findById(userId);
        film.getLikes().add(userId);
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = findById(filmId);
        userService.findById(userId);
        film.getLikes().remove(userId);
    }

    public List<Film> getTopFilms(int count) {
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt(f -> -f.getLikes().size()))
                .limit(count)
                .toList();
    }

    public Film create(Film film) {
        if (film.getId() != null && filmStorage.findAll().stream()
                .anyMatch(f -> f.getId().equals(film.getId()))) {
            throw new ExistingMovieException("Фильм с таким ID уже существует.");
        }

        if (filmStorage.findAll().stream()
                .anyMatch(f -> f.getName().equalsIgnoreCase(film.getName()))) {
            throw new ExistingMovieException("Фильм с таким названием уже существует.");
        }

        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (!filmStorage.containsKey(film.getId())) {
            throw new NoSuchElementException("Фильм с указанным ID не найден.");
        }
        return filmStorage.update(film);
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Фильм с id=" + id + " не найден"));
    }
}
