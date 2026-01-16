package ru.yandex.practicum.exception.not_found;

public class PostNotFoundException extends NotFoundException {
    public PostNotFoundException(long id) {
        super("Post not found: " + id);
    }
}
