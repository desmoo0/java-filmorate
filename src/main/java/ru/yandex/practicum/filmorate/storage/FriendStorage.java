package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.friend.Friendship;
import java.util.List;

public interface FriendStorage {
    void addFriend(long userId, long friendId);
    
    void confirmFriend(long userId, long friendId);

    void removeFriend(long userId, long friendId);

    List<Long> getFriends(long userId);

    List<Long> getCommonFriends(long userId, long otherId);

    List<Friendship> getFriendRequests(long userId);
}