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

@RestController
@RequestMapping("/api/posts/{id}/image")
public class PostImageController {

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateImage(
            @PathVariable long id,
            @RequestParam("image") MultipartFile image
    ) {
        // TODO: implement
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<byte[]> getImage(@PathVariable long id) {
        // TODO: implement
        return ResponseEntity.notFound().build();
    }
}
