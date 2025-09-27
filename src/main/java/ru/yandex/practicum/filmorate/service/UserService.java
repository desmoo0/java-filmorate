package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserService {

    private final UserStorage users;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage users) {
        this.users = users;
    }

    public UserService(UserStorage users, Object dummy) {
        this.users = users;
    }

    public User create(User user) {
        normalizeName(user);
        return users.create(user);
    }

    public User update(User user) {
        findById(requiredId(user));
        normalizeName(user);
        return users.update(user);
    }

    public List<User> findAll() {
        return users.findAll();
    }

    public User findById(Long id) {
        return users.findById(id).orElseThrow(() ->
                new NoSuchElementException("Пользователь не найден: " + id));
    }

    public void addFriend(Long id, Long friendId) {
        findById(id);
        findById(friendId);
        users.addFriend(id, friendId);
    }

    public void removeFriend(Long id, Long friendId) {
        findById(id);
        findById(friendId);
        users.removeFriend(id, friendId);
    }

    public List<User> getFriends(Long id) {
        findById(id);
        return users.getFriends(id);
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        return users.getCommonFriends(id, otherId);
    }

    private void normalizeName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private Long requiredId(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Идентификатор пользователя не задан");
        }
        return user.getId();
    }
}
