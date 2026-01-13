package ru.yandex.practicum.api.images;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.domain.ImagePayload;
import ru.yandex.practicum.service.PostService;

@RestController
@RequestMapping("/api/posts/{id}/image")
public class PostImageController {

    private final PostService postService;

    public PostImageController(PostService postService) {
        this.postService = postService;
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateImage(
            @PathVariable long id,
            @RequestParam("image") MultipartFile image
    ) {
        postService.updatePostImage(id, image);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<byte[]> getImage(@PathVariable long id) {
        ImagePayload payload = postService.getPostImageOrDefault(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(payload.contentType()))
                .body(payload.bytes());
    }
}
