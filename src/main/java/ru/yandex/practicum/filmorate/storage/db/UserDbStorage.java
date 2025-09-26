package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
@Qualifier("db")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbc;

    @Override
    public boolean containsKey(Long id) {
        Integer cnt = jdbc.queryForObject("SELECT COUNT(1) FROM USERS WHERE ID=?", Integer.class, id);
        return cnt != null && cnt > 0;
    }

    @Override
    public Optional<User> findById(Long id) {
        final String sql = "SELECT ID, EMAIL, LOGIN, NAME, BIRTHDAY FROM USERS WHERE ID=?";
        List<User> list = jdbc.query(sql, (rs, rn) -> {
            User u = new User();
            u.setId(rs.getLong("ID"));
            u.setEmail(rs.getString("EMAIL"));
            u.setLogin(rs.getString("LOGIN"));
            u.setName(rs.getString("NAME"));
            Date d = rs.getDate("BIRTHDAY");
            u.setBirthday(d != null ? d.toLocalDate() : null);
            return u;
        }, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.getFirst());
    }

    @Override
    public List<User> findAll() {
        final String sql = "SELECT ID, EMAIL, LOGIN, NAME, BIRTHDAY FROM USERS ORDER BY ID";
        return jdbc.query(sql, (rs, rn) -> {
            User u = new User();
            u.setId(rs.getLong("ID"));
            u.setEmail(rs.getString("EMAIL"));
            u.setLogin(rs.getString("LOGIN"));
            u.setName(rs.getString("NAME"));
            Date d = rs.getDate("BIRTHDAY");
            u.setBirthday(d != null ? d.toLocalDate() : null);
            return u;
        });
    }

    @Override
    public User create(User user) {
        final String sql = "INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY) VALUES (?, ?, ?, ?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null);
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key != null) user.setId(key.longValue());
        return user;
    }

    @Override
    public User update(User user) {
        final String sql = "UPDATE USERS SET EMAIL=?, LOGIN=?, NAME=?, BIRTHDAY=? WHERE ID=?";
        jdbc.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null,
                user.getId());
        return user;
    }

    @Override
    public void addFriend(long userId, long friendId) {
        final String sql = "INSERT INTO FRIENDS (USER_ID, FRIEND_ID) VALUES (?, ?)";
        jdbc.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        final String sql = "DELETE FROM FRIENDS WHERE USER_ID = ? AND FRIEND_ID = ?";
        jdbc.update(sql, userId, friendId);
    }

    @Override
    public List<User> getFriends(long userId) {
        final String sql = "SELECT u.* FROM FRIENDS f " +
                "JOIN USERS u ON u.ID = f.FRIEND_ID " +
                "WHERE f.USER_ID = ? ORDER BY u.ID";
        return jdbc.query(sql, (rs, rowNum) -> new User(
                rs.getLong("id"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getString("name"),
                rs.getDate("birthday").toLocalDate()
        ), userId);
    }

    @Override
    public List<User> getCommonFriends(long userId, long otherId) {
        final String sql = "SELECT u.* FROM USERS u WHERE u.ID IN (" +
                "  SELECT FRIEND_ID FROM FRIENDS WHERE USER_ID = ? " +
                "  INTERSECT " +
                "  SELECT FRIEND_ID FROM FRIENDS WHERE USER_ID = ?" +
                ") ORDER BY u.ID";
        return jdbc.query(sql, (rs, rowNum) -> new User(
                rs.getLong("id"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getString("name"),
                rs.getDate("birthday").toLocalDate()
        ), userId, otherId);
    }
}
