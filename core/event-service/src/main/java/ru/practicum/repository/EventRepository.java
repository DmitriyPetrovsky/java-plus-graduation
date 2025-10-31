package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import ru.practicum.dto.event.EventState;
import ru.practicum.model.event.Event;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {
    List<Event> findByIdIn(List<Long> userIds);

    Optional<Event> findByIdAndState(Long id, EventState state);

    List<Event> findByIdInAndState(List<Long> id, EventState state);

    Page<Event> findAllByInitiatorId(Long userId, Pageable page);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    Page<Event> findAll(Specification<Event> spec, Pageable pageable);

    boolean existsByIdAndInitiatorId(Long eventId, Long userId);

    boolean existsByCategoryId(Long categoryId);

    boolean existsById(@NonNull Long eventId);

    @Modifying
    @Query("UPDATE Event e SET e.confirmedRequests = :count WHERE e.id = :eventId")
    void setContirmedRequestsCount(
            @Param("eventId") Long eventId,
            @Param("count") Long count);

    Set<Event> findAllByIdIn(Set<Long> ids);
}
