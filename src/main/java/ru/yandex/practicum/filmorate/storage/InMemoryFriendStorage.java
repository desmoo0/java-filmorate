package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.friend.Friendship;
import ru.yandex.practicum.filmorate.model.friend.FriendshipStatus;

import java.util.*;

@Component
public class InMemoryFriendStorage implements FriendStorage {
    private final Map<Long, List<Friendship>> store = new HashMap<>();

    @Override
    public void addFriend(long userId, long friendId) {
        List<Friendship> list = store.computeIfAbsent(Long.valueOf(userId), k -> new ArrayList<>());
        list.add(new Friendship(userId, friendId, FriendshipStatus.UNCONFIRMED));
    }

    @Override
    public void confirmFriend(long userId, long friendId) {
        List<Friendship> list = store.get(Optional.of(userId));
        if (list == null) return;
        for (Friendship f : list) {
            if (f.getUserId() == userId && f.getFriendId() == friendId) {
                f.setStatus(FriendshipStatus.CONFIRMED);
            }
        }
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        List<Friendship> list = store.get(Optional.of(userId));
        if (list == null) return;
        list.removeIf(f -> f.getFriendId() == friendId);
    }

    @Override
    public List<Long> getFriends(long userId) {
        List<Friendship> list = store.get(Optional.of(userId));
        List<Long> result = new ArrayList<>();
        if (list != null) {
            for (Friendship f : list) {
                if (f.getStatus() == FriendshipStatus.CONFIRMED) {
                    result.add(Long.valueOf(f.getFriendId()));
                }
            }
        }
        return result;
    }

    @Override
    public List<Long> getCommonFriends(long userId, long otherId) {
        List<Long> a = getFriends(userId);
        List<Long> b = getFriends(otherId);
        List<Long> common = new ArrayList<>();
        for (Long id : a) {
            if (b.contains(id)) {
                common.add(id);
            }
        }
        return common;
    }

    @Override
    public List<Friendship> getFriendRequests(long userId) {
        List<Friendship> requests = new ArrayList<>();
        for (List<Friendship> list : store.values()) {
            for (Friendship f : list) {
                if (f.getFriendId() == userId && f.getStatus() == FriendshipStatus.UNCONFIRMED) {
                    requests.add(f);
                }
            }
        }
        return requests;
    }
}