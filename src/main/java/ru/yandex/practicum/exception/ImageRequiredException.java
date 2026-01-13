package ru.yandex.practicum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ImageRequiredException extends RuntimeException {
    public ImageRequiredException() {
        super("image is required");
    }
}