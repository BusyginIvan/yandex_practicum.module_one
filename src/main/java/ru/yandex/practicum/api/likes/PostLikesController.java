package ru.yandex.practicum.api.likes;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.service.PostService;

@RestController
@RequestMapping("/api/posts/{id}/likes")
public class PostLikesController {

    private final PostService postService;

    public PostLikesController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<Integer> incrementLikes(@PathVariable long id) {
        int newCount = postService.incrementLikes(id);
        return ResponseEntity.ok(newCount);
    }
}
