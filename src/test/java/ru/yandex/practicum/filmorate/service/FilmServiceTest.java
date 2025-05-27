package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.MpaRating;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFriendStorage;

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
        var userStorage = new InMemoryUserStorage();
        var friendStorage = new InMemoryFriendStorage();
        var filmStorage = new InMemoryFilmStorage();

        this.userService = new UserService(userStorage, friendStorage);
        this.filmService = new FilmService(filmStorage, userService);
    }

    @Test
    void shouldCreateFilmAssignId() {
        Film film = makeFilm("Интерстеллар");
        Film created = filmService.create(film);

        assertNotNull(created.getId(), "ID должен быть присвоен");
        assertEquals("Интерстеллар", created.getName());
        assertEquals("Описание", created.getDescription());
        assertEquals(LocalDate.of(2020, 1, 1), created.getReleaseDate());
        assertEquals(100, created.getDuration());
        assertEquals(Set.of(Genre.COMEDY), created.getGenres());
        assertEquals(MpaRating.G, created.getMpaRating());
    }

    @Test
    void shouldUpdateFilmDetails() {
        Film f = filmService.create(makeFilm("Первое название"));
        f.setName("Новое название");
        f.setDescription("Новое описание");
        Film updated = filmService.update(f);

        assertEquals(f.getId(), updated.getId());
        assertEquals("Новое название", updated.getName());
        assertEquals("Новое описание", updated.getDescription());
    }

    @Test
    void shouldGetAllFilms() {
        filmService.create(makeFilm("Фильм 1"));
        filmService.create(makeFilm("Фильм 2"));
        List<Film> all = filmService.getAll();

        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(f -> f.getName().equals("Фильм 1")));
        assertTrue(all.stream().anyMatch(f -> f.getName().equals("Фильм 2")));
    }

    @Test
    void shouldThrowWhenFilmNotFoundOnGet() {
        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> filmService.getById(999L));
        assertTrue(ex.getMessage().contains("не найден"));
    }

    @Test
    void shouldAddAndRemoveLike() {
        Film film = filmService.create(makeFilm("Титаник"));
        User user = userService.create(makeUser("like@mail.com", "liker"));

        filmService.addLike(film.getId(), user.getId());
        Film withLike = filmService.getById(film.getId());
        assertTrue(withLike.getLikes().contains(user.getId()), "Лайк должен сохраниться");

        filmService.removeLike(film.getId(), user.getId());
        Film withoutLike = filmService.getById(film.getId());
        assertFalse(withoutLike.getLikes().contains(user.getId()), "Лайк должен удалиться");
    }

    @Test
    void shouldReturnTopFilmsByLikes() {
        // создаём три фильма
        Film f1 = filmService.create(makeFilm("F1"));
        Film f2 = filmService.create(makeFilm("F2"));
        Film f3 = filmService.create(makeFilm("F3"));
        // два пользователя
        User u1 = userService.create(makeUser("u1@mail.com", "u1"));
        User u2 = userService.create(makeUser("u2@mail.com", "u2"));

        // f1: 2 лайка, f2: 1 лайк, f3: 0
        filmService.addLike(f1.getId(), u1.getId());
        filmService.addLike(f1.getId(), u2.getId());
        filmService.addLike(f2.getId(), u1.getId());

        List<Film> top2 = filmService.getTopFilms(2);
        assertEquals(2, top2.size());
        assertEquals(f1.getId(), top2.get(0).getId(), "Первым должен идти фильм с двумя лайками");
        assertEquals(f2.getId(), top2.get(1).getId(), "Вторым — фильм с одним лайком");
    }

    @Test
    void shouldThrowWhenUserNotFoundOnLike() {
        Film film = filmService.create(makeFilm("Нет юзера"));
        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> filmService.addLike(film.getId(), 999L));
        assertTrue(ex.getMessage().contains("не найден"));
    }

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

    @Test
    void shouldThrowOnUpdateNonexistentFilm() {
        Film f = makeFilm("Нет");
        f.setId(999L);
        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> filmService.update(f));
        assertTrue(ex.getMessage().contains("не найден"));
    }

    @Test
    void shouldNotDuplicateLike() {
        Film film = filmService.create(makeFilm("Тест"));
        User user = userService.create(makeUser("a@mail.com","u"));
        filmService.addLike(film.getId(), user.getId());
        filmService.addLike(film.getId(), user.getId()); // второй раз
        Film got = filmService.getById(film.getId());
        assertEquals(1, got.getLikes().size());
    }

    @Test
    void shouldNotFailOnRemoveLikeFromNonexistentFilm() {
        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> filmService.removeLike(999L, 1L));
        assertTrue(ex.getMessage().contains("не найден"));
    }

}
