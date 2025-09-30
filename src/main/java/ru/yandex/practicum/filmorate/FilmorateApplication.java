package ru.yandex.practicum.filmorate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FilmorateApplication {
    public static void main(String[] args) {
        SpringApplication.run(FilmorateApplication.class, args);
        System.out.println("Привет! :)\nЯ умею обрабатывать и хранить данные о Ваших любимых фильмах.\nВыбрать что-нибудь для просмотра за ужином больше не составит труда!");
    }
}
