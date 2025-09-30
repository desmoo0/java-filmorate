package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long currentId = 1L;

    @Override
    public Film create(Film film) {
        film.setId(currentId++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
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

    @Override
    public boolean containsKey(Long id) {
        return films.containsKey(id);
    }

    @Override
    public void addLike(long filmId, long userId) {
        Film film = films.get(filmId);
        if (film != null) {
            film.addLike(userId);
        }
    }

    @Override
    public void removeLike(long filmId, long userId) {
        Film film = films.get(filmId);
        if (film != null) {
            film.removeLike(userId);
        }
    }

    @Override
    public List<Film> findPopular(int count) {
        return films.values().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
