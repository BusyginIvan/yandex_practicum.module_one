package ru.yandex.practicum.storage;

public interface PostImageStorage {
    void save(long postId, byte[] bytes);
    byte[] read(long postId);
    boolean exists(long postId);
    void delete(long postId);
}
