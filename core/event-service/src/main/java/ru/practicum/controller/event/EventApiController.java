package ru.practicum.controller.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.feign.EventOperations;

import ru.practicum.service.event.EventService;

import java.util.List;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
public class EventApiController implements EventOperations {

    private final EventService eventService;

    @Override
    public EventDto getEventById(long eventId) {
        log.info("Межсервисное взаимодействие: получение события по ID: {}", eventId);
        return eventService.getById(eventId);
    }

    @Override
    public List<EventShortDto> getEventsByIds(List<Long> eventIds) {
        log.info("Межсервисное взаимодействие: получение событий по списку ID");
        return eventService.findAllByIds(eventIds);
    }

}
