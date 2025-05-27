package ru.yandex.practicum.filmorate.model.film;

import lombok.Getter;

@Getter
public enum MpaRating {
    G("G", "нет ограничений"),
    PG("PG", "для детей рекомендуется с родителями"),
    PG_13("PG-13", "до 13 лет не рекомендуется"),
    R("R", "до 17 лет в присутствии взрослого"),
    NC_17("NC-17", "до 18 лет просмотр запрещён");

    private final String code;
    private final String description;

    MpaRating(String code, String description) {
        this.code = code;
        this.description = description;
    }

}
