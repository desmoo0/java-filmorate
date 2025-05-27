package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.film.MpaRating;
import java.util.List;

public interface MpaRatingStorage {
    List<MpaRating> findAll();

    MpaRating findById(int id);
}
