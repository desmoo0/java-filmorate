package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.film.Mpa;
import ru.yandex.practicum.filmorate.storage.InMemoryMpaStorage;

import java.util.List;

@RestController
@RequestMapping("/mpa")
public class MpaController {
    private final InMemoryMpaStorage storage;

    public MpaController(InMemoryMpaStorage storage) {
        this.storage = storage;
    }

    @GetMapping
    public List<Mpa> getAll() {
        return List.copyOf(storage.findAll());
    }

    @GetMapping("/{id}")
    public Mpa getById(@PathVariable int id) {
        return storage.findById(id);
    }
}
