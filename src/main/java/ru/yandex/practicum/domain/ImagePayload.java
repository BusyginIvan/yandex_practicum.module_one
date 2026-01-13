package ru.yandex.practicum.domain;

public record ImagePayload(String contentType, byte[] bytes) { }
