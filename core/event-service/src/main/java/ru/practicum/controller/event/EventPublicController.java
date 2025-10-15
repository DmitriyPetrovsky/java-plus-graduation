package ru.practicum.controller.event;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.StatsOperations;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventFilter;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.exception.InternalServerException;
import ru.practicum.service.event.EventService;


import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequestMapping(path = "/events")
@RequiredArgsConstructor
@RestController
@Validated
public class EventPublicController {
    private final StatsOperations statsClient;
    private final EventService eventService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> find(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size,
            HttpServletRequest request) {

        saveHit(request);

        EventFilter param = EventFilter.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .from(from)
                .size(size)
                .build();

        return eventService.getByFilter(param);
    }

    @GetMapping(path = "/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventDto getById(@PathVariable @Positive Long eventId, HttpServletRequest request) {

        saveHit(request);

        eventService.increaseViews(eventId, request.getRemoteAddr());
        return eventService.getPublic(eventId);
    }

    private void saveHit(HttpServletRequest request) {
        HitDto hitDto = new HitDto();
        hitDto.setApp("event-service");
        hitDto.setIp(request.getRemoteAddr());
        hitDto.setUri(request.getRequestURI());
        hitDto.setTimestamp(LocalDateTime.now());
        try {
            statsClient.save(hitDto);
        } catch (FeignException e) {
            log.error("Ошибка при обращении к сервису stats-server.");
            throw new InternalServerException("Ошибка при обращении к сервису stats-server.");
        }
    }
}
