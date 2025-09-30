package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.function.GenreStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Primary
@Repository("genreDbStorage")
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbc;

    @Override
    public List<Genre> findAll() {
        return jdbc.query("SELECT ID, NAME FROM GENRE ORDER BY ID", this::mapRowToGenre);
    }

    @Override
    public Optional<Genre> findById(int id) {
        final String sql = "SELECT ID, NAME FROM GENRE WHERE ID = ?";
        try {
            Genre genre = jdbc.queryForObject(sql, this::mapRowToGenre, id);
            return Optional.of(genre);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private Genre mapRowToGenre(ResultSet resultSet, int rowNum) throws SQLException {
        Genre genre = new Genre();
        genre.setId(resultSet.getInt("ID"));
        genre.setName(resultSet.getString("NAME"));
        return genre;
    }
}
