package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.function.FilmStorage;
import ru.yandex.practicum.filmorate.storage.function.GenreStorage;
import ru.yandex.practicum.filmorate.storage.function.MpaStorage;
import ru.yandex.practicum.filmorate.storage.function.UserStorage;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmServiceTest {

    @Mock
    private FilmStorage filmStorage;

    @Mock
    private GenreStorage genreStorage;

    @Mock
    private MpaStorage mpaStorage;

    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private FilmService filmService;

    private Film testFilm;
    private User testUser;

    @BeforeEach
    void setUp() {
        testFilm = makeFilm("Test Film");
        testUser = makeUser("test@mail.ru", "test");
    }

    @Test
    void shouldCreateFilmWithGenres() {
        when(genreStorage.findById(1)).thenReturn(Optional.of(new Genre(1, "Комедия")));
        when(genreStorage.findById(2)).thenReturn(Optional.of(new Genre(2, "Драма")));
        when(mpaStorage.findById(1)).thenReturn(Optional.of(new Mpa(1, "G")));
        when(filmStorage.create(any(Film.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Set<Genre> genres = new LinkedHashSet<>();
        genres.add(new Genre(1, null));
        genres.add(new Genre(2, null));
        testFilm.setGenres(genres);

        Film created = filmService.create(testFilm);

        assertNotNull(created);
        assertEquals(2, created.getGenres().size());
        assertTrue(created.getGenres().stream().anyMatch(g -> "Комедия".equals(g.getName())));
        assertTrue(created.getGenres().stream().anyMatch(g -> "Драма".equals(g.getName())));
        verify(filmStorage, times(1)).create(testFilm);
    }

    @Test
    void shouldUpdateFilmWithGenres() {
        when(mpaStorage.findById(1)).thenReturn(Optional.of(new Mpa(1, "G")));
        when(genreStorage.findById(1)).thenReturn(Optional.of(new Genre(1, "Комедия")));
        when(filmStorage.containsKey(1L)).thenReturn(true);
        when(filmStorage.update(any(Film.class))).thenReturn(testFilm);

        Set<Genre> newGenres = new LinkedHashSet<>();
        newGenres.add(new Genre(1, null));
        testFilm.setGenres(newGenres);
        testFilm.setId(1L);

        Film updated = filmService.update(testFilm);

        assertEquals(1, updated.getGenres().size());
        assertTrue(updated.getGenres().stream().anyMatch(g -> "Комедия".equals(g.getName())));
        verify(filmStorage, times(1)).update(testFilm);
    }

    @Test
    void shouldThrowNotFoundForInvalidGenre() {
        when(mpaStorage.findById(1)).thenReturn(Optional.of(new Mpa(1, "G")));
        when(genreStorage.findById(999)).thenReturn(Optional.empty());

        testFilm.setMpa(new Mpa(1, null));
        Set<Genre> genres = new LinkedHashSet<>();
        genres.add(new Genre(999, null));
        testFilm.setGenres(genres);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> filmService.create(testFilm));
        assertTrue(ex.getMessage().contains("Genre 999"));
    }

    @Test
    void shouldAddAndRemoveLike() {
        testFilm.setId(1L);
        testUser.setId(1L);

        when(userStorage.findById(1L)).thenReturn(Optional.of(testUser));

        filmService.addLike(1L, 1L);
        verify(filmStorage, times(1)).addLike(1L, 1L);

        filmService.removeLike(1L, 1L);
        verify(filmStorage, times(1)).removeLike(1L, 1L);
    }

    @Test
    void shouldGetTopFilms() {
        Film f1 = makeFilm("Film A");
        f1.setId(1L);
        Film f2 = makeFilm("Film B");
        f2.setId(2L);

        when(filmStorage.findPopular(2)).thenReturn(List.of(f1, f2));

        List<Film> top = filmService.getTopFilms(2);
        assertEquals(2, top.size());
        assertEquals("Film A", top.get(0).getName());
    }

    @Test
    void shouldThrowWhenUserNotFoundForLike() {
        when(userStorage.findById(999L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> filmService.addLike(1L, 999L));
        assertTrue(ex.getMessage().contains("User 999"));
    }

    private Film makeFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Описание");
        film.setDuration(100);
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setMpa(new Mpa(1, "G"));
        return film;
    }

    private User makeUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }
}
