package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class})
class FilmDbStorageIT {

    private final FilmDbStorage filmDbStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void seedDictionaries() {
        jdbcTemplate.update("MERGE INTO MPA (ID, NAME) KEY(ID) VALUES (1, 'G')");
        jdbcTemplate.update("MERGE INTO MPA (ID, NAME) KEY(ID) VALUES (2, 'PG-13')");
        jdbcTemplate.update("MERGE INTO GENRE (ID, NAME) KEY(ID) VALUES (1, 'Комедия')");
        jdbcTemplate.update("MERGE INTO GENRE (ID, NAME) KEY(ID) VALUES (2, 'Драма')");

        // Пользователи для лайков (на случай внешних ключей)
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY) VALUES ('u1@x','u1','U1', DATE '1990-01-01')");
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY) VALUES ('u2@x','u2','U2', DATE '1991-01-01')");
        jdbcTemplate.update("INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY) VALUES ('u3@x','u3','U3', DATE '1992-01-01')");
    }

    @Test
    void create_find_update_and_containsKey() {
        Film film = new Film();
        film.setName("Test");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2001, 1, 1));
        film.setDuration(100);
        film.setMpa(new Mpa(1, null));
        Set<Genre> genres = new LinkedHashSet<>();
        genres.add(new Genre(1, null));
        film.setGenres(genres);

        film = filmDbStorage.create(film);
        assertThat(film.getId()).isPositive();
        assertThat(filmDbStorage.containsKey(film.getId())).isTrue();

        Optional<Film> foundFilm = filmDbStorage.findById(film.getId());
        assertThat(foundFilm).isPresent();
        assertThat(foundFilm.get().getMpa().getName()).isEqualTo("G"); // имя подхватилось из БД
        assertThat(foundFilm.get().getGenres()).extracting(Genre::getId).containsExactly(1);

        // обновим жанры и длительность
        film.setDuration(120);
        Set<Genre> updatedGenres = new LinkedHashSet<>();
        updatedGenres.add(new Genre(1, null));
        updatedGenres.add(new Genre(2, null));
        film.setGenres(updatedGenres);

        Film updatedFilm = filmDbStorage.update(film);
        assertThat(updatedFilm.getDuration()).isEqualTo(120);
        assertThat(updatedFilm.getGenres()).extracting(Genre::getId).containsExactly(1, 2);
    }

    @Test
    void findAll_and_popular() {
        // фильм 1
        Film filmA = new Film();
        filmA.setName("A");
        filmA.setDescription("d");
        filmA.setReleaseDate(LocalDate.of(2000, 1, 1));
        filmA.setDuration(90);
        filmA.setMpa(new Mpa(1, null));
        filmA.setGenres(new LinkedHashSet<>(List.of(new Genre(1, null))));
        filmA = filmDbStorage.create(filmA);

        // фильм 2
        Film filmB = new Film();
        filmB.setName("B");
        filmB.setDescription("d");
        filmB.setReleaseDate(LocalDate.of(2000, 2, 1));
        filmB.setDuration(95);
        filmB.setMpa(new Mpa(2, null));
        filmB.setGenres(new LinkedHashSet<>(List.of(new Genre(2, null))));
        filmB = filmDbStorage.create(filmB);

        List<Film> allFilms = filmDbStorage.findAll();
        assertThat(allFilms.size()).isGreaterThanOrEqualTo(2);

        filmDbStorage.addLike(filmB.getId(), 1L);
        filmDbStorage.addLike(filmB.getId(), 2L);
        filmDbStorage.addLike(filmA.getId(), 1L);

        List<Film> popularFilms = filmDbStorage.findPopular(10);
        assertThat(popularFilms).isNotEmpty();
        assertThat(popularFilms.get(0).getId()).isEqualTo(filmB.getId());
    }
}
