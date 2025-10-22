package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventState;
import ru.practicum.exception.ValidationException;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.model.Comment;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.dto.UpdateCommentDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.feign.EventOperations;
import ru.practicum.mappers.CommentMapper;
import ru.practicum.repository.CommentRepository;


import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final EventOperations eventClient;

    public CommentDto get(Long id) {
        return commentRepository.findById(id)
                .map(CommentMapper::toCommentDto)
                .orElseThrow(() -> new NotFoundException("Комментарий с таким id не найден"));
    }

    public List<CommentDto> getCommentsByEventId(Long eventId) {
        return commentRepository.getAllByEventId(eventId)
                .stream().map(CommentMapper::toCommentDto).toList();
    }

    public List<CommentDto> getAllForUser(Long userId) {
        return commentRepository.getAllByUserId(userId)
                .stream().map(CommentMapper::toCommentDto).toList();
    }

    @Transactional
    public CommentDto createComment(Long userId, NewCommentDto newCommentDto) {
        EventDto event = eventClient.getEventById(newCommentDto.getEventId());

        if (!event.getState().equals(EventState.PUBLISHED.toString())) {
            throw new ValidationException("Можно комментировать только опубликованные события");
        }

        Comment newComment = new Comment();
        newComment.setCreatedOn(LocalDateTime.now());
        newComment.setUserId(userId);
        newComment.setEventId(newCommentDto.getEventId());
        newComment.setText(newCommentDto.getText());
        return CommentMapper.toCommentDto(commentRepository.save(newComment));
    }

    @Transactional
    public void deleteCommentByIdByOwner(Long userId, Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Комментарий с таким id не найден");
        }

        CommentDto stored = getDtoOrThrow(commentId);
        if (!userId.equals(stored.getUserId())) {
            throw new AccessDeniedException("Удалять комментарий может только автор или администратор");
        }
        commentRepository.deleteById(commentId);
        commentRepository.flush();
    }

    @Transactional
    public void deleteCommentByIdByAdmin(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Комментарий с таким id не найден");
        }
        commentRepository.deleteById(commentId);
        commentRepository.flush();
    }

    @Transactional
    public CommentDto updateCommentForEvent(Long commentId, Long userId, UpdateCommentDto dto) {
        Comment stored = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден!"));
        if (!userId.equals(stored.getUserId())) {
            throw new AccessDeniedException("Обновлять комментарий может только автор или администратор");
        }
        if (dto.getText() != null) {
            stored.setText(dto.getText());
            stored = commentRepository.save(stored);
        }
        return CommentMapper.toCommentDto(stored);
    }

    public List<CommentDto> getAll(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "createdOn"));
        Page<Comment> comments = commentRepository.findAll(pageable);
        return comments.stream().map(CommentMapper::toCommentDto).toList();
    }

    private CommentDto getDtoOrThrow(Long id) {
        return commentRepository.findById(id)
                .map(CommentMapper::toCommentDto)
                .orElseThrow(() -> new NotFoundException("Комментарий с таким id не найден"));
    }
}
