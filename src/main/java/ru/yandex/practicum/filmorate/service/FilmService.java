package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmStorage films;
    private final UserService userService; // используем сервис пользователей для валидации существования
    private final GenreStorage genres;     // могут быть null в юнит-тестах
    private final MpaStorage mpas;         // могут быть null в юнит-тестах

    /** Конструктор для юнит-тестов без Spring-контекста. */
    public FilmService(FilmStorage films, UserService userService) {
        this(films, userService, null, null);
    }

    /** Базовый конструктор. Если genres/mpas == null — просто не обогащаем ссылки. */
    public FilmService(FilmStorage films, UserService userService, GenreStorage genres, MpaStorage mpas) {
        this.films = Objects.requireNonNull(films, "films");
        this.userService = Objects.requireNonNull(userService, "userService");
        this.genres = genres;
        this.mpas = mpas;
    }

    /**
     * Конструктор для Spring. Явно просим именно DB-реализации,
     * чтобы не было коллизии с InMemory бинами.
     */
    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage films,
                       @Qualifier("userDbStorage") UserStorage users,
                       @Qualifier("genreDbStorage") GenreStorage genres,
                       @Qualifier("mpaDbStorage") MpaStorage mpas) {
        this.films = Objects.requireNonNull(films, "films");
        // Лёгкая обёртка над стораджем — отдельный бин UserService нам не обязателен
        this.userService = new UserService(users);
        this.genres = genres;
        this.mpas = mpas;
    }

    // =========== CRUD ===========

    public Film create(Film film) {
        Film toSave = enrichRefs(film);
        return films.create(toSave);
    }

    public Film update(Film film) {
        // проверим, что фильм существует — чтобы получить ожидаемую ошибку при апдейте несуществующего
        findById(requiredId(film));
        Film toSave = enrichRefs(film);
        return films.update(toSave);
    }

    public List<Film> findAll() {
        return films.findAll();
    }

    public Film findById(Long id) {
        return films.findById(id).orElseThrow(() ->
                new NoSuchElementException("Фильм не найден: " + id));
    }

    // =========== Лайки / популярность ===========

    /**
     * Добавляет лайк. Возвращает true при фактическом добавлении (идемпотентно).
     */
    public void addLike(Long filmId, Long userId) {
        Film film = findById(filmId);
        // бросит NoSuchElementException с текстом "не найден", если пользователя нет
        userService.findById(userId);

        Set<Long> likes = film.getLikes();
        if (likes == null) {
            likes = new LinkedHashSet<>();
            film.setLikes(likes);
        }
        boolean added = likes.add(userId);
        films.update(film); // синхронизация состояния
    }

    /**
     * Удаляет лайк. Возвращает true при фактическом удалении.
     */
    public void removeLike(Long filmId, Long userId) {
        Film film = findById(filmId);
        Set<Long> likes = film.getLikes();
        boolean removed = likes != null && likes.remove(userId);
        films.update(film);
    }

    /** Топ N фильмов по количеству лайков (по убыванию). */
    public List<Film> getTopFilms(int count) {
        if (count <= 0) return List.of();
        return films.findAll().stream()
                .sorted(Comparator
                        .comparingInt((Film f) -> f.getLikes() == null ? 0 : f.getLikes().size())
                        .reversed()
                        .thenComparing(Film::getId))
                .limit(count)
                .collect(Collectors.toList());
    }

    // =========== Вспомогательные ===========

    private Film enrichRefs(Film film) {
        if (film == null) return null;

        // Обогащаем MPA из справочника, если возможно
        if (mpas != null && film.getMpa() != null) {
            Mpa full = mpas.findById(film.getMpa().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Неизвестный рейтинг MPA: " + film.getMpa().getId()));
            film.setMpa(full);
        }

        // Обогащаем жанры из справочника, если возможно
        if (genres != null && film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> resolved = film.getGenres().stream()
                    .map(g -> genres.findById(g.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Неизвестный жанр: " + g.getId())))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            film.setGenres(resolved);
        }
        return film;
    }

    private static Long requiredId(Film film) {
        if (film == null || film.getId() == null) {
            throw new IllegalArgumentException("Идентификатор фильма не задан");
        }
        return film.getId();
    }
}
