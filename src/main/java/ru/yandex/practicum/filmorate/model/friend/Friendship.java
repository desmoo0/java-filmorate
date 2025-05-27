package ru.yandex.practicum.filmorate.model.friend;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
public class Friendship {
    private final long userId;            // кто отправил запрос
    private final long friendId;          // кому отправлен запрос
    @Setter
    private FriendshipStatus status;      // статус дружбы

    public Friendship(long userId, long friendId, FriendshipStatus status) {
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Friendship that)) return false;
        return userId == that.userId
                && friendId == that.friendId
                && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, friendId, status);
    }

    @Override
    public String toString() {
        return "Friendship{" +
                "userId=" + userId +
                ", friendId=" + friendId +
                ", status=" + status +
                '}';
    }
}
