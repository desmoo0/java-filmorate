package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.function.GenreStorage;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GenreController {
    private final GenreStorage storage;

    @GetMapping("/genres")
    public List<Genre> getGenres() {
        return storage.findAll();
    }

    @GetMapping("/genres/{id}")
    public Genre getGenre(@PathVariable int id) {
        return storage.findById(id)
                .orElseThrow(() -> new NotFoundException("Genre " + id));
    }
}
