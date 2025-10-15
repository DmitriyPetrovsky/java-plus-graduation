package ru.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventShortDto;

import java.util.List;

@FeignClient("event-service")
public interface EventOperations {
    @GetMapping(path = "/api/events/{eventId}")
    EventDto getEventById(@PathVariable long eventId);

    @PostMapping(path = "/api/events/getbyids")
    List<EventShortDto> getEventsByIds(@RequestBody List<Long> eventIds);
}
