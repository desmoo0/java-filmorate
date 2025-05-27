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

    public FilmService(FilmStorage filmStorage,
                       UserService userService,
                       GenreStorage genreStorage,
                       InMemoryMpaStorage mpaStorage) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
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
        try {
            Mpa mpa = mpaStorage.findById(film.getMpa().getId());
            film.setMpa(mpa);
        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "MPA-рейтинг с id=" + film.getMpa().getId() + " не найден"
            );
        }

        // Проверка и нормализация жанров
        Set<Genre> normalized = film.getGenres().stream()
                .map(g -> genreStorage.getById(g.getId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Жанр с id=" + g.getId() + " не найден"
                        ))
                )
                .collect(Collectors.toSet());
        film.setGenres(normalized);
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
