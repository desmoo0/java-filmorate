package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    // Добавляем хранилище для друзей: ключ - ID пользователя, значение - множество ID его друзей
    private final Map<Long, Set<Long>> friends = new HashMap<>();
    private long currentId = 1L;

    @Override
    public User create(User user) {
        user.setId(currentId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public boolean containsKey(Long id) {
        return users.containsKey(id);
    }

    @Override
    public void addFriend(long userId, long friendId) {
        friends.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        Set<Long> userFriends = friends.get(userId);
        if (userFriends != null) {
            userFriends.remove(friendId);
        }
    }

    @Override
    public List<User> getFriends(long userId) {
        Set<Long> friendIds = friends.getOrDefault(userId, Collections.emptySet());
        return friendIds.stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(long userId, long otherId) {
        Set<Long> userFriends = new HashSet<>(friends.getOrDefault(userId, Collections.emptySet()));
        Set<Long> otherFriends = friends.getOrDefault(otherId, Collections.emptySet());

        userFriends.retainAll(otherFriends);

        return userFriends.stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
