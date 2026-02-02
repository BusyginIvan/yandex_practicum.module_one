package ru.yandex.practicum.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
@Profile("!test")
public class FileSystemPostImageStorage implements PostImageStorage {

    private final Path imagesDir;

    public FileSystemPostImageStorage(@Value("${storage.images-dir}") String imagesDir) {
        this.imagesDir = Paths.get(imagesDir);
    }

    private Path pathForPost(long postId) {
        return imagesDir.resolve(Long.toString(postId));
    }

    @Override
    public void save(long postId, byte[] bytes) {
        try {
            Files.write(pathForPost(postId), bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image for post " + postId, e);
        }
    }

    @Override
    public byte[] read(long postId) {
        try {
            return Files.readAllBytes(pathForPost(postId));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read image for post " + postId, e);
        }
    }

    @Override
    public boolean exists(long postId) {
        return Files.exists(pathForPost(postId));
    }

    @Override
    public void delete(long postId) {
        try {
            Files.deleteIfExists(pathForPost(postId));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image for post " + postId, e);
        }
    }
}
