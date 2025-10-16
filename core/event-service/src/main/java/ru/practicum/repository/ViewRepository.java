package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.views.View;

public interface ViewRepository extends JpaRepository<View, Long> {
    boolean existsByIpAndEventId(String ip, Long eventId);

    Long countByEventId(Long eventId);
}
