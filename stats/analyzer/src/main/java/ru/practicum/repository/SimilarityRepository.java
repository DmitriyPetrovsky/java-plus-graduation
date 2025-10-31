package ru.practicum.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.model.EventSimilarity;

import java.util.List;
import java.util.Set;

@Repository
public interface SimilarityRepository extends JpaRepository<EventSimilarity, Long> {

    List<EventSimilarity> findAllByEventAIn(Set<Long> eventIds, PageRequest pageRequest);

    @Query("SELECT COUNT(s) > 0 FROM EventSimilarity s WHERE s.eventA = :eventA AND s.eventB = :eventB")
    boolean existsByEventAAndEventB(Long eventA, Long eventB);

    List<EventSimilarity> findAllByEventBIn(Set<Long> eventIds, PageRequest pageRequest);

    List<EventSimilarity> findAllByEventA(Long eventId, PageRequest pageRequest);

    List<EventSimilarity> findAllByEventB(Long eventId, PageRequest pageRequest);

    @Query("SELECT es FROM EventSimilarity es " +
            "WHERE (es.eventA = :eventId OR es.eventB = :eventId) " +
            "AND NOT EXISTS (SELECT 1 FROM UserAction ua WHERE ua.eventId IN (es.eventA, es.eventB) AND ua.userId = :userId) " +
            "ORDER BY es.score DESC")
    List<EventSimilarity> findSimilarEventsExcludingUser(@Param("eventId") Long eventId,
                                                         @Param("userId") Long userId,
                                                         PageRequest pageRequest);

}
