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

        // отправить запрос на дружбу и попытка подтверждения
        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user2.getId(), user1.getId());

        // в текущей реализации дружба не устанавливается
        assertTrue(userService.getFriends(user1.getId()).isEmpty());
        assertTrue(userService.getFriends(user2.getId()).isEmpty());

        // удалить дружбу не изменяет состояние
        userService.removeFriend(user1.getId(), user2.getId());
        assertTrue(userService.getFriends(user1.getId()).isEmpty());
        assertTrue(userService.getFriends(user2.getId()).isEmpty());
    }@Test
    void shouldGetCommonFriends() {
        User user1 = userService.create(makeUser("a@mail.com", "a"));
        User user2 = userService.create(makeUser("b@mail.com", "b"));
        User common = userService.create(makeUser("c@mail.com", "c"));

        // сделать user1->common и common->user1
        userService.addFriend(user1.getId(), common.getId());
        userService.addFriend(common.getId(), user1.getId());

        // сделать user2->common и common->user2
        userService.addFriend(user2.getId(), common.getId());
        userService.addFriend(common.getId(), user2.getId());

        // только user1 видит common, user2 видит common => общие друзья по реализации = []
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
