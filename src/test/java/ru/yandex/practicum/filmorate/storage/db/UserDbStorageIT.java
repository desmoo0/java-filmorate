package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class})
class UserDbStorageIT {

    private final UserDbStorage userStorage;

    @Test
    void create_and_findById_and_containsKey() {
        User u = new User(0L, "user@mail.ru", "login", "Имя", LocalDate.of(1990, 1, 1));
        u = userStorage.create(u);

        assertThat(u.getId()).isPositive();
        assertThat(userStorage.containsKey(u.getId())).isTrue();

        Optional<User> byId = userStorage.findById(u.getId());
        assertThat(byId).isPresent();
        assertThat(byId.get().getEmail()).isEqualTo("user@mail.ru");
    }

    @Test
    void update_updates_fields() {
        User u = userStorage.create(new User(0L, "a@a", "a", "A", LocalDate.of(2000, 1, 1)));
        u.setName("B");
        u.setEmail("b@b");
        userStorage.update(u);

        User actual = userStorage.findById(u.getId()).orElseThrow();
        assertThat(actual.getName()).isEqualTo("B");
        assertThat(actual.getEmail()).isEqualTo("b@b");
    }

    @Test
    void findAll_returns_list() {
        userStorage.create(new User(0L, "1@x", "l1", "N1", LocalDate.of(1991, 1, 1)));
        userStorage.create(new User(0L, "2@x", "l2", "N2", LocalDate.of(1992, 2, 2)));

        List<User> all = userStorage.findAll();
        assertThat(all.size()).isGreaterThanOrEqualTo(2);
    }
}
