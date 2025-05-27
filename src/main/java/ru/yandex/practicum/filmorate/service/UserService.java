package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ExistingUserException;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.FriendStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final FriendStorage friendStorage;

    @Autowired
    public UserService(UserStorage userStorage, FriendStorage friendStorage) {
        this.userStorage = userStorage;
        this.friendStorage = friendStorage;
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

    public User findById(long id) {
        Optional<User> opt = userStorage.findById(Long.valueOf(id));
        if (opt.isEmpty()) {
            throw new NoSuchElementException("Пользователь с id=" + id + " не найден");
        }
        return opt.get();
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public void addFriend(long userId, long friendId) {
        findById(userId);
        findById(friendId);

        // проверяем, есть ли входящий запрос от friendId к userId
        boolean hasRequest = friendStorage.getFriendRequests(userId).stream()
                .anyMatch(r -> r.getUserId() == friendId && r.getFriendId() == userId);

        if (hasRequest) {
            // подтверждаем первоначальный запрос
            friendStorage.confirmFriend(friendId, userId);
            // создаём зеркальную запись и сразу подтверждаем её
            friendStorage.addFriend(userId, friendId);
            friendStorage.confirmFriend(userId, friendId);
        } else {
            // создаём новый запрос на дружбу
            friendStorage.addFriend(userId, friendId);
        }
    }

    public void removeFriend(long userId, long friendId) {
        findById(userId);
        findById(friendId);
        friendStorage.removeFriend(userId, friendId);
        friendStorage.removeFriend(friendId, userId);
    }

    public List<User> getFriends(long id) {
        findById(id);
        return friendStorage.getFriends(id).stream()
                .map(userStorage::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public List<User> getCommonFriends(long id1, long id2) {
        findById(id1);
        findById(id2);
        return friendStorage.getCommonFriends(id1, id2).stream()
                .map(userStorage::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}
