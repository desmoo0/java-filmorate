package ru.yandex.practicum.filmorate.model.film;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Mpa {
    private final int id;
    private String name;

    @JsonCreator
    public Mpa(@JsonProperty("id") int id) {
        this.id = id;
    }
}
