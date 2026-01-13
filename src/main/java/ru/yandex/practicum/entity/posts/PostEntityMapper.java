package ru.yandex.practicum.entity.posts;

import org.mapstruct.Mapper;
import ru.yandex.practicum.domain.Post;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PostEntityMapper {
    Post toPost(PostEntity post, int commentsCount, List<String> tags);
}