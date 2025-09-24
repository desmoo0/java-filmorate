package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaStorage storage;

    public List<Mpa> findAll() {
        return storage.findAll();
    }

    public Mpa findById(int id) {
        return storage.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("MPA not found: " + id));
    }
}
