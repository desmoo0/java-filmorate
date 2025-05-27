package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (filmStorage.getById(film.getId()).isEmpty()) {
            throw new NoSuchElementException("Фильм с id=" + film.getId() + " не найден");
        }
        return filmStorage.update(film);
    }

    public Film getById(long id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new NoSuchElementException("Фильм с id=" + id + " не найден"));
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public void addLike(long filmId, long userId) {
        userService.findById(userId);
        Film film = getById(filmId);
        film.getLikes().add(userId);
        filmStorage.update(film);
    }

    public void removeLike(long filmId, long userId) {
        userService.findById(userId);
        Film film = getById(filmId);
        film.getLikes().remove(userId);
        filmStorage.update(film);
    }

    public List<Film> getTopFilms(int count) {
        return filmStorage.getAll().stream()
                .sorted((a, b) -> Integer.compare(b.getLikes().size(), a.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
