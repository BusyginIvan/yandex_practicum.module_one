package ru.yandex.practicum.api.posts;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.api.posts.dto.PostDto;
import ru.yandex.practicum.api.posts.dto.PostsPageResponse;
import ru.yandex.practicum.domain.Post;
import ru.yandex.practicum.domain.PostPage;

@Mapper(componentModel = "spring")
public interface PostApiMapper {

    PostDto toPostDto(Post post);

    @Mapping(target = "text", expression = "java(PostApiMapper.previewText(post.text()))")
    PostDto toPostPreviewDto(Post post);

    default PostsPageResponse toPostsPageResponse(PostPage page) {
        return new PostsPageResponse(
            page.posts().stream()
                .map(this::toPostPreviewDto)
                .toList(),
            page.hasPrev(),
            page.hasNext(),
            page.lastPage()
        );
    }

    static String previewText(String text) {
        if (text == null) return "";
        if (text.length() <= 128) return text;
        return text.substring(0, 128) + "…";
    }
}
