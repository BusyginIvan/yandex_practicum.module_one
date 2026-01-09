package ru.yandex.practicum.api.posts;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.posts.dto.PostCreateRequest;
import ru.yandex.practicum.api.posts.dto.PostDto;
import ru.yandex.practicum.api.posts.dto.PostUpdateRequest;
import ru.yandex.practicum.api.posts.dto.PostsPageResponse;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @GetMapping
    public PostsPageResponse getPosts(
            @RequestParam String search,
            @RequestParam int pageNumber,
            @RequestParam int pageSize
    ) {
        // TODO: implement
        return new PostsPageResponse(List.of(), false, false, 1);
    }

    @GetMapping("/{id}")
    public PostDto getPost(@PathVariable long id) {
        // TODO: implement
        return new PostDto(id, "", "", List.of(), 0, 0);
    }

    @PostMapping
    public PostDto createPost(@RequestBody PostCreateRequest request) {
        // TODO: implement
        return new PostDto(0L, request.title(), request.text(), request.tags(), 0, 0);
    }

    @PutMapping("/{id}")
    public PostDto updatePost(@PathVariable long id, @RequestBody PostUpdateRequest request) {
        // TODO: implement
        return new PostDto(id, request.title(), request.text(), request.tags(), 0, 0);
    }

    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable long id) {
        // TODO: implement
    }
}
