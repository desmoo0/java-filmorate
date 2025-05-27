package ru.yandex.practicum.filmorate.model.film;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum MpaRating {
    G(1, "G"),
    PG(2, "PG"),
    PG_13(3, "PG-13"),
    R(4, "R"),
    NC_17(5, "NC-17");

    private final int id;
    private final String name;

    MpaRating(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @JsonCreator
    public static MpaRating fromId(int id) {
        for (MpaRating r : values()) {
            if (r.id == id) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown MPA rating id: " + id);
    }

    @JsonValue
    public int toValue() {
        return id;
    }
}
