package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private Long id;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String login;

    /** Имя может быть пустым, правка имени выполняется в сервисе */
    private String name;

    @PastOrPresent
    private LocalDate birthday;

    @Builder.Default
    private Set<Long> friends = new HashSet<>();

    /** Конструктор, которого ждут тесты: (id, email, login, name, birthday) */
    public User(long id, String email, String login, String name, LocalDate birthday) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
        this.friends = new HashSet<>();
    }

    /** Гарантируем не-null коллекцию */
    public Set<Long> getFriends() {
        if (friends == null) friends = new HashSet<>();
        return friends;
    }

    /** Равенство по id, если он задан */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : Objects.hash(id);
    }
}
