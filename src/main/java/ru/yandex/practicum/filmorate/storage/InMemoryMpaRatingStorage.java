package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.film.MpaRating;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryMpaRatingStorage implements MpaRatingStorage {

    private final Map<Integer, MpaRating> ratings;

    public InMemoryMpaRatingStorage() {
        ratings = Arrays.stream(MpaRating.values())
                .collect(Collectors.toMap(MpaRating::getId, r -> r));
    }

    @Override
    public List<MpaRating> findAll() {
        return new ArrayList<>(ratings.values());
    }

    @Override
    public MpaRating findById(int id) {
        MpaRating rating = ratings.get(id);
        if (rating == null) {
            throw new NoSuchElementException("MPA-рейтинг с id=" + id + " не найден");
        }
        return rating;
    }
}
