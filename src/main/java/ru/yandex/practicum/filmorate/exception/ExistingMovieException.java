package ru.yandex.practicum.filmorate.exception;

public class ExistingMovieException extends RuntimeException {
    public ExistingMovieException(String message) {
        super(message);
    }
}
