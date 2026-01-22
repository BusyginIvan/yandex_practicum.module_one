package ru.yandex.practicum.api;

import ru.yandex.practicum.exception.validation.IdMismatchException;

public final class IdChecks {

    public static void requireMatch(String what, long pathId, long bodyId) {
        if (pathId != bodyId) {
            throw new IdMismatchException(what, pathId, bodyId);
        }
    }
}
