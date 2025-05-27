package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.film.Film;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Film create(Film film) {
        long id = idGenerator.getAndIncrement();
        film.setId(id);
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<Long>());
        }
        films.put(id, film);
        return film;
    }

    @Override
    public Optional<Film> getById(long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public List<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film update(Film film) {
        films.put(film.getId(), film);
        return film;
    }
}
