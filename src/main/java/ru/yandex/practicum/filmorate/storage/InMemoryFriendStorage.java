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
        List<Friendship> list = store.computeIfAbsent(userId, k -> new ArrayList<>());
        list.add(new Friendship(userId, friendId, FriendshipStatus.UNCONFIRMED));
    }

    @Override
    public void confirmFriend(long userId, long friendId) {
        List<Friendship> list = store.get(userId);
        if (list == null) return;
        for (Friendship f : list) {
            if (f.getUserId() == userId && f.getFriendId() == friendId) {
                f.setStatus(FriendshipStatus.CONFIRMED);
            }
        }
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        List<Friendship> list = store.get(userId);
        if (list == null) return;
        list.removeIf(f -> f.getFriendId() == friendId);
    }

    @Override
    public List<Long> getFriends(long userId) {
        List<Friendship> list = store.get(userId);
        List<Long> result = new ArrayList<>();
        if (list != null) {
            for (Friendship f : list) {
                if (f.getStatus() == FriendshipStatus.CONFIRMED) {
                    result.add(f.getFriendId());
                }
            }
        }
        return result;
    }

    @Override
    public List<Long> getCommonFriends(long userId, long otherId) {
        return List.of();
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
