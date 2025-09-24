package ru.yandex.practicum.filmorate.storage.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(GenreDbStorage.class)
class GenreDbStorageIT {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    GenreDbStorage genreDbStorage;

    @BeforeEach
    void setUp() {
        // Схема подтягивается из src/main/resources/schema.sql
        jdbcTemplate.update("DELETE FROM GENRE");
        jdbcTemplate.update("INSERT INTO GENRE (ID, NAME) VALUES (1,'Комедия')");
        jdbcTemplate.update("INSERT INTO GENRE (ID, NAME) VALUES (2,'Драма')");
        jdbcTemplate.update("INSERT INTO GENRE (ID, NAME) VALUES (3,'Мультфильм')");
        jdbcTemplate.update("INSERT INTO GENRE (ID, NAME) VALUES (4,'Триллер')");
        jdbcTemplate.update("INSERT INTO GENRE (ID, NAME) VALUES (5,'Документальный')");
        jdbcTemplate.update("INSERT INTO GENRE (ID, NAME) VALUES (6,'Боевик')");
    }

    @Test
    void findAll_returnsAllGenres_sortedByIdAsc() {
        List<ru.yandex.practicum.filmorate.model.Genre> genres = genreDbStorage.findAll();
        assertThat(genres).hasSize(6);
        assertThat(genres.stream().map(ru.yandex.practicum.filmorate.model.Genre::getId))
                .containsExactly(1, 2, 3, 4, 5, 6);
        assertThat(genres.get(0).getName()).isEqualTo("Комедия");
        assertThat(genres.get(2).getName()).isEqualTo("Мультфильм");
    }

    @Test
    void findById_existing_returnsGenre() {
        Optional<ru.yandex.practicum.filmorate.model.Genre> g = genreDbStorage.findById(3);
        assertThat(g).isPresent();
        assertThat(g.get().getId()).isEqualTo(3);
        assertThat(g.get().getName()).isEqualTo("Мультфильм");
    }

    @Test
    void findById_missing_returnsEmpty() {
        Optional<ru.yandex.practicum.filmorate.model.Genre> g = genreDbStorage.findById(999);
        assertThat(g).isEmpty();
    }
}
