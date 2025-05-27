package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.film.Mpa;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class InMemoryMpaStorage {
    private final Map<Integer, Mpa> ratings = new HashMap<>();

    public InMemoryMpaStorage() {
        ratings.put(1, new Mpa(1)); ratings.get(1).setName("G");
        ratings.put(2, new Mpa(2)); ratings.get(2).setName("PG");
        ratings.put(3, new Mpa(3)); ratings.get(3).setName("PG-13");
        ratings.put(4, new Mpa(4)); ratings.get(4).setName("R");
        ratings.put(5, new Mpa(5)); ratings.get(5).setName("NC-17");
    }

    public Collection<Mpa> findAll() {
        return ratings.values();
    }

    public Mpa findById(int id) {
        Mpa m = ratings.get(Optional.of(id));
        if (m == null) throw new NoSuchElementException("Mpa with id=" + id + " not found");
        return m;
    }
}
