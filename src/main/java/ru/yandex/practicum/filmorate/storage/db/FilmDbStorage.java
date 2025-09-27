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
import ru.yandex.practicum.filmorate.storage.FilmStorage;

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

    private final JdbcTemplate jdbc;

    @Override
    public boolean containsKey(Long id) {
        final String sql = "SELECT COUNT(1) FROM FILM WHERE ID = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public Film create(Film film) {
        final String sql = "INSERT INTO FILM (NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"ID"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null);
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            film.setId(key.longValue());
        }

        syncGenres(film.getId(), film.getGenres());
        return findById(film.getId()).orElse(film);
    }

    @Override
    public Film update(Film film) {
        final String sql = "UPDATE FILM SET NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ?, MPA_ID = ? WHERE ID = ?";
        int rowsAffected = jdbc.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null,
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        if (rowsAffected == 0) {
            throw new NoSuchElementException("Фильм не найден: " + film.getId());
        }
        syncGenres(film.getId(), film.getGenres());
        return findById(film.getId()).orElse(film);
    }

    @Override
    public List<Film> findAll() {
        final String sql = "SELECT F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, F.MPA_ID, M.NAME as MPA_NAME " +
                "FROM FILM AS F JOIN MPA AS M ON F.MPA_ID = M.ID ORDER BY F.ID";
        List<Film> films = jdbc.query(sql, this::mapRowToFilm);
        Map<Long, Film> filmsById = films.stream().collect(Collectors.toMap(Film::getId, f -> f));
        loadGenresForFilms(filmsById);
        loadLikesForFilms(filmsById);
        return films;
    }

    @Override
    public Optional<Film> findById(Long id) {
        final String sql = "SELECT F.ID, F.NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION, F.MPA_ID, M.NAME as MPA_NAME " +
                "FROM FILM AS F JOIN MPA AS M ON F.MPA_ID = M.ID WHERE F.ID = ?";
        List<Film> films = jdbc.query(sql, this::mapRowToFilm, id);
        if (films.isEmpty()) {
            return Optional.empty();
        }
        Film film = films.get(0);
        Map<Long, Film> filmMap = Collections.singletonMap(film.getId(), film);
        loadGenresForFilms(filmMap);
        loadLikesForFilms(filmMap);
        return Optional.of(film);
    }

    @Override
    public void addLike(long filmId, long userId) {
        try {
            jdbc.update("INSERT INTO LIKES (FILM_ID, USER_ID) VALUES (?, ?)", filmId, userId);
        } catch (DuplicateKeyException ignore) {
            log.info("Пользователь {} уже поставил лайк фильму {}, игнорируем", userId, filmId);
        }
    }

    @Override
    public void removeLike(long filmId, long userId) {
        jdbc.update("DELETE FROM LIKES WHERE FILM_ID = ? AND USER_ID = ?", filmId, userId);
    }

    @Override
    public List<Film> findPopular(int count) {
        final String sql = "SELECT f.ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, f.MPA_ID, m.NAME AS MPA_NAME " +
                "FROM FILM f " +
                "JOIN MPA m ON m.ID = f.MPA_ID " +
                "LEFT JOIN LIKES l ON l.FILM_ID = f.ID " +
                "GROUP BY f.ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, f.MPA_ID, m.NAME " +
                "ORDER BY COUNT(DISTINCT l.USER_ID) DESC, f.ID ASC " +
                "LIMIT ?";
        List<Film> films = jdbc.query(sql, this::mapRowToFilm, count);
        Map<Long, Film> filmsById = films.stream().collect(Collectors.toMap(Film::getId, f -> f));
        loadGenresForFilms(filmsById);
        loadLikesForFilms(filmsById);
        return films;
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        Date releaseDate = rs.getDate("release_date");
        if (releaseDate != null) {
            film.setReleaseDate(releaseDate.toLocalDate());
        }
        film.setDuration(rs.getInt("duration"));
        film.setMpa(new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")));
        return film;
    }

    private void syncGenres(long filmId, Set<Genre> genres) {
        jdbc.update("DELETE FROM FILM_GENRE WHERE FILM_ID = ?", filmId);
        if (genres == null || genres.isEmpty()) {
            log.debug("No genres to sync for film {}", filmId);
            return;
        }

        List<Object[]> batch = genres.stream()
                .map(g -> new Object[]{filmId, g.getId()})
                .toList();

        try {
            jdbc.batchUpdate("INSERT INTO FILM_GENRE (FILM_ID, GENRE_ID) VALUES (?, ?)", batch);
            log.info("Successfully synced {} genres for film {}", genres.size(), filmId);
        } catch (Exception e) {
            log.error("Failed to sync genres for film {}: {}", filmId, e.getMessage(), e);
            throw e;
        }
    }

    private void loadGenresForFilms(Map<Long, Film> filmsById) {
        if (filmsById.isEmpty()) {
            log.debug("No films to load genres for");
            return;
        }

        String placeholders = String.join(",", Collections.nCopies(filmsById.size(), "?"));
        String sql = "SELECT fg.FILM_ID, g.ID, g.NAME " +
                "FROM FILM_GENRE fg " +
                "JOIN GENRE g ON g.ID = fg.GENRE_ID " +
                "WHERE fg.FILM_ID IN (" + placeholders + ") " +
                "ORDER BY g.ID ASC";

        log.debug("Loading genres for {} films", filmsById.size());

        jdbc.query(sql, rs -> {
            long filmId = rs.getLong("FILM_ID");
            int genreId = rs.getInt("ID");
            String genreName = rs.getString("NAME");

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
        final String sql = "SELECT FILM_ID, USER_ID FROM LIKES WHERE FILM_ID IN (" + placeholders + ")";
        jdbc.query(sql, rs -> {
            Film film = filmsById.get(rs.getLong("FILM_ID"));
            if (film != null) {
                film.addLike(rs.getLong("USER_ID"));
            }
        }, filmsById.keySet().toArray());
    }
}
