package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.function.FilmStorage;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmServiceTest {

    @Mock
    private FilmStorage filmStorage;

    @Mock
    private GenreService genreService;

    @Mock
    private MpaService mpaService;

    @Mock
    private UserService userService;

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
        when(genreService.findById(1)).thenReturn(new Genre(1, "Комедия"));
        when(genreService.findById(2)).thenReturn(new Genre(2, "Драма"));
        when(mpaService.findById(1)).thenReturn(new Mpa(1, "G"));
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
        when(mpaService.findById(1)).thenReturn(new Mpa(1, "G"));
        when(genreService.findById(1)).thenReturn(new Genre(1, "Комедия"));
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
    void shouldThrowNoSuchElementForInvalidGenre() {
        when(mpaService.findById(1)).thenReturn(new Mpa(1, "G"));
        when(genreService.findById(999)).thenThrow(new NoSuchElementException("Genre not found: 999"));

        testFilm.setMpa(new Mpa(1, null));
        Set<Genre> genres = new LinkedHashSet<>();
        genres.add(new Genre(999, null));
        testFilm.setGenres(genres);

        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> filmService.create(testFilm));
        assertTrue(ex.getMessage().contains("Genre not found: 999"));
    }

    @Test
    void shouldAddAndRemoveLike() {
        testFilm.setId(1L);
        testUser.setId(1L);

        when(userService.findById(1L)).thenReturn(testUser);

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
        when(userService.findById(999L)).thenThrow(new NoSuchElementException("Пользователь не найден: 999"));

        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> filmService.addLike(1L, 999L));
        assertTrue(ex.getMessage().contains("Пользователь не найден: 999"));
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
        return new User(0L, email, login, login, LocalDate.of(2000, 1, 1));
    }
}
