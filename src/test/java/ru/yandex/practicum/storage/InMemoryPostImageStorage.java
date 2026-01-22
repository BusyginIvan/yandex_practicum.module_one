package ru.yandex.practicum.storage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPostImageStorage implements PostImageStorage {

    private final Map<Long, byte[]> storage = new ConcurrentHashMap<>();

    @Override
    public void save(long postId, byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes must not be null");
        }
        storage.put(postId, bytes);
    }

    @Override
    public byte[] read(long postId) {
        byte[] bytes = storage.get(postId);
        if (bytes == null) {
            throw new IllegalStateException("Image not found for postId=" + postId);
        }
        return bytes;
    }

    @Override
    public boolean exists(long postId) {
        return storage.containsKey(postId);
    }

    @Override
    public void delete(long postId) {
        storage.remove(postId);
    }

    public void clear() {
        storage.clear();
    }
}
