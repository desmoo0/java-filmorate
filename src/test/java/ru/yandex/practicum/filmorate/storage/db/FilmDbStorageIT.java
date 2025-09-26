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

    private final FilmDbStorage films;
    private final JdbcTemplate jdbc;

    @BeforeEach
    void seedDicts() {
        jdbc.update("MERGE INTO MPA (ID, NAME) KEY(ID) VALUES (1, 'G')");
        jdbc.update("MERGE INTO MPA (ID, NAME) KEY(ID) VALUES (2, 'PG-13')");
        jdbc.update("MERGE INTO GENRE (ID, NAME) KEY(ID) VALUES (1, 'Комедия')");
        jdbc.update("MERGE INTO GENRE (ID, NAME) KEY(ID) VALUES (2, 'Драма')");

        // Пользователи для лайков (на случай внешних ключей)
        jdbc.update("INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY) VALUES ('u1@x','u1','U1', DATE '1990-01-01')");
        jdbc.update("INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY) VALUES ('u2@x','u2','U2', DATE '1991-01-01')");
        jdbc.update("INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY) VALUES ('u3@x','u3','U3', DATE '1992-01-01')");
    }

    @Test
    void create_find_update_and_containsKey() {
        Film f = new Film();
        f.setName("Test");
        f.setDescription("Desc");
        f.setReleaseDate(LocalDate.of(2001, 1, 1));
        f.setDuration(100);
        f.setMpa(new Mpa(1, null));
        Set<Genre> g = new LinkedHashSet<>();
        g.add(new Genre(1, null));
        f.setGenres(g);

        f = films.create(f);
        assertThat(f.getId()).isPositive();
        assertThat(films.containsKey(f.getId())).isTrue();

        Optional<Film> byId = films.findById(f.getId());
        assertThat(byId).isPresent();
        assertThat(byId.get().getMpa().getName()).isEqualTo("G"); // имя подхватилось из БД
        assertThat(byId.get().getGenres()).extracting(Genre::getId).containsExactly(1);

        // обновим жанры и длительность
        f.setDuration(120);
        Set<Genre> newGenres = new LinkedHashSet<>();
        newGenres.add(new Genre(1, null));
        newGenres.add(new Genre(2, null));
        f.setGenres(newGenres);

        Film updated = films.update(f);
        assertThat(updated.getDuration()).isEqualTo(120);
        assertThat(updated.getGenres()).extracting(Genre::getId).containsExactly(1, 2);
    }

    @Test
    void findAll_and_popular() {
        // фильм 1
        Film a = new Film();
        a.setName("A");
        a.setDescription("d");
        a.setReleaseDate(LocalDate.of(2000, 1, 1));
        a.setDuration(90);
        a.setMpa(new Mpa(1, null));
        a.setGenres(new LinkedHashSet<>(List.of(new Genre(1, null))));
        a = films.create(a);

        // фильм 2
        Film b = new Film();
        b.setName("B");
        b.setDescription("d");
        b.setReleaseDate(LocalDate.of(2000, 2, 1));
        b.setDuration(95);
        b.setMpa(new Mpa(2, null));
        b.setGenres(new LinkedHashSet<>(List.of(new Genre(2, null))));
        b = films.create(b);

        List<Film> all = films.findAll();
        assertThat(all.size()).isGreaterThanOrEqualTo(2);

        films.addLike(b.getId(), 1L);
        films.addLike(b.getId(), 2L);
        films.addLike(a.getId(), 1L);

        List<Film> top = films.findPopular(10);
        assertThat(top).isNotEmpty();
        assertThat(top.get(0).getId()).isEqualTo(b.getId());
    }
}
