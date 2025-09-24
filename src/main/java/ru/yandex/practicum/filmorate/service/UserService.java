package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис работы с пользователями.
 * Сообщения об ошибках — на русском ("не найден"), как требуют тесты.
 */
@Service
public class UserService {

    private final UserStorage users;
    @Getter
    private final FilmStorage films; // может быть null в юнит-тестах; оставлен для расширений

    /** Конструктор для Spring — берём именно DB-реализации. */
    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage users,
                       @Qualifier("filmDbStorage") FilmStorage films) {
        this.users = Objects.requireNonNull(users, "users");
        this.films = films;
    }

    /** Упрощённый конструктор для юнит-тестов. */
    public UserService(UserStorage users) {
        this.users = Objects.requireNonNull(users, "users");
        this.films = null;
    }

    // =========== CRUD ===========

    public User create(User user) {
        normalizeName(user);
        return users.create(user);
    }

    public User update(User user) {
        // проверим, что пользователь существует
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

    // =========== Друзья ===========

    public void addFriend(Long userId, Long friendId) {
        if (Objects.equals(userId, friendId)) {
            throw new IllegalArgumentException("Нельзя добавить в друзья самого себя");
        }
        User u = findById(userId);
        User f = findById(friendId);

        Set<Long> a = ensureFriendsSet(u);
        Set<Long> b = ensureFriendsSet(f);

        boolean added = a.add(friendId) | b.add(userId);

        // Если реализация стораджа копирует объекты — синхронизируем:
        users.update(u);
        users.update(f);
    }

    public void removeFriend(Long userId, Long friendId) {
        User u = findById(userId);
        User f = findById(friendId);

        Set<Long> a = u.getFriends();
        Set<Long> b = f.getFriends();

        boolean removed = (a != null && a.remove(friendId)) | (b != null && b.remove(userId));
        users.update(u);
        users.update(f);
    }

    public List<User> getFriends(Long userId) {
        User u = findById(userId);
        if (u.getFriends() == null || u.getFriends().isEmpty()) return List.of();
        return u.getFriends().stream()
                .map(this::findById)
                .sorted(Comparator.comparing(User::getId))
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        User u1 = findById(userId);
        User u2 = findById(otherId);

        Set<Long> s1 = u1.getFriends() == null ? Set.of() : u1.getFriends();
        Set<Long> s2 = u2.getFriends() == null ? Set.of() : u2.getFriends();

        return s1.stream()
                .filter(s2::contains)
                .map(this::findById)
                .sorted(Comparator.comparing(User::getId))
                .collect(Collectors.toList());
    }

    // =========== Вспомогательные ===========

    private static void normalizeName(User user) {
        if (user == null) return;
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private static Long requiredId(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Идентификатор пользователя не задан");
        }
        return user.getId();
    }

    private static Set<Long> ensureFriendsSet(User u) {
        if (u.getFriends() == null) {
            u.setFriends(new LinkedHashSet<>());
        }
        return u.getFriends();
    }

}
