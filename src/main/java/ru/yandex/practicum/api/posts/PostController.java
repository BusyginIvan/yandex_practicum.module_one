package ru.yandex.practicum.api.posts;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
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
import ru.yandex.practicum.api.IdChecks;
import ru.yandex.practicum.domain.Post;
import ru.yandex.practicum.domain.PostPage;
import ru.yandex.practicum.service.PostService;

@Validated
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final PostApiMapper mapper;

    public PostController(PostService postService, PostApiMapper mapper) {
        this.postService = postService;
        this.mapper = mapper;
    }

    @GetMapping
    public PostsPageResponse getPosts(
        @RequestParam String search,
        @RequestParam @Positive int pageNumber,
        @RequestParam @Positive int pageSize
    ) {
        PostPage page = postService.search(search, pageNumber, pageSize);
        return mapper.toPostsPageResponse(page);
    }

    @GetMapping("/{id}")
    public PostDto getPost(@PathVariable @Positive long id) {
        Post post = postService.getPost(id);
        return mapper.toPostDto(post);
    }

    @PostMapping
    public PostDto createPost(@Valid @RequestBody PostCreateRequest request) {
        Post created = postService.create(request.title(), request.text(), request.tags());
        return mapper.toPostDto(created);
    }

    @PutMapping("/{id}")
    public PostDto updatePost(
        @PathVariable @Positive long id,
        @Valid @RequestBody PostUpdateRequest request
    ) {
        IdChecks.requireMatch("post", id, request.id());
        Post updated = postService.update(id, request.title(), request.text(), request.tags());
        return mapper.toPostDto(updated);
    }

    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable @Positive long id) {
        postService.delete(id);
    }
}
