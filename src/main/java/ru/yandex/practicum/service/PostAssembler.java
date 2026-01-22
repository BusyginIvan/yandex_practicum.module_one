package ru.yandex.practicum.service;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.domain.Post;
import ru.yandex.practicum.entity.posts.PostEntity;
import ru.yandex.practicum.entity.posts.PostEntityMapper;
import ru.yandex.practicum.repository.comments.CommentRepository;
import ru.yandex.practicum.repository.tags.TagRepository;

import java.util.List;
import java.util.Map;

@Component
public class PostAssembler {
    private final CommentRepository commentRepository;
    private final TagRepository tagRepository;
    private final PostEntityMapper mapper;

    public PostAssembler(
        CommentRepository commentRepository,
        TagRepository tagRepository,
        PostEntityMapper mapper
    ) {
        this.commentRepository = commentRepository;
        this.tagRepository = tagRepository;
        this.mapper = mapper;
    }

    public Post toPost(PostEntity entity) {
        int commentsCount = commentRepository.countByPostId(entity.id());
        List<String> tags = tagRepository.findTagsByPostId(entity.id());
        return mapper.toPost(entity, commentsCount, tags);
    }

    public List<Post> toPosts(List<PostEntity> entities) {
        List<Long> ids = entities.stream().map(PostEntity::id).toList();

        Map<Long, Integer> commentCounts = commentRepository.countByPostIds(ids);
        Map<Long, List<String>> tagsByPost = tagRepository.findTagsByPostIds(ids);

        return entities.stream()
            .map(e -> mapper.toPost(
                e,
                commentCounts.getOrDefault(e.id(), 0),
                tagsByPost.getOrDefault(e.id(), List.of())
            ))
            .toList();
    }
}