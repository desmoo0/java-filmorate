package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
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
        final String sqlQuery = "UPDATE FILM SET NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ?, MPA_ID = ? WHERE ID = ?";
        int rowsUpdated = jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null,
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        if (rowsUpdated == 0) {
            throw new NoSuchElementException("Фильм не найден: " + film.getId());
        }
        syncGenres(film.getId(), film.getGenres());
        return findById(film.getId()).orElse(film);
    }

    @Override
    public List<Film> findAll() {
        final String sqlQuery = "SELECT F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, F.MPA_ID, M.NAME as MPA_NAME " +
                "FROM FILM AS F JOIN MPA AS M ON F.MPA_ID = M.ID ORDER BY F.ID";
        List<Film> filmList = jdbcTemplate.query(sqlQuery, this::mapRowToFilm);
        Map<Long, Film> filmsById = filmList.stream().collect(Collectors.toMap(Film::getId, film -> film));
        loadGenresForFilms(filmsById);
        loadLikesForFilms(filmsById);
        return filmList;
    }

    @Override
    public Optional<Film> findById(Long filmId) {
        final String sqlQuery = "SELECT F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, F.MPA_ID, M.NAME as MPA_NAME " +
                "FROM FILM AS F JOIN MPA AS M ON F.MPA_ID = M.ID WHERE F.ID = ?";
        List<Film> filmList = jdbcTemplate.query(sqlQuery, this::mapRowToFilm, filmId);
        if (filmList.isEmpty()) {
            return Optional.empty();
        }
        Film film = filmList.get(0);
        Map<Long, Film> filmMap = Collections.singletonMap(film.getId(), film);
        loadGenresForFilms(filmMap);
        loadLikesForFilms(filmMap);
        return Optional.of(film);
    }

    @Override
    public void addLike(long filmId, long userId) {
        try {
            jdbcTemplate.update("INSERT INTO LIKES (FILM_ID, USER_ID) VALUES (?, ?)", filmId, userId);
        } catch (DuplicateKeyException duplicateKeyException) {
            log.info("Пользователь {} уже поставил лайк фильму {}, игнорируем", userId, filmId);
        }
    }

    @Override
    public void removeLike(long filmId, long userId) {
        jdbcTemplate.update("DELETE FROM LIKES WHERE FILM_ID = ? AND USER_ID = ?", filmId, userId);
    }

    @Override
    public List<Film> findPopular(int limit) {
        final String sqlQuery = "SELECT f.ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, f.MPA_ID, m.NAME AS MPA_NAME " +
                "FROM FILM f " +
                "JOIN MPA m ON m.ID = f.MPA_ID " +
                "LEFT JOIN LIKES l ON l.FILM_ID = f.ID " +
                "GROUP BY f.ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, f.MPA_ID, m.NAME " +
                "ORDER BY COUNT(DISTINCT l.USER_ID) DESC, f.ID ASC " +
                "LIMIT ?";
        List<Film> popularFilms = jdbcTemplate.query(sqlQuery, this::mapRowToFilm, limit);
        Map<Long, Film> popularFilmsById = popularFilms.stream().collect(Collectors.toMap(Film::getId, film -> film));
        loadGenresForFilms(popularFilmsById);
        loadLikesForFilms(popularFilmsById);
        return popularFilms;
    }

    private Film mapRowToFilm(ResultSet resultSet, int rowNumber) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getLong("id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        Date releaseDateSql = resultSet.getDate("release_date");
        if (releaseDateSql != null) {
            film.setReleaseDate(releaseDateSql.toLocalDate());
        }
        film.setDuration(resultSet.getInt("duration"));
        film.setMpa(new Mpa(resultSet.getInt("mpa_id"), resultSet.getString("mpa_name")));
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
            log.debug("No films to load genres for");
            return;
        }

        String placeholders = String.join(",", Collections.nCopies(filmsById.size(), "?"));
        String sqlQuery = "SELECT fg.FILM_ID, g.ID, g.NAME " +
                "FROM FILM_GENRE fg " +
                "JOIN GENRE g ON g.ID = fg.GENRE_ID " +
                "WHERE fg.FILM_ID IN (" + placeholders + ") " +
                "ORDER BY g.ID ASC";

        log.debug("Loading genres for {} films", filmsById.size());

        jdbcTemplate.query(sqlQuery, resultSet -> {
            long filmId = resultSet.getLong("FILM_ID");
            int genreId = resultSet.getInt("ID");
            String genreName = resultSet.getString("NAME");

            Genre genre = new Genre(genreId, genreName);
            Film film = filmsById.get(filmId);

            if (film != null) {
                film.getGenres().add(genre);
                log.debug("Loaded genre {} ({}) for film {}", genreId, genreName, filmId);
            } else {
                log.warn("Found genre for non-existing film in map: {}", filmId);
            }
        }, filmsById.keySet().toArray());

        log.debug("Finished loading genres for films");
    }

    private void loadLikesForFilms(Map<Long, Film> filmsById) {
        if (filmsById.isEmpty()) return;
        String placeholders = String.join(",", Collections.nCopies(filmsById.size(), "?"));
        final String sqlQuery = "SELECT FILM_ID, USER_ID FROM LIKES WHERE FILM_ID IN (" + placeholders + ")";
        jdbcTemplate.query(sqlQuery, resultSet -> {
            Film film = filmsById.get(resultSet.getLong("FILM_ID"));
            if (film != null) {
                film.addLike(resultSet.getLong("USER_ID"));
            }
        }, filmsById.keySet().toArray());
    }
}
