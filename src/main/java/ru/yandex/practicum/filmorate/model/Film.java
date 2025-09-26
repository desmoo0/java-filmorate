package ru.yandex.practicum.filmorate.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Film {

    private Long id;

    @NotBlank(message = "Имя не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание максимум 200 символов")
    private String description;

    @PastOrPresent(message = "Дата выпуска должна быть прошлой или настоящей")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность должна быть положительной")
    private int duration;

    private Mpa mpa;

    @Valid
    @Builder.Default
    private Set<Genre> genres = new LinkedHashSet<>();

    @Builder.Default
    private Set<Long> likes = new HashSet<>();

    @AssertTrue(message = "Дата выхода не может быть ранее 1895-12-28")
    private boolean isReleaseDateValid() {
        return releaseDate == null || !releaseDate.isBefore(LocalDate.of(1895, 12, 28));
    }

    public void addLike(Long userId) {
        likes.add(userId);
    }

    public void removeLike(Long userId) {
        likes.remove(userId);
    }
}
