package ru.yandex.practicum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PostImageNotFoundException extends RuntimeException {
    public PostImageNotFoundException(long postId) {
        super("Image for post " + postId + " not found");
    }
}
