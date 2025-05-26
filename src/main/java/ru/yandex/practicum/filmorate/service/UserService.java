package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ExistingUserException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.*;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage, FilmStorage filmStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getFriends(Long id) {
        User user = findById(id);
        return user.getFriends().stream()
                .map(userStorage::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public void addFriend(Long userId, Long friendId) {
        User user = userStorage.findById(userId).orElseThrow();
        User friend = userStorage.findById(friendId).orElseThrow();

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = userStorage.findById(userId).orElseThrow();
        User friend = userStorage.findById(friendId).orElseThrow();

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    public List<User> getCommonFriends(Long id1, Long id2) {
        User u1 = userStorage.findById(id1).orElseThrow();
        User u2 = userStorage.findById(id2).orElseThrow();

        Set<Long> commonIds = new HashSet<>(u1.getFriends());
        commonIds.retainAll(u2.getFriends());

        return commonIds.stream()
                .map(userStorage::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public User create(User user) {
        if (user.getId() != null && userStorage.containsKey(user.getId())) {
            throw new ExistingUserException("Пользователь с таким ID уже существует.");
        }

        if (user.getLogin().contains(" ")) {
            throw new ExistingUserException("Логин не может содержать пробелы.");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ExistingUserException("Дата рождения не может быть в будущем.");
        }
        return userStorage.create(user);
    }

    public User update(User user) {
        if (user.getId() == null || !userStorage.containsKey(user.getId())) {
            throw new NoSuchElementException("Пользователь не найден.");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.update(user);
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id=" + id + " не найден"));
    }
}
