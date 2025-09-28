package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.db.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
class UserServiceTest {

    @Autowired
    private UserDbStorage userDbStorage;

    private UserService userService;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        // 1. Используем реальное БД-хранилище
        UserStorage userStorage = userDbStorage;
        // 2. Создаем сервис, передавая ему это хранилище
        userService = new UserService(userStorage, null);

        // 3. Создаем тестовых пользователей и сохраняем их
        user1 = new User(0L, "user1@mail.com", "user1", "User One", LocalDate.of(1990, 1, 1));
        user2 = new User(0L, "user2@mail.com", "user2", "User Two", LocalDate.of(1991, 2, 2));
        user3 = new User(0L, "user3@mail.com", "user3", "User Three", LocalDate.of(1992, 3, 3));

        // Сохраняем пользователей через сервис, чтобы он присвоил им ID
        user1 = userService.create(user1);
        user2 = userService.create(user2);
        user3 = userService.create(user3);
    }

    @Test
    void shouldAddAndRemoveFriend() {
        // Добавляем user2 в друзья к user1
        userService.addFriend(user1.getId(), user2.getId());

        // Проверяем, что user2 теперь в списке друзей user1
        List<User> friendsOfUser1 = userService.getFriends(user1.getId());
        assertEquals(1, friendsOfUser1.size(), "В списке друзей должен быть один друг");
        assertEquals(user2.getId(), friendsOfUser1.get(0).getId(), "ID друга не совпадает");
        assertTrue(friendsOfUser1.contains(user2), "Список друзей должен содержать user2");

        // Удаляем друга
        userService.removeFriend(user1.getId(), user2.getId());

        // Проверяем, что список друзей теперь пуст
        List<User> friendsAfterRemove = userService.getFriends(user1.getId());
        assertTrue(friendsAfterRemove.isEmpty(), "Список друзей должен быть пуст после удаления");
    }

    @Test
    void shouldFindCommonFriends() {
        // user1 дружит с user3
        userService.addFriend(user1.getId(), user3.getId());
        // user2 тоже дружит с user3
        userService.addFriend(user2.getId(), user3.getId());

        // Ищем общих друзей для user1 и user2
        List<User> commonFriends = userService.getCommonFriends(user1.getId(), user2.getId());

        // Проверяем, что общий друг - это user3
        assertEquals(1, commonFriends.size(), "Должен быть один общий друг");
        assertEquals(user3.getId(), commonFriends.get(0).getId(), "Общий друг должен быть user3");
    }
}
