package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
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
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbc;

    @Override
    public boolean containsKey(Long id) {
        Integer cnt = jdbc.queryForObject("SELECT COUNT(1) FROM FILM WHERE ID=?", Integer.class, id);
        return cnt != null && cnt > 0;
    }

    @Override
    public Optional<Film> findById(Long id) {
        final String sql =
                "SELECT f.ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, " +
                        "       m.ID AS MPA_ID, m.NAME AS MPA_NAME " +
                        "FROM FILM f JOIN MPA m ON m.ID = f.MPA_ID WHERE f.ID=?";
        List<Film> list = jdbc.query(sql, (rs, rn) -> {
            Film f = new Film();
            f.setId(rs.getLong("ID"));
            f.setName(rs.getString("NAME"));
            f.setDescription(rs.getString("DESCRIPTION"));
            Date d = rs.getDate("RELEASE_DATE");
            f.setReleaseDate(d != null ? d.toLocalDate() : null);
            f.setDuration(rs.getInt("DURATION"));
            f.setMpa(new Mpa(rs.getInt("MPA_ID"), rs.getString("MPA_NAME")));
            f.setGenres(new LinkedHashSet<>());
            return f;
        }, id);
        if (list.isEmpty()) return Optional.empty();
        Map<Long, Film> map = list.stream().collect(Collectors.toMap(Film::getId, it -> it));
        loadGenresForFilms(map);
        return Optional.of(list.getFirst());
    }

    @Override
    public List<Film> findAll() {
        final String sql =
                "SELECT f.ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, " +
                        "       m.ID AS MPA_ID, m.NAME AS MPA_NAME " +
                        "FROM FILM f JOIN MPA m ON m.ID = f.MPA_ID ORDER BY f.ID";
        List<Film> list = jdbc.query(sql, (rs, rn) -> {
            Film f = new Film();
            f.setId(rs.getLong("ID"));
            f.setName(rs.getString("NAME"));
            f.setDescription(rs.getString("DESCRIPTION"));
            Date d = rs.getDate("RELEASE_DATE");
            f.setReleaseDate(d != null ? d.toLocalDate() : null);
            f.setDuration(rs.getInt("DURATION"));
            f.setMpa(new Mpa(rs.getInt("MPA_ID"), rs.getString("MPA_NAME")));
            f.setGenres(new LinkedHashSet<>());
            return f;
        });
        if (list.isEmpty()) return list;
        Map<Long, Film> map = list.stream().collect(Collectors.toMap(Film::getId, it -> it));
        loadGenresForFilms(map);
        return list;
    }

    @Override
    public Film create(Film film) {
        final String sql = "INSERT INTO FILM (NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID) VALUES (?, ?, ?, ?, ?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null);
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key != null) film.setId(key.longValue());
        syncGenres(film.getId(), film.getGenres());
        return findById(film.getId()).orElse(film);
    }

    @Override
    public Film update(Film film) {
        final String sql = "UPDATE FILM SET NAME=?, DESCRIPTION=?, RELEASE_DATE=?, DURATION=?, MPA_ID=? WHERE ID=?";
        jdbc.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null,
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        syncGenres(film.getId(), film.getGenres());
        return findById(film.getId()).orElse(film);
    }

    // Доп. API (если используешь)
    public void addLike(long filmId, long userId) {
        try {
            jdbc.update("INSERT INTO FILM_LIKES (FILM_ID, USER_ID) VALUES (?, ?)", filmId, userId);
        } catch (DuplicateKeyException ignore) {}
    }

    public void removeLike(long filmId, long userId) {
        jdbc.update("DELETE FROM FILM_LIKES WHERE FILM_ID=? AND USER_ID=?", filmId, userId);
    }

    public List<Film> findPopular(int count) {
        final String sql =
                "SELECT f.ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, " +
                        "       m.ID AS MPA_ID, m.NAME AS MPA_NAME " +
                        "FROM FILM f " +
                        "JOIN MPA m ON m.ID = f.MPA_ID " +
                        "LEFT JOIN FILM_LIKES fl ON fl.FILM_ID = f.ID " +
                        "GROUP BY f.ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, m.ID, m.NAME " +
                        "ORDER BY COUNT(fl.USER_ID) DESC, f.ID ASC " +
                        "LIMIT ?";
        List<Film> list = jdbc.query(sql, (rs, rn) -> {
            Film f = new Film();
            f.setId(rs.getLong("ID"));
            f.setName(rs.getString("NAME"));
            f.setDescription(rs.getString("DESCRIPTION"));
            Date d = rs.getDate("RELEASE_DATE");
            f.setReleaseDate(d != null ? d.toLocalDate() : null);
            f.setDuration(rs.getInt("DURATION"));
            f.setMpa(new Mpa(rs.getInt("MPA_ID"), rs.getString("MPA_NAME")));
            f.setGenres(new LinkedHashSet<>());
            return f;
        }, count);
        if (list.isEmpty()) return list;
        Map<Long, Film> map = list.stream().collect(Collectors.toMap(Film::getId, it -> it));
        loadGenresForFilms(map);
        return list;
    }

    private void syncGenres(long filmId, Set<Genre> genres) {
        jdbc.update("DELETE FROM FILM_GENRES WHERE FILM_ID=?", filmId);
        if (genres == null || genres.isEmpty()) return;
        final String ins = "INSERT INTO FILM_GENRES (FILM_ID, GENRE_ID) VALUES (?, ?)";
        for (Genre g : new LinkedHashSet<>(genres)) {
            try {
                jdbc.update(ins, filmId, g.getId());
            } catch (DuplicateKeyException ignore) {}
        }
    }

    private void loadGenresForFilms(Map<Long, Film> filmsById) {
        if (filmsById.isEmpty()) return;
        String placeholders = filmsById.keySet().stream().map(k -> "?").collect(Collectors.joining(","));
        String sql = "SELECT fg.FILM_ID, g.ID, g.NAME " +
                "FROM FILM_GENRES fg JOIN GENRE g ON g.ID = fg.GENRE_ID " +
                "WHERE fg.FILM_ID IN (" + placeholders + ") ORDER BY g.ID";
        Object[] args = filmsById.keySet().toArray();
        jdbc.query(sql, rs -> {
            long filmId = rs.getLong(1);
            int genreId = rs.getInt(2);
            String genreName = rs.getString(3);
            Film f = filmsById.get(filmId);
            if (f != null) {
                f.getGenres().add(new Genre(genreId, genreName));
            }
        }, args);
    }
}
