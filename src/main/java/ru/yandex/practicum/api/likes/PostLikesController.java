package ru.yandex.practicum.api.likes;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts/{id}/likes")
public class PostLikesController {

    @PostMapping
    public ResponseEntity<Integer> incrementLikes(@PathVariable long id) {
        // TODO: implement
        return ResponseEntity.ok(0);
    }
}
