package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ValidationErrorResponse handleValidation(MethodArgumentNotValidException validationException) {
        Map<String, String> fieldErrorsMap = new HashMap<>();
        validationException.getBindingResult().getFieldErrors().forEach(fieldError -> fieldErrorsMap.put(fieldError.getField(), fieldError.getDefaultMessage()));
        log.warn("{}", fieldErrorsMap);
        return new ValidationErrorResponse("", fieldErrorsMap);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchElementException.class)
    public ErrorResponse handleNoSuchElementException(NoSuchElementException noSuchElementException) {
        log.warn("{}", noSuchElementException.getMessage());
        return new ErrorResponse(noSuchElementException.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ExistingUserException.class)
    public ErrorResponse handleExistingUserException(ExistingUserException existingUserException) {
        log.warn("{}", existingUserException.getMessage());
        return new ErrorResponse(existingUserException.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ExistingMovieException.class)
    public ErrorResponse handleExistingMovieException(ExistingMovieException existingMovieException) {
        log.warn("{}", existingMovieException.getMessage());
        return new ErrorResponse(existingMovieException.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleAnyException(Exception generalException) {
        log.error("{}", generalException.getMessage(), generalException);
        return new ErrorResponse(generalException.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ErrorResponse handleNotFoundException(NotFoundException notFoundException) {
        log.warn("{}", notFoundException.getMessage());
        return new ErrorResponse(notFoundException.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SQLException.class)
    public ErrorResponse handleSqlException(SQLException sqlException) {
        log.warn("DB error: {}", sqlException.getMessage());
        return new ErrorResponse("Invalid data: " + sqlException.getMessage());
    }
}
