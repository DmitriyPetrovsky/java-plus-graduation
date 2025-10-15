package ru.practicum.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.EventDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventsMapper;
import ru.practicum.repository.EventRepository;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceHelperImpl implements EventServiceHelper {
    private final EventRepository eventRepository;

    @Override
    public boolean existsById(Long id) {
        return eventRepository.existsById(id);
    }

    @Override
    public boolean checkEventsExistInCategory(Long categoryId) {
        return eventRepository.existsByCategoryId(categoryId);
    }

    @Override
    public EventDto getRaw(Long eventId) {
        EventDto event = eventRepository.findById(eventId)
                .map(EventsMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Событие с таким id не найдено"));
        return event;
    }
}
