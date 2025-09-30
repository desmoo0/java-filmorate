package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.function.FilmStorage;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage films;
    private final GenreService genres;
    private final MpaService mpas;
    private final UserService users;

    public FilmService(FilmStorage films,
                       GenreService genres,
                       MpaService mpas,
                       UserService users) {
        this.films = films;
        this.genres = genres;
        this.mpas = mpas;
        this.users = users;
    }

    @Transactional
    public Film create(Film film) {
        enrichRefs(film);
        Film created = films.create(film);
        log.info("Created film {} with genres: {}", created.getId(), created.getGenres());
        return created;
    }

    @Transactional
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Film " + film.getId());
        }
        enrichRefs(film);
        Film updated = films.update(film);
        log.info("Updated film {} with genres: {}", updated.getId(), updated.getGenres());
        return updated;
    }

    public List<Film> findAll() {
        return films.findAll();
    }

    public Film findById(Long id) {
        return films.findById(id).orElseThrow(() -> new NotFoundException("Film " + id));
    }

    public void addLike(Long filmId, Long userId) {
        users.findById(userId);
        films.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        users.findById(userId);
        films.removeLike(filmId, userId);
    }

    public List<Film> getTopFilms(int count) {
        return films.findPopular(count);
    }

    private void enrichRefs(Film film) {
        film.setMpa(mpas.findById(film.getMpa().getId()));

        Set<Genre> resolvedGenres = film.getGenres().stream()
                .map(g -> genres.findById(g.getId()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        film.setGenres(resolvedGenres);
        log.debug("Enriched film with genres: {}", resolvedGenres);
    }
}
