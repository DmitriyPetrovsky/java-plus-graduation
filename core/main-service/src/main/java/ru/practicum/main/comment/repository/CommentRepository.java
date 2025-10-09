package ru.practicum.main.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ru.practicum.main.comment.model.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
        @Query("SELECT c FROM Comment c WHERE c.event.id = :eventId")
        List<Comment> getAllByEventId(@Param("eventId") Long eventId);

        @Query("SELECT c FROM Comment c WHERE c.user.id = :userId")
        List<Comment> getAllByUserId(@Param("userId") Long userId);
}
