package ru.yandex.practicum.exception.validation;

public class IdMismatchException extends ValidationException {
    public IdMismatchException(String what, long pathId, long bodyId) {
        super(what + " id mismatch: pathId=" + pathId + ", bodyId=" + bodyId);
    }
}
