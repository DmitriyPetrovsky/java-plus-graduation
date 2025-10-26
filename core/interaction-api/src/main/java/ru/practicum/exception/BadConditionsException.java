package ru.practicum.exception;

public class BadConditionsException extends RuntimeException {
    public BadConditionsException(String message) {
        super(message);
    }
}