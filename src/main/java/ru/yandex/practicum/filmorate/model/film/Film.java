package ru.yandex.practicum.filmorate.model.film;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Setter;
import ru.yandex.practicum.filmorate.validator.AfterDate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private static final LocalDate MIN_DATE = LocalDate.of(1895, 12, 28);

    private Long id;

    @NotBlank(message = "Название фильма не может быть пустым.")
    private String name;

    @Size(max = 200, message = "Описание фильма не должно превышать 200 символов.")
    private String description;

    @NotNull
    @AfterDate(value = "1895-12-27", message = "Дата не ранее 28.12.1895.")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительной.")
    private int duration;

    @Setter
    private Set<Long> likes = new HashSet<>();

    @NotEmpty(message = "Укажите хотя бы один жанр")
    private Set<Genre> genres = new HashSet<>();

    @NotNull(message = "Укажите рейтинг МРА")
    private MpaRating mpaRating;

}
