package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({MpaDbStorage.class})
class MpaDbStorageIT {

    private final MpaDbStorage storage;
    private final JdbcTemplate jdbc;

    @BeforeEach
    void seedDict() {
        jdbc.update("MERGE INTO MPA (ID, NAME) KEY(ID) VALUES (1, 'G')");
        jdbc.update("MERGE INTO MPA (ID, NAME) KEY(ID) VALUES (2, 'PG')");
    }

    @Test
    void findAll_and_findById_work() {
        List<Mpa> all = storage.findAll();
        assertThat(all).isNotEmpty();

        Mpa m = storage.findById(1).orElseThrow();
        assertThat(m.getName()).isEqualTo("G");
    }
}
