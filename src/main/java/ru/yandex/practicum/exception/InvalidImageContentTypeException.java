package ru.yandex.practicum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidImageContentTypeException extends RuntimeException {
    public InvalidImageContentTypeException(String contentType) {
        super("Only image/* content types are allowed" +
            (contentType == null || contentType.isBlank() ? "" : (", got: " + contentType)));
    }
}
