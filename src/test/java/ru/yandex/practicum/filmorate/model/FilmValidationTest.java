package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.validation.*;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.MpaRating;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FilmValidationTest {

    private Validator validator;

    @BeforeEach
    public void setup() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private Film makeValidFilm() {
        Film film = new Film();
        film.setName("Название");
        film.setDescription("Описание");
        film.setDuration(100);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setGenres(Set.of(Genre.COMEDY));
        film.setMpaRating(MpaRating.G);
        return film;
    }

    @Test
    void shouldFailValidationWhenNameIsBlank() {
        Film film = makeValidFilm();
        film.setName("   ");

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenDescriptionTooLong() {
        Film film = makeValidFilm();
        film.setDescription("a".repeat(201));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenDurationIsNegative() {
        Film film = makeValidFilm();
        film.setDuration(-90);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenReleaseDateBefore1895() {
        Film film = makeValidFilm();
        film.setReleaseDate(LocalDate.of(1800, 1, 1));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenGenresEmpty() {
        Film film = makeValidFilm();
        film.setGenres(Set.of());

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenMpaRatingNull() {
        Film film = makeValidFilm();
        film.setMpaRating(null);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldPassValidationWithCorrectFilm() {
        Film film = makeValidFilm();
        film.setName("Тест");
        film.setDescription("Описание");
        film.setDuration(90);
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setGenres(Set.of(Genre.DRAMA, Genre.COMEDY));
        film.setMpaRating(MpaRating.PG_13);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }
}
