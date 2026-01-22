package ru.yandex.practicum.api.comments;

import org.mapstruct.Mapper;
import ru.yandex.practicum.api.comments.dto.CommentDto;
import ru.yandex.practicum.domain.Comment;

@Mapper(componentModel = "spring")
public interface CommentApiMapper {
    CommentDto toDto(Comment comment);
}
