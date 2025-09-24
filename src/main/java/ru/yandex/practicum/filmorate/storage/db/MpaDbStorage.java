package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbc;

    @Override
    public Optional<Mpa> findById(int id) {
        final String sql = "SELECT ID, NAME FROM MPA WHERE ID=?";
        List<Mpa> list = jdbc.query(sql, (rs, rn) ->
                new Mpa(rs.getInt("ID"), rs.getString("NAME")), id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.getFirst());
    }

    @Override
    public List<Mpa> findAll() {
        final String sql = "SELECT ID, NAME FROM MPA ORDER BY ID";
        return jdbc.query(sql, (rs, rn) -> new Mpa(rs.getInt("ID"), rs.getString("NAME")));
    }
}
