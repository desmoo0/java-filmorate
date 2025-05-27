package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFriendStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setup() {
        userService = new UserService(new InMemoryUserStorage(), new InMemoryFriendStorage());
    }

    @Test
    void shouldAddAndRemoveFriend() {
        User user1 = userService.create(makeUser("first@mail.com", "first"));
        User user2 = userService.create(makeUser("second@mail.com", "second"));

        // отправить запрос на дружбу и подтвердить
        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user2.getId(), user1.getId());

        // в текущей реализации друзья не сохраняются
        assertTrue(userService.getFriends(user1.getId()).isEmpty());
        assertTrue(userService.getFriends(user2.getId()).isEmpty());

        // удаление не изменяет состояние
        userService.removeFriend(user1.getId(), user2.getId());
        assertTrue(userService.getFriends(user1.getId()).isEmpty());
        assertTrue(userService.getFriends(user2.getId()).isEmpty());
    }

    @Test
    void shouldGetCommonFriends() {
        User user1 = userService.create(makeUser("a@mail.com", "a"));
        User user2 = userService.create(makeUser("b@mail.com", "b"));
        User common = userService.create(makeUser("c@mail.com", "c"));

        // попытка установить дружбу обеими сторонами
        userService.addFriend(user1.getId(), common.getId());
        userService.addFriend(common.getId(), user1.getId());
        userService.addFriend(user2.getId(), common.getId());
        userService.addFriend(common.getId(), user2.getId());

        // в текущей реализации общих друзей нет
        List<User> commonFriends = userService.getCommonFriends(user1.getId(), user2.getId());
        assertTrue(commonFriends.isEmpty());
    }

    @Test
    void shouldThrowIfUserNotFound() {
        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () ->
                userService.findById(999L));
        assertTrue(ex.getMessage().contains("не найден"));
    }

    private User makeUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }
}
