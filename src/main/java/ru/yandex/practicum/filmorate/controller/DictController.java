package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class DictController {
    private final GenreService genreService;
    private final MpaService mpaService;

    @GetMapping("/genres")
    public List<Genre> getGenres() { return genreService.findAll(); }

    @GetMapping("/genres/{id}")
    public Genre getGenre(@PathVariable int id) { return genreService.findById(id); }

    @GetMapping("/mpa")
    public List<Mpa> getAllMpa() { return mpaService.findAll(); }

    @GetMapping("/mpa/{id}")
    public Mpa getMpa(@PathVariable int id) { return mpaService.findById(id); }
}
