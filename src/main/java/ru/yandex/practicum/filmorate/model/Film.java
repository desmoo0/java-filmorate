package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Film {

    private Long id;

    @NotBlank
    private String name;

    @Size(max = 200)
    private String description;

    @PastOrPresent
    private LocalDate releaseDate;

    @Positive
    private Integer duration;

    @Setter
    private Mpa mpa;

    @Builder.Default
    private Set<Genre> genres = new LinkedHashSet<>();

    @Builder.Default
    private Set<Long> likes = new HashSet<>();

    /** Валидация исторической границы кинопоказа 1895-12-28 */
    @AssertTrue(message = "releaseDate cannot be before 1895-12-28")
    public boolean isReleaseDateValid() {
        return releaseDate == null || !releaseDate.isBefore(LocalDate.of(1895, 12, 28));
    }

    /** Лояльный сеттер для коллекций из мапперов JDBC */
    public void setGenres(Collection<?> source) {
        if (source == null) {
            this.genres = new LinkedHashSet<>();
            return;
        }
        LinkedHashSet<Genre> target = new LinkedHashSet<>();
        for (Object o : source) {
            if (o instanceof Genre) {
                target.add((Genre) o);
            }
        }
        this.genres = target;
    }

    public Set<Genre> getGenres() {
        if (genres == null) genres = new LinkedHashSet<>();
        return genres;
    }

    public Set<Long> getLikes() {
        if (likes == null) likes = new HashSet<>();
        return likes;
    }

    public void addLike(Long userId) {
        getLikes().add(userId);
    }

    public void removeLike(Long userId) {
        getLikes().remove(userId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Film)) return false;
        Film film = (Film) o;
        return id != null && id.equals(film.id);
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : Objects.hash(id);
    }
}
