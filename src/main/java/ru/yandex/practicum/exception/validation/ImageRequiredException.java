package ru.yandex.practicum.exception.validation;

public class ImageRequiredException extends ValidationException {
    public ImageRequiredException() {
        super("image is required");
    }
}