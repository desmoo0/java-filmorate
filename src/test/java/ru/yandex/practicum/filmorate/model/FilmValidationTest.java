package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.*;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FilmValidationTest {

    private Validator validator;

    @BeforeEach
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldFailValidationWhenNameIsBlank() {
        Film film = new Film();
        film.setName("   ");
        film.setDescription("Описание");
        film.setDuration(100);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenDescriptionTooLong() {
        Film film = new Film();
        film.setName("Название");
        film.setDescription("a".repeat(201)); // > 200
        film.setDuration(100);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenDurationIsNegative() {
        Film film = new Film();
        film.setName("Название");
        film.setDescription("Описание");
        film.setDuration(-90);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldThrowWhenReleaseDateBefore1895() {
        Film film = new Film();
        film.setName("Название");
        film.setDescription("Описание");
        film.setDuration(120);
        film.setReleaseDate(LocalDate.of(1800, 1, 1));

        assertThrows(IllegalArgumentException.class, film::validate);
    }

    @Test
    void shouldPassValidationWithCorrectFilm() {
        Film film = new Film();
        film.setName("Тест");
        film.setDescription("Описание");
        film.setDuration(90);
        film.setReleaseDate(LocalDate.of(2020, 1, 1));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
        assertDoesNotThrow(film::validate);
    }
}
