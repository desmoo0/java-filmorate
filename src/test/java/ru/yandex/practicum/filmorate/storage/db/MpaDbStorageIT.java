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

    private final MpaDbStorage mpaDbStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void seedDatabase() {
        jdbcTemplate.update("MERGE INTO MPA (ID, NAME) KEY(ID) VALUES (1, 'G')");
        jdbcTemplate.update("MERGE INTO MPA (ID, NAME) KEY(ID) VALUES (2, 'PG')");
    }

    @Test
    void findAll_and_findById_work() {
        List<Mpa> allMpas = mpaDbStorage.findAll();
        assertThat(allMpas).isNotEmpty();

        Mpa mpa = mpaDbStorage.findById(1).orElseThrow();
        assertThat(mpa.getName()).isEqualTo("G");
    }
}
