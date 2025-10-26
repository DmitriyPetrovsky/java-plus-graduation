package ru.practicum.mappers;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.model.Comment;


import java.time.LocalDateTime;

@UtilityClass
public class CommentMapper {

    public static CommentDto toCommentDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setUserId(comment.getUserId());
        dto.setEventId(comment.getEventId());
        dto.setCreatedOn(comment.getCreatedOn());

        return dto;
    }

    public static Comment fromNewCommentDto(NewCommentDto newCommentDto, Long userId, Long eventId) {
        if (newCommentDto == null) {
            return null;
        }

        Comment comment = new Comment();
        comment.setText(newCommentDto.getText());
        comment.setUserId(userId);
        comment.setEventId(eventId);
        comment.setCreatedOn(LocalDateTime.now());

        return comment;
    }
}
