package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class DictController {
    private final GenreStorage storage;
    private final MpaService mpaService;

    @GetMapping("/genres")
    public List<Genre> getGenres() {
        return storage.findAll();
    }

    @GetMapping("/genres/{id}")
    public Genre getGenre(@PathVariable int id) {
        return storage.findById(id).orElseThrow();
    }

    @GetMapping("/mpa")
    public List<Mpa> getAllMpa() {
        return mpaService.findAll();
    }

    @GetMapping("/mpa/{id}")
    public Mpa getMpa(@PathVariable int id) {
        return mpaService.findById(id);
    }
}
