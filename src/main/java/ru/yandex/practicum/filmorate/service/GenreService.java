package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage storage;

    public List<Genre> findAll() {
        return storage.findAll();
    }

    public Genre findById(int id) {
        return storage.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Genre not found: " + id));
    }
}
