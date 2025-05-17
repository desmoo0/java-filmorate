package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {

    private FilmService filmService;
    private UserService userService;

    @BeforeEach
    void setup() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();

        userService = new UserService(userStorage, filmStorage);
        filmService = new FilmService(filmStorage, userService);
    }

    @Test
    void shouldAddAndRemoveLike() {
        Film film = filmService.create(makeFilm("Титаник"));
        User user = userService.create(makeUser("like@mail.com", "liker"));

        filmService.addLike(film.getId(), user.getId());
        assertTrue(film.getLikes().contains(user.getId()));

        filmService.removeLike(film.getId(), user.getId());
        assertFalse(film.getLikes().contains(user.getId()));
    }

    @Test
    void shouldGetTopFilms() {
        Film f1 = filmService.create(makeFilm("Film A"));
        Film f2 = filmService.create(makeFilm("Film B"));
        Film f3 = filmService.create(makeFilm("Film C"));

        User u1 = userService.create(makeUser("a@mail.com", "a"));
        User u2 = userService.create(makeUser("b@mail.com", "b"));

        filmService.addLike(f1.getId(), u1.getId());
        filmService.addLike(f1.getId(), u2.getId());
        filmService.addLike(f2.getId(), u1.getId());

        List<Film> top = filmService.getTopFilms(2);
        assertEquals(List.of(f1, f2), top);
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        Film film = filmService.create(makeFilm("Unknown User Like"));

        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () ->
                filmService.addLike(film.getId(), 999L));
        assertTrue(ex.getMessage().contains("не найден"));
    }

    private Film makeFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Описание");
        film.setDuration(100);
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
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
