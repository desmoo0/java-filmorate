package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.MpaRating;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFriendStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {

    private FilmService filmService;
    private UserService userService;

    @BeforeEach
    void setup() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        InMemoryFriendStorage friendStorage = new InMemoryFriendStorage();
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();

        userService = new UserService(userStorage, friendStorage);
        filmService = new FilmService(filmStorage, userService);
    }

    @Test
    void shouldAddAndRemoveLike() {
        Film film = filmService.create(makeFilm("Титаник"));
        User user = userService.create(makeUser("like@mail.com", "liker"));

        filmService.addLike(film.getId(), user.getId());
        Film withLike = filmService.getById(film.getId());
        assertTrue(withLike.getLikes().contains(user.getId()), "Like should be recorded");

        filmService.removeLike(film.getId(), user.getId());
        Film withoutLike = filmService.getById(film.getId());
        assertFalse(withoutLike.getLikes().contains(user.getId()), "Like should be removed");
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        Film film = filmService.create(makeFilm("Unknown User Like"));

        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () ->
                filmService.addLike(film.getId(), 999L));
        assertTrue(ex.getMessage().contains("не найден"));
    }

    @Test
    void shouldCreateFilmWithValidData() {
        Film created = filmService.create(makeFilm("Интерстеллар"));
        assertNotNull(created.getId());
        assertEquals("Интерстеллар", created.getName());
        assertEquals(100, created.getDuration());
        assertEquals(LocalDate.of(2020,1,1), created.getReleaseDate());
        assertEquals(Set.of(Genre.COMEDY), created.getGenres());
        assertEquals(MpaRating.G, created.getMpaRating());
    }

    @Test
    void shouldUpdateExistingFilm() {
        Film film = filmService.create(makeFilm("Original"));
        film.setName("Updated");
        film.setDuration(150);
        Film updated = filmService.update(film);
        assertEquals(film.getId(), updated.getId());
        assertEquals("Updated", updated.getName());
        assertEquals(150, updated.getDuration());
    }

    @Test
    void shouldGetFilmById() {
        Film film = filmService.create(makeFilm("Avatar"));
        Film found = filmService.getById(film.getId());
        assertEquals(film.getId(), found.getId());
        assertEquals("Avatar", found.getName());
    }

    @Test
    void shouldGetAllFilms() {
        filmService.create(makeFilm("A"));
        filmService.create(makeFilm("B"));
        List<Film> all = filmService.getAll();
        assertEquals(2, all.size());
        assertTrue(all.stream().map(Film::getName).toList().containsAll(List.of("A","B")));
    }

    @Test
    void shouldReturnTopFilmsByLikes() {
        Film f1 = filmService.create(makeFilm("F1"));
        Film f2 = filmService.create(makeFilm("F2"));
        User u1 = userService.create(makeUser("u1@mail.ru","u1"));
        User u2 = userService.create(makeUser("u2@mail.ru","u2"));

        // f1 gets two likes, f2 gets one
        filmService.addLike(f1.getId(), u1.getId());
        filmService.addLike(f1.getId(), u2.getId());
        filmService.addLike(f2.getId(), u1.getId());

        List<Film> top1 = filmService.getTopFilms(1);
        assertEquals(1, top1.size());
        assertEquals(f1.getId(), top1.get(0).getId());
    }

    // Вспомогательные методы
    private Film makeFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Описание");
        film.setDuration(100);
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setGenres(Set.of(Genre.COMEDY));
        film.setMpaRating(MpaRating.G);
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
