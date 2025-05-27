package ru.yandex.practicum.filmorate.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryMpaStorage;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreStorage genreStorage;
    private final InMemoryMpaStorage mpaStorage;

    public FilmService(FilmStorage fs, UserService us, GenreStorage gs, InMemoryMpaStorage ms) {
        this.filmStorage = fs;
        this.userService = us;
        this.genreStorage = gs;
        this.mpaStorage = ms;
    }

    public Film create(Film film) {
        validateFilm(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (filmStorage.getById(film.getId()).isEmpty()) {
            throw new NoSuchElementException("Фильм с id=" + film.getId() + " не найден");
        }
        validateFilm(film);
        return filmStorage.update(film);
    }

    private void validateFilm(Film film) {
        // проверяем MPA
        Mpa m = mpaStorage.findById(film.getMpa().getId());
        film.setMpa(m);
        // проверяем и нормализуем жанры
        Set<Genre> ok = film.getGenres().stream()
                .map(g -> genreStorage.getById(g.getId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "Жанр с id=" + g.getId() + " не найден")))
                .collect(Collectors.toSet());
        film.setGenres(ok);
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
