package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbc;

    @Override
    public Optional<Genre> findById(int id) {
        final String sql = "SELECT ID, NAME FROM GENRE WHERE ID=?";
        List<Genre> list = jdbc.query(sql, (rs, rn) ->
                new Genre(rs.getInt("ID"), rs.getString("NAME")), id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.getFirst());
    }

    @Override
    public List<Genre> findAll() {
        final String sql = "SELECT ID, NAME FROM GENRE ORDER BY ID";
        return jdbc.query(sql, (rs, rn) -> new Genre(rs.getInt("ID"), rs.getString("NAME")));
    }
}
