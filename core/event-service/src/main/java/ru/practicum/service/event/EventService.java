package ru.practicum.service.event;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.dto.event.*;
import ru.practicum.dto.reuqest.ParticipationRequestDto;

import java.util.List;

public interface EventService {
    EventDto getById(Long eventId);

    EventDto getPublic(long userId, Long eventId, HttpServletRequest request);

    List<EventDto> get(List<Long> eventIds);

    List<EventDto> getRecommendations(Long userId);

    List<EventDto> getPublished(List<Long> eventIds);

    List<EventShortDto> getByFilter(EventFilter filter, HttpServletRequest request);

    boolean existsById(Long id);

    boolean checkEventsExistInCategory(Long categoryId);

    EventDto create(Long userId, NewEventDto newEventDto);

    EventDto getByEventIdAndUserId(Long eventId, Long userId);

    Page<EventShortDto> getByUserId(Long userId, Pageable pageable);

    EventDto updateByAdmin(Long eventId, UpdateEventDto updated);

    EventDto updateByUser(Long eventId, Long userId, UpdateEventDto updated);

    boolean existsByIdAndInitiatorId(Long eventId, Long userId);

    EventRequestStatusUpdateResult updateRequestsStatus(Long eventId, Long userId,
                                                        EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);

    List<ParticipationRequestDto> findAllRequestsByEventId(Long eventId, Long userId);

    List<EventShortDto> findAllByIds(List<Long> eventIds);

    void likeEvent(Long userId, Long eventId);
}
