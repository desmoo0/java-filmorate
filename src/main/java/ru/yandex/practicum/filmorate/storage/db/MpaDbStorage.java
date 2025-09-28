package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.function.MpaStorage;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<Mpa> findById(int mpaId) {
        final String sqlQuery = "SELECT ID, NAME FROM MPA WHERE ID=?";
        List<Mpa> mpaList = jdbcTemplate.query(sqlQuery, (resultSet, rowNum) ->
                new Mpa(resultSet.getInt("ID"), resultSet.getString("NAME")), mpaId);
        return mpaList.isEmpty() ? Optional.empty() : Optional.of(mpaList.getFirst());
    }

    @Override
    public List<Mpa> findAll() {
        final String sqlQuery = "SELECT ID, NAME FROM MPA ORDER BY ID";
        return jdbcTemplate.query(sqlQuery, (resultSet, rowNum) -> new Mpa(resultSet.getInt("ID"), resultSet.getString("NAME")));
    }
}
