package ru.yandex.practicum.filmorate.model.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private Long id;

    @NotBlank(message = "Емайл пользователя не может быть пустым")
    @NotNull(message = "Емайл пользователя не может быть пустым")
    private String email;

    @NotBlank(message = "Логин пользователя не может быть пустым")
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}
