package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.function.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean containsKey(Long filmId) {
        final String sqlQuery = "SELECT COUNT(1) FROM FILM WHERE ID = ?";
        Integer count = jdbcTemplate.queryForObject(sqlQuery, Integer.class, filmId);
        return count != null && count > 0;
    }

    @Override
    public Film create(Film film) {
        final String sqlQuery = "INSERT INTO FILM (NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery, new String[]{"ID"});
            preparedStatement.setString(1, film.getName());
            preparedStatement.setString(2, film.getDescription());
            preparedStatement.setDate(3, film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null);
            preparedStatement.setInt(4, film.getDuration());
            preparedStatement.setInt(5, film.getMpa().getId());
            return preparedStatement;
        }, keyHolder);

        Number generatedKey = keyHolder.getKey();
        if (generatedKey != null) {
            film.setId(generatedKey.longValue());
        }

        syncGenres(film.getId(), film.getGenres());
        return findById(film.getId()).orElse(film);
    }

    @Override
    public Film update(Film film) {
        final String sqlQuery = "UPDATE FILM SET NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, " +
                "DURATION = ?, MPA_ID = ? WHERE ID = ?";

        int rowsUpdated = jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null,
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (rowsUpdated == 0) {
            throw new NoSuchElementException("Film not found: " + film.getId());
        }

        syncGenres(film.getId(), film.getGenres());
        return findById(film.getId()).orElse(film);
    }

    @Override
    public List<Film> findAll() {
        final String sqlQuery = "SELECT F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, " +
                "F.MPA_ID, M.NAME AS MPA_NAME " +
                "FROM FILM AS F " +
                "JOIN MPA AS M ON F.MPA_ID = M.ID " +
                "ORDER BY F.ID";

        List<Film> filmList = jdbcTemplate.query(sqlQuery, this::mapRowToFilm);
        Map<Long, Film> filmsById = filmList.stream()
                .collect(Collectors.toMap(Film::getId, film -> film));

        loadGenresForFilms(filmsById);
        loadLikesForFilms(filmsById);

        return filmList;
    }

    @Override
    public Optional<Film> findById(Long filmId) {
        final String sqlQuery = "SELECT F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, " +
                "F.MPA_ID, M.NAME AS MPA_NAME " +
                "FROM FILM AS F " +
                "JOIN MPA AS M ON F.MPA_ID = M.ID " +
                "WHERE F.ID = ?";

        List<Film> films = jdbcTemplate.query(sqlQuery, this::mapRowToFilm, filmId);

        if (films.isEmpty()) {
            return Optional.empty();
        }

        Film film = films.get(0);
        Map<Long, Film> filmsById = Map.of(film.getId(), film);
        loadGenresForFilms(filmsById);
        loadLikesForFilms(filmsById);

        return Optional.of(film);
    }

    @Override
    public void addLike(long filmId, long userId) {
        final String sqlQuery = "INSERT INTO LIKES (FILM_ID, USER_ID) VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, filmId, userId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        final String sqlQuery = "DELETE FROM LIKES WHERE FILM_ID = ? AND USER_ID = ?";
        jdbcTemplate.update(sqlQuery, filmId, userId);
    }

    @Override
    public List<Film> findPopular(int count) {
        final String sqlQuery = "SELECT F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, " +
                "F.MPA_ID, M.NAME AS MPA_NAME, COUNT(L.USER_ID) AS LIKES_COUNT " +
                "FROM FILM AS F " +
                "JOIN MPA AS M ON F.MPA_ID = M.ID " +
                "LEFT JOIN LIKES AS L ON F.ID = L.FILM_ID " +
                "GROUP BY F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, F.MPA_ID, M.NAME " +
                "ORDER BY LIKES_COUNT DESC " +
                "LIMIT ?";

        List<Film> filmList = jdbcTemplate.query(sqlQuery, this::mapRowToFilm, count);
        Map<Long, Film> filmsById = filmList.stream()
                .collect(Collectors.toMap(Film::getId, film -> film));

        loadGenresForFilms(filmsById);
        loadLikesForFilms(filmsById);

        return filmList;
    }

    private Film mapRowToFilm(ResultSet resultSet, int rowNumber) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getLong("ID"));
        film.setName(resultSet.getString("NAME"));
        film.setDescription(resultSet.getString("DESCRIPTION"));

        Date releaseDateSql = resultSet.getDate("RELEASE_DATE");
        if (releaseDateSql != null) {
            film.setReleaseDate(releaseDateSql.toLocalDate());
        }

        film.setDuration(resultSet.getInt("DURATION"));
        film.setMpa(new Mpa(resultSet.getInt("MPA_ID"), resultSet.getString("MPA_NAME")));

        return film;
    }

    private void syncGenres(long filmId, Set<Genre> genres) {
        jdbcTemplate.update("DELETE FROM FILM_GENRE WHERE FILM_ID = ?", filmId);

        if (genres == null || genres.isEmpty()) {
            log.debug("No genres to sync for film {}", filmId);
            return;
        }

        List<Object[]> batchParams = genres.stream()
                .map(genre -> new Object[]{filmId, genre.getId()})
                .toList();

        try {
            jdbcTemplate.batchUpdate("INSERT INTO FILM_GENRE (FILM_ID, GENRE_ID) VALUES (?, ?)", batchParams);
            log.info("Successfully synced {} genres for film {}", genres.size(), filmId);
        } catch (Exception exception) {
            log.error("Failed to sync genres for film {}: {}", filmId, exception.getMessage(), exception);
            throw exception;
        }
    }

    private void loadGenresForFilms(Map<Long, Film> filmsById) {
        if (filmsById.isEmpty()) {
            return;
        }

        String placeholders = String.join(",", Collections.nCopies(filmsById.size(), "?"));
        String sqlQuery = String.format(
                "SELECT fg.FILM_ID, g.ID, g.NAME " +
                        "FROM FILM_GENRE fg " +
                        "JOIN GENRE g ON g.ID = fg.GENRE_ID " +
                        "WHERE fg.FILM_ID IN (%s) " +
                        "ORDER BY g.ID ASC",
                placeholders
        );

        jdbcTemplate.query(sqlQuery, resultSet -> {
            long filmId = resultSet.getLong("FILM_ID");
            int genreId = resultSet.getInt("ID");
            String genreName = resultSet.getString("NAME");

            Genre genre = new Genre();
            genre.setId(genreId);
            genre.setName(genreName);

            Film film = filmsById.get(filmId);
            if (film != null) {
                film.getGenres().add(genre);
            }
        }, filmsById.keySet().toArray());
    }

    private void loadLikesForFilms(Map<Long, Film> filmsById) {
        if (filmsById.isEmpty()) {
            return;
        }

        String placeholders = String.join(",", Collections.nCopies(filmsById.size(), "?"));
        final String sqlQuery = String.format(
                "SELECT FILM_ID, USER_ID FROM LIKES WHERE FILM_ID IN (%s)",
                placeholders
        );

        jdbcTemplate.query(sqlQuery, resultSet -> {
            Film film = filmsById.get(resultSet.getLong("FILM_ID"));
            if (film != null) {
                film.addLike(resultSet.getLong("USER_ID"));
            }
        }, filmsById.keySet().toArray());
    }
}
