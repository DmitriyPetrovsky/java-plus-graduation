package ru.practicum.service.event;


import ru.practicum.dto.event.EventDto;

public interface EventServiceHelper {
    boolean existsById(Long id);

    boolean checkEventsExistInCategory(Long categoryId);

    EventDto getRaw(Long eventId);
}
