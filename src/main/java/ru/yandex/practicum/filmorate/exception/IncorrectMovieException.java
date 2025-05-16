package ru.yandex.practicum.filmorate.exception;

public class IncorrectMovieException extends RuntimeException {
    public IncorrectMovieException(String message) {
        super(message);
    }
}
