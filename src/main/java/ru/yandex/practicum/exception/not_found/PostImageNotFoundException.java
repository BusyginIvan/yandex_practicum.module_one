package ru.yandex.practicum.exception.not_found;

public class PostImageNotFoundException extends NotFoundException {
    public PostImageNotFoundException(long postId) {
        super("Image for post " + postId + " not found");
    }
}
