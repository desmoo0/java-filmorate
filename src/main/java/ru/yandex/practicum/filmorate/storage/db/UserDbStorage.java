package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.function.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
@Qualifier("db")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userRowMapper = (resultSet, rowNum) -> new User(
            resultSet.getLong("ID"),
            resultSet.getString("EMAIL"),
            resultSet.getString("LOGIN"),
            resultSet.getString("NAME"),
            resultSet.getDate("BIRTHDAY") != null ? resultSet.getDate("BIRTHDAY").toLocalDate() : null
    );

    @Override
    public boolean containsKey(Long userId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM USERS WHERE ID=?", Integer.class, userId);
        return count != null && count > 0;
    }

    @Override
    public Optional<User> findById(Long userId) {
        final String sqlQuery = "SELECT ID, EMAIL, LOGIN, NAME, BIRTHDAY FROM USERS WHERE ID=?";
        List<User> userList = jdbcTemplate.query(sqlQuery, (resultSet, rowNum) -> new User(
                resultSet.getLong("ID"),
                resultSet.getString("EMAIL"),
                resultSet.getString("LOGIN"),
                resultSet.getString("NAME"),
                resultSet.getDate("BIRTHDAY") != null ? resultSet.getDate("BIRTHDAY").toLocalDate() : null
        ), userId);
        return userList.isEmpty() ? Optional.empty() : Optional.of(userList.get(0));
    }

    @Override
    public List<User> findAll() {
        final String sqlQuery = "SELECT ID, EMAIL, LOGIN, NAME, BIRTHDAY FROM USERS ORDER BY ID";
        return jdbcTemplate.query(sqlQuery, userRowMapper);
    }

    @Override
    public User create(User user) {
        final String sqlQuery = "INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery, new String[]{"ID"});
            preparedStatement.setString(1, user.getEmail());
            preparedStatement.setString(2, user.getLogin());
            preparedStatement.setString(3, user.getName());
            preparedStatement.setDate(4, user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null);
            return preparedStatement;
        }, keyHolder);
        Number generatedKey = keyHolder.getKey();
        if (generatedKey != null) user.setId(generatedKey.longValue());
        return user;
    }

    @Override
    public User update(User user) {
        final String sqlQuery = "UPDATE USERS SET EMAIL=?, LOGIN=?, NAME=?, BIRTHDAY=? WHERE ID=?";
        jdbcTemplate.update(sqlQuery,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null,
                user.getId());
        return user;
    }

    @Override
    public void addFriend(long userId, long friendId) {
        final String sqlQuery = "INSERT INTO FRIENDS (USER_ID, FRIEND_ID) VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, userId, friendId);
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        final String sqlQuery = "DELETE FROM FRIENDS WHERE USER_ID = ? AND FRIEND_ID = ?";
        jdbcTemplate.update(sqlQuery, userId, friendId);
    }

    @Override
    public List<User> getFriends(long userId) {
        final String sqlQuery = "SELECT u.* FROM FRIENDS f " +
                "JOIN USERS u ON u.ID = f.FRIEND_ID " +
                "WHERE f.USER_ID = ? ORDER BY u.ID";
        return jdbcTemplate.query(sqlQuery, (resultSet, rowNum) -> new User(
                resultSet.getLong("id"),
                resultSet.getString("email"),
                resultSet.getString("login"),
                resultSet.getString("name"),
                resultSet.getDate("birthday").toLocalDate()
        ), userId);
    }

    @Override
    public List<User> getCommonFriends(long userId, long anotherUserId) {
        final String sqlQuery = "SELECT u.* " +
                " FROM USERS u " +
                " JOIN FRIENDS f1 ON u.ID = f1.FRIEND_ID AND f1.USER_ID = ? " +
                " JOIN FRIENDS f2 ON u.ID = f2.FRIEND_ID AND f2.USER_ID = ? " +
                " ORDER BY u.ID; ";
        return jdbcTemplate.query(sqlQuery, (resultSet, rowNum) -> new User(
                resultSet.getLong("id"),
                resultSet.getString("email"),
                resultSet.getString("login"),
                resultSet.getString("name"),
                resultSet.getDate("birthday").toLocalDate()
        ), userId, anotherUserId);
    }
}
